package cmu.edu.ds.mprocess.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class TransactionalFileInputStream extends InputStream {
  private long offset;
  private File file;
  private transient RandomAccessFile fileHandler;
  
  public TransactionalFileInputStream(String filePath) {
    // TODO Auto-generated constructor stub
    this.file = new File(filePath);
    this.offset = 0;
  }
  
  @Override
  public int read() throws IOException {
    // TODO Auto-generated method stub
    if (fileHandler == null) {
      fileHandler = new RandomAccessFile(file, "r");
      fileHandler.seek(offset);
    }
    int result = fileHandler.read();
    if (result != -1)
      offset++;
    return result;
  }
  
  public int read(byte[] b) throws IOException {
    if (fileHandler == null) {
      fileHandler = new RandomAccessFile(file, "r");
      fileHandler.seek(offset);
    }
    int result = fileHandler.read(b);
    if (result != -1)
      offset += result;
    return result;
  }
  
  public static void main(String[] args) throws IOException {
    TransactionalFileInputStream stream = new TransactionalFileInputStream(args[0]);
    System.out.println(stream.read());
    System.out.println(stream.read());
    System.out.println(stream.read());
    System.out.println(stream.read());
    byte[] result = new byte[8];
    //System.out.println(stream.read(result));
    for (byte b : result)
      System.out.println((int)b);
    stream.close();
  }

}
