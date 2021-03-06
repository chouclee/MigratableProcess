package cmu.edu.ds.mprocess.process;

import java.io.Serializable;

public interface MigratableProcess extends Runnable, Serializable {
  void run();

  void suspend();
}
