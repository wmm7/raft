import com.google.inject.Guice;
import com.google.inject.Injector;

import node.NodeService;

public class RaftMainServer {

  public static void main(String[] args) {

    NodeService nodeService_1 = new NodeService(1);
    nodeService_1.receive();
    NodeService nodeService_2 = new NodeService(2);
    nodeService_2.receive();
    NodeService nodeService_3 = new NodeService(3);
    nodeService_3.receive();
    NodeService nodeService_4 = new NodeService(4);
    nodeService_4.receive();
    NodeService nodeService_5 = new NodeService(5);
    nodeService_5.receive();

    nodeService_1.startTimer(1000l);
//    nodeService_2.startTimer(2000l);
//    nodeService_3.startTimer(3000l);
//    nodeService_4.startTimer(4000l);
//    nodeService_5.startTimer(5000l);
  }


}
