import com.google.inject.Guice;
import com.google.inject.Injector;

import org.apache.thrift.TProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;

import java.net.ServerSocket;

public class BeanServer {

  public static void main(String[] args) {
    try {
      Injector injector = Guice.createInjector(new BeanModule());
      TProcessor processor = null;
      ServerSocket socket = new ServerSocket(8082);
      TServerSocket serverTransport = new TServerSocket(socket);
      TServer.Args tServerArgs = new TServer.Args(serverTransport);
      tServerArgs.processor(processor);
      TServer server = new TSimpleServer(tServerArgs);
      server.serve();
      System.out.println("Starting the simple server");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
