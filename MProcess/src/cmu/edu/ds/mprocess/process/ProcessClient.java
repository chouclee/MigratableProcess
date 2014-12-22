package cmu.edu.ds.mprocess.process;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class ProcessClient implements Runnable {
  private Socket client;

  public ProcessClient(Socket socket) {
    this.client = socket;
  }

  public void run() {
    try {
      ObjectInputStream in = new ObjectInputStream(client.getInputStream());
      DataOutputStream out = new DataOutputStream(client.getOutputStream());

      Object obj = in.readObject();
      MigratableProcess process = null;

      if (obj instanceof MigratableProcess) {
        process = (MigratableProcess) obj;
        out.writeBoolean(true);
        ProcessManager.getInstance().launchingProcess(process);
      } else {
        out.writeBoolean(false);
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
