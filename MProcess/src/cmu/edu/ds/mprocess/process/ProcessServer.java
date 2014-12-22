package cmu.edu.ds.mprocess.process;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ProcessServer implements Runnable {
  // public static final int PORT = 8888;
  public static final String HOSTNAME = "localhost";

  private ServerSocket server;

  public ProcessServer(int port) {
    try {
      server = new ServerSocket(port);
      System.out.println("Starting server on port: " + server.getLocalPort());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      System.err.println("Failed to open port " + port);
      e.printStackTrace();
    }
  }

  private void accept() {
    Socket client = null;
    try {
      client = server.accept();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      System.err.println("Server acception error");
      e.printStackTrace();
    }
  }

  public void run() {
    accept();
  }
}
