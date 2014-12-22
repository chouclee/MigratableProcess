package cmu.edu.ds.mprocess.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessManager {
  private static final ProcessManager INSTANCE = new ProcessManager();
  private ConcurrentHashMap<Long, MigratableProcess> processPool;
  private ConcurrentHashMap<MigratableProcess, Long> idPool;

  private static long processSeqNumber;
  
  //private Set<Class<? extends MigratableProcess>> migratableProcessSet;
  
  private ProcessManager() {
    processPool = new ConcurrentHashMap<Long, MigratableProcess>();
    idPool = new ConcurrentHashMap<>();
    processSeqNumber = -1;
  }

  public static ProcessManager getInstance() {
    return INSTANCE;
  }
  
  public void launchingProcess(String className, String... args) throws ClassNotFoundException,
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
      long id = nextProcessID();
      processPool.putIfAbsent(id, processInstance);
      idPool.putIfAbsent(processInstance, id);
    } else {
      System.err.println("The Class " + className
              + " has not implemented MigratableProcess Interface");
    }
  }
  
  public void launchingProcess(MigratableProcess processInstance) {
    System.out.println("Migrated...\n" + ((GrepProcess)processInstance).toString());
    Thread t = new Thread(processInstance);
    t.start();
    long id = nextProcessID();
    processPool.putIfAbsent(id, processInstance);
    idPool.putIfAbsent(processInstance, id);
  }
  
  public void removeProcess(MigratableProcess processInstance) {
    long id = idPool.get(processInstance);
    idPool.remove(processInstance);
    processPool.remove(id);
  }

  /*
   * Generate Process ID This is a thread-safe method.
   */
  private static synchronized long nextProcessID() {
    return ++processSeqNumber;
  }
  
  public void startServer() {
    Thread t = new Thread(new ProcessServer(0));
    t.start();
  }

  public void startConsole() {
    System.out.println("Type in command:");
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    while (true) {
      String commandLine = null;
      try {
        commandLine = reader.readLine();
      } catch (IOException e) {
        System.err.println("Cannot input empty command");
        System.exit(-1);
      }
      commandLineParsing(commandLine);
    }
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
      case "run":
        commandLaunch(args);
        break;
      case "mg":
        commandMG(args);
        break;
      case "quit":
        commandQuit();
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
      System.out.println("ID=" + id + "\t" + processPool.get(id).toString());
  }
  
  private void commandLaunch(String[] args) {
    if (args.length <= 1) {
      System.out.println("Usage: run Classname args1 args2 ...");
      return;
    }
    if (args.length == 2) {
      try {
        launchingProcess(args[1]);
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
              | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
              | SecurityException e) {
        // TODO Auto-generated catch block
        System.err.println("Failed to lauch process");
      }
    } else {
      String[] classArgs = new String[args.length - 2];
      for (int i = 2; i < args.length; i++)
        classArgs[i-2] = args[i];
      try {
        launchingProcess(args[1], classArgs);
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
              | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
              | SecurityException e) {
        // TODO Auto-generated catch block
        System.err.println("Failed to lauch process");
      }
    }
  }

  private void commandMG(String[] args) {
    if (args.length <= 3) {
      System.out.println("Usage: mg processID hostname port");
      return;
    }

    long id = Long.parseLong(args[1]);
    MigratableProcess process = processPool.get(id);
    if (process == null) {
      System.err.println("No such process, process ID = " + id);
      return;
    }
    String hostname = args[2];
    int port = Integer.parseInt(args[3]);
    Socket socket = null;
    try {
      socket = new Socket(hostname, port);
      process.suspend();
      startMigrating(socket, process, hostname);
      socket.close();
    } catch (IOException e) {
      System.err.println("Failed to connect " + hostname);
    }

  }

  private void startMigrating(Socket socket, MigratableProcess process, String hostname) throws IOException {
    // TODO Auto-generated method stub
    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
    try {
      out.writeObject(process);
    } catch (IOException e) {
      System.err.println("Failed to migrate process to " + hostname);
      return;
    }
    out.close();
    socket.close();
  }
  
  private void commandQuit() {
    System.out.println("Quit...");
    System.exit(0);
  }

  public static void main(String[] args) {
    ProcessManager.getInstance().startServer();
    ProcessManager.getInstance().startConsole();
  }
}
