package cmu.edu.ds.mprocess.process;

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
  
  private boolean finished = false;

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
    BufferedReader in = new BufferedReader(new InputStreamReader(inFile));

    try {
      while (!suspending) {
        String line = in.readLine();

        if (line == null)
          break;

        if (line.contains(query)) {
          out.println(line);
        }

        // Make grep take longer so that we don't require extremely large files for interesting
        // results
        try {
          Thread.sleep(1500);
        } catch (InterruptedException e) {
          // ignore it
        }
      }
    } catch (EOFException e) {
      // End of File
    } catch (IOException e) {
      System.out.println("GrepProcess: Error: " + e);
    }
    finished = true;
    suspending = false;
  }

  public void suspend() {
    suspending = true;
    while (suspending);
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("GrepProcess ");
    for (String str : args)
      sb.append(str + " ");
    sb.append(finished ? "\tFinished." : "\tIn progress...");
    return sb.toString();
  }

}
