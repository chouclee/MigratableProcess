package cmu.edu.ds.mprocess.process;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessManager {
  private ConcurrentHashMap<Long, MigratableProcess> processPool;

  private static long processSeqNumber;

  public ProcessManager() {
    processPool = new ConcurrentHashMap<Long, MigratableProcess>();
    processSeqNumber = -1;
  }

  public void launchingProcess(String className, String[] args) throws ClassNotFoundException,
          InstantiationException, IllegalAccessException, IllegalArgumentException,
          InvocationTargetException, NoSuchMethodException, SecurityException {
    Class<?> newProcess = Class.forName(className);
    // assume all classes implements MigratableProcess must have a constructor taking
    // array of strings
    if (MigratableProcess.class.isAssignableFrom(newProcess)) {
      Constructor<?> newProcessCtor = newProcess
              .getDeclaredConstructor(new Class[] { String[].class });

      MigratableProcess processInstance = (MigratableProcess) newProcessCtor
              .newInstance((Object) args);

      Thread t = new Thread(processInstance);
      t.start();
      processPool.putIfAbsent(nextProcessID(), processInstance);
    } else {
      System.err.println("The Class " + className
              + " has not implemented MigratableProcess Interface");
    }
  }

  /*
   * Generate Process ID This is a thread-safe method.
   */
  private static synchronized long nextProcessID() {
    return ++processSeqNumber;
  }
  
  private void commandLineParsing(String commandLine) {
    if (commandLine == null || commandLine.length() == 0)
      return;
    
    String[] args = commandLine.split("\\s+");
    if (args == null || args.length == 0) {
      return;
    }
    
    switch (args[0].toLowerCase()) {
      case "ps":
        commandPS();
        break;
      default:
        System.err.println("Unknown command: " + commandLine);
    }
  }
  
  private void commandPS() {
    ArrayList<Long> idPool = new ArrayList<Long>(processPool.keySet());
    if (idPool == null || idPool.size() == 0) {
      System.out.println("No running process");
      return;
    }
    
    Collections.sort(idPool);
    for (Long id : idPool)
      System.out.println(processPool.get(id).toString());
  }

}
