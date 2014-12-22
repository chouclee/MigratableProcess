package cmu.edu.ds.mprocess.process;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.EOFException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.Thread;
import java.lang.InterruptedException;

import cmu.edu.ds.mprocess.transaction.TransactionalFileInputStream;
import cmu.edu.ds.mprocess.transaction.TransactionalFileOutputStream;

public class GrepProcess implements MigratableProcess {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private TransactionalFileInputStream inFile;

  private TransactionalFileOutputStream outFile;

  private String query;

  private volatile boolean suspending;
  
  private String[] args;
  
  //private boolean finished = false;

  public GrepProcess(String args[]) throws Exception {
    this.args = args;
    if (args.length != 3) {
      System.out.println("usage: GrepProcess <queryString> <inputFile> <outputFile>");
      throw new Exception("Invalid Arguments");
    }

    query = args[0];
    inFile = new TransactionalFileInputStream(args[1]);
    outFile = new TransactionalFileOutputStream(args[2]);
  }

  public void run() {
    PrintStream out = new PrintStream(outFile);
    //BufferedReader in = new BufferedReader(new InputStreamReader(inFile));
    DataInputStream in = new DataInputStream(inFile);
    try {
      while (!suspending) {
        @SuppressWarnings("deprecation")
        String line = in.readLine();
        //System.out.println("processing to " + inFile.getOffset() + " bytes...");
        if (line == null) {
          System.out.println("processing finished");
          break;
        }

        if (line.contains(query)) {
          out.println(line);
        }

        // Make grep take longer so that we don't require extremely large files for interesting
        // results
        try {
          Thread.sleep(3000);
        } catch (InterruptedException e) {
          // ignore it
        }
      }
    } catch (EOFException e) {
      // End of File
    } catch (IOException e) {
      System.out.println("GrepProcess: Error: " + e);
    } finally {
      //finished = true;
      ProcessManager.getInstance().removeProcess(this);
      suspending = false;
    }
  }

  public void suspend() {
    suspending = true;
    while (suspending) {
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("GrepProcess ");
    for (String str : args)
      sb.append(str + " ");
    sb.append(suspending ? "\tFinished." : "\tIn progress...");
    return sb.toString();
  }

}
