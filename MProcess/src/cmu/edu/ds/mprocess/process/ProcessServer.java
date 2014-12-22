package cmu.edu.ds.mprocess.process;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ProcessServer implements Runnable {

  private ServerSocket server;

  private boolean running;

  public ProcessServer(int port) {
    try {
      server = new ServerSocket(port);
      System.out.println("Starting server on port: " + server.getLocalPort());
      System.out.println("IP Address: " + server.getInetAddress());
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
      new Thread(new ProcessClient(client)).start();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      System.err.println("Server acception error");
      e.printStackTrace();
    }
  }

  public void run() {
    running = true;
    while (running) {
      accept();
    }
  }

  public void stop() {
    running = false;
  }

}
