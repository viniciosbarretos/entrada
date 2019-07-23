package nl.sidnlabs.entrada;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.log4j.Log4j2;
import nl.sidnlabs.entrada.enrich.resolver.DnsResolverCheck;
import nl.sidnlabs.entrada.load.PacketProcessor;

@Log4j2
@Component
public class ScheduledExecution {

  private ServerContext serverCtx;
  private ApplicationContext applicationContext;
  private List<DnsResolverCheck> resolverChecks;

  @Value("${entrada.nameservers}")
  private String servers;

  private Timer processTimer;

  public ScheduledExecution(ServerContext serverCtx, ApplicationContext applicationContext,
      List<DnsResolverCheck> resolverChecks, MeterRegistry registry) {

    this.serverCtx = serverCtx;
    this.applicationContext = applicationContext;
    this.resolverChecks = resolverChecks;

    processTimer = registry.timer("processor.execution.time");
  }

  @Scheduled(fixedDelayString = "#{${entrada.execution.delay}*1000}")
  public void run() {
    log.info("Start loading data forname servers: {}", servers);
    // initialize DnsResolverCheck to make sure they use uptodate data
    resolverChecks.stream().forEach(DnsResolverCheck::init);

    // create new processor each time, to avoid caches getting too big or having
    // memory leaks leading to OOM Exceptions
    PacketProcessor processor = applicationContext.getBean(PacketProcessor.class);

    if (StringUtils.isBlank(servers)) {
      // no individual servers configured, assume the pcap data is in the input location root dir
      runForServer("", processor);
    } else {
      // individual servers configured, process each server directory
      Arrays.stream(StringUtils.split(servers, ",")).forEach(s -> runForServer(s, processor));
    }

    log.info("Completed loading name server data");
  }

  private void runForServer(String server, PacketProcessor processor) {
    log.info("Start loading name data for server: {}", server);

    serverCtx.setServer(server);

    try {
      // record time spent while processing all pcap files
      processTimer.record(processor::execute);
    } catch (Exception e) {
      log.error("Error while processing pcap data for name server: {}", server, e);
    }

    log.info("Completed loading data for name server: {}", server);
  }

}
