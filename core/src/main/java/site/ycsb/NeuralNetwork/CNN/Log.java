package site.ycsb.CNN;

import java.io.PrintStream;

/**
 * 
 */
public final class Log {
  private Log(){
  }
  private static PrintStream stream = System.out;
  
  public static void i(String tag, String msg){
    stream.println(tag+"\t"+msg);
  }
  
  public static void i(String msg){
    stream.println(msg);
  }

}
