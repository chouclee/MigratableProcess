package cmu.edu.ds.mprocess.transaction;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

public class TransactionalFileOutputStream extends OutputStream implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 11L;

  private long offset;

  private File file;

  private transient RandomAccessFile fileHandler;

  public TransactionalFileOutputStream(String filePath) {
    // TODO Auto-generated constructor stub
    this.file = new File(filePath);
    this.offset = 0;
  }

  @Override
  public void write(int b) throws IOException {
    // TODO Auto-generated method stub
    if (fileHandler == null) {
      fileHandler = new RandomAccessFile(file, "rw");
      fileHandler.seek(offset);
    }
    fileHandler.write(b);
    offset++;
  }

  public void write(byte[] b) throws IOException {
    if (fileHandler == null) {
      fileHandler = new RandomAccessFile(file, "rw");
      fileHandler.seek(offset);
    }
    for (byte each : b)
      fileHandler.write(each);
    offset += b.length;
  }
}
