import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class client {

  public static void main(String[] args){
    //创建transport
    TTransport tTransport = new TSocket("http://localhost", 8082);
    //创建client

  }

}
