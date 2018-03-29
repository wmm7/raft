package node;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import dao.NodeDao;
import entity.Message;
import entity.NodeEntity;
import enums.NodeState;
import enums.RequestStatus;
import util.Constant;

public class NodeService {

  private static ObjectMapper objectMapper = new ObjectMapper();

  private static final String EXCHANGE_NAME = "raft";

  private NodeDao nodeDao = new NodeDao();

  private Channel channel;

  private int nodeId;

  Timer start_timer;

  Timer run_timer;

  public NodeService(int nodeId) {
    this.nodeId = nodeId;
  }

  public void receive(){
    try{
      ConnectionFactory factory = new ConnectionFactory();
      factory.setHost("localhost");
      Connection connection = factory.newConnection();
      channel = connection.createChannel();
      channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
      System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

      //接收消息
      String queueName = channel.queueDeclare().getQueue();
      channel.queueBind(queueName, EXCHANGE_NAME, "");
      Consumer consumer = new DefaultConsumer(channel) {
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope,
                                   AMQP.BasicProperties properties, byte[] body) throws IOException {
          String message = new String(body, "UTF-8");
          System.out.println(" [x] Received = " + message + ", timestamp = " + new Date());
          Message receive = objectMapper.readValue(message, Message.class);
          if(nodeId != receive.getNodeId()){
            //接收到消息校验并返回对应信息
            try{
              String returnMsg = checkAndReturnMsg(nodeId, receive);
              System.out.println(" [x] Return = " + returnMsg + ", timestamp = " + new Date());
              channel.basicPublish(EXCHANGE_NAME, "", null, returnMsg.getBytes("UTF-8"));
            } catch (Exception e){
              e.printStackTrace();
            }
          }
        }
      };
      channel.basicConsume(queueName, true, consumer);
    } catch (Exception e){
      e.printStackTrace();
    }
  }

  public void startTimer(long delay){
    final int final_nodeId = this.nodeId;
    start_timer = new Timer();
    TimerTask task = new TimerTask (){
      public void run() {
        System.out.println("start node " + nodeId);
        start(final_nodeId);
      }
    };
    start_timer.schedule(task, delay);
  }

  public void runTimer(long delay, long period){
    run_timer = new Timer();
    TimerTask task = new TimerTask (){
      public void run() {
        //TODO
      }
    };
    run_timer.schedule(task, delay, period);
  }

  private void start(int nodeId){
    try{
      NodeEntity nodeEntity = nodeDao.queryByNodeId(nodeId);
      //本地无数据则初始化并发布投票
      if(nodeEntity == null){
        nodeEntity = buildNodeEntity(nodeId, Constant.START_TERM, Constant.START_LOG_INDEX, NodeState.CANDIDATE, nodeId, 1);
        nodeDao.insert(nodeEntity);
        String requestVote = requestMsg(nodeId, nodeEntity, RequestStatus.RQUEST_VOTE);
        System.out.println("node " + nodeId + " request vote " + requestVote + ", timestamp" + new Date());
        channel.basicPublish(EXCHANGE_NAME, "", null, requestVote.getBytes("UTF-8"));
      }
    } catch (Exception e){
      e.printStackTrace();
    }
  }

  private String checkAndReturnMsg(int nodeId, Message receive) throws Exception {
    Message message = new Message();
    NodeEntity nodeEntity = nodeDao.queryByNodeId(nodeId);
    switch (receive.getRequestStatus()){
      case RQUEST_VOTE:
        if(nodeEntity == null){
          nodeEntity = buildNodeEntity(nodeId, receive.getTerm(), receive.getLogIndex(), NodeState.FOLLOWER, receive.getNodeId(), 0);
          nodeDao.insert(nodeEntity);
        } else{
          if(!checkRequestVote(nodeEntity, message)){
            return returnMsg(nodeId, nodeEntity, RequestStatus.RETURN_RQUEST_VOTE, false);
          }
        }
        return returnMsg(nodeId, nodeEntity, RequestStatus.RETURN_RQUEST_VOTE, true);
      case HEART_BEAT:
        if(NodeState.FOLLOWER.getValue() != nodeEntity.getNodeState()){
          nodeDao.updateNodeState(nodeId, NodeState.FOLLOWER);
        }
        return returnMsg(nodeId, nodeEntity, RequestStatus.RETURN_HEART_BEAT, true);
      case APPEND_LOG:
      case COMMIT_LOG:
      case RETURN_RQUEST_VOTE:
        if(message.isAckStatus()
            && NodeState.CANDIDATE.getValue() == nodeEntity.getNodeState()){
          if(nodeEntity.getVoteCount()+1 >= Constant.NODE_COUNT_LEADER){
            nodeDao.updateNodeState(nodeId, NodeState.LEADER);
            runTimer(0, 10000l);
          }
        }
    }
    return objectMapper.writeValueAsString(message);
  }

  private boolean checkRequestVote(NodeEntity nodeEntity, Message message){
    if(NodeState.FOLLOWER.getValue() != nodeEntity.getNodeState()){
      return false;
    }
    if(nodeEntity.getTerm() >= message.getTerm()){
      return false;
    }
    if(nodeEntity.getLogIndex() >= message.getLogIndex()){
      return false;
    }
    return true;
  }

  private String returnMsg(int nodeId, NodeEntity nodeEntity, RequestStatus requestStatus, boolean status) throws Exception {
    Message message = new Message();
    message.setLogIndex(nodeEntity.getLogIndex());
    message.setNodeId(nodeId);
    message.setTerm(nodeEntity.getTerm());
    message.setRequestStatus(RequestStatus.RETURN_HEART_BEAT);
    message.setAckStatus(status);
    return objectMapper.writeValueAsString(message);
  }

  private String requestMsg(int nodeId, NodeEntity nodeEntity, RequestStatus requestStatus) throws Exception {
    Message message = new Message();
    message.setLogIndex(nodeEntity.getLogIndex());
    message.setNodeId(nodeId);
    message.setTerm(nodeEntity.getTerm());
    message.setRequestStatus(requestStatus);
    return objectMapper.writeValueAsString(message);
  }

  private NodeEntity buildNodeEntity(int nodeId, int term, int logIndex, NodeState nodeState, int voteFor, int voteCount){
    NodeEntity nodeEntity = new NodeEntity();
    nodeEntity.setTerm(term);
    nodeEntity.setLogIndex(logIndex);
    nodeEntity.setNodeState(nodeState.getValue());
    nodeEntity.setNodeId(nodeId);
    nodeEntity.setVoteFor(voteFor);
    nodeEntity.setVoteCount(voteCount);
    return nodeEntity;
  }


}
