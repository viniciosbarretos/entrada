package nl.sidnlabs.entrada.enrich.geoip;

import java.net.InetAddress;
import java.util.Optional;


public interface GeoIPService {

  Optional<String> lookupCountry(String ip);

  Optional<String> lookupCountry(InetAddress addr);

  Optional<String> lookupASN(InetAddress ip);

  Optional<String> lookupASN(String ip);

}