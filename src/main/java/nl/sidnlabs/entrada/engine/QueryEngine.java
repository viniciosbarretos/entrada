package nl.sidnlabs.entrada.engine;

import java.util.Set;
import java.util.concurrent.Future;
import nl.sidnlabs.entrada.model.Partition;
import nl.sidnlabs.entrada.model.jpa.TablePartition;

public interface QueryEngine {

  Future<Boolean> execute(String sql);

  Future<Boolean> addPartition(String table, Set<Partition> partitions, String location);

  boolean compact(TablePartition p);
}
