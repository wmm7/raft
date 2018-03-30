package node;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;

import org.apache.commons.lang3.StringUtils;

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

  Timer run_candidate_timer;

  Timer run_leader_timer;

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

      //接收消息
      String queueName = channel.queueDeclare().getQueue();
      channel.queueBind(queueName, EXCHANGE_NAME, "");
      Consumer consumer = new DefaultConsumer(channel) {
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope,
                                   AMQP.BasicProperties properties, byte[] body) throws IOException {
          String receiveMsg = new String(body, "UTF-8");
          Message receive = objectMapper.readValue(receiveMsg, Message.class);
          if(nodeId != receive.getNodeId()){
            //接收到消息校验并返回对应信息
            try{
              String returnMsg = checkAndReturnMsg(nodeId, receive);
              if(StringUtils.isNotBlank(returnMsg)){
                System.out.println(" node " + nodeId + " [x] Return = " + returnMsg + ", timestamp = " + new Date());
                channel.basicPublish(EXCHANGE_NAME, "", null, returnMsg.getBytes("UTF-8"));
              }
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
    start_timer = new Timer();
    TimerTask task = new TimerTask (){
      public void run() {
        System.out.println("start node " + nodeId + ", timestamp " + new Date());
        try{
          NodeEntity nodeEntity = nodeDao.queryByNodeId(nodeId);
          //本地无数据则初始化并发布投票
          if(nodeEntity == null){
            nodeEntity = buildNodeEntity(nodeId, Constant.START_TERM, Constant.START_LOG_INDEX, NodeState.CANDIDATE, nodeId, 1);
            nodeDao.insert(nodeEntity);
            runCandidateTimer(0, nodeEntity);
          }
          if(NodeState.FOLLOWER.getValue() == nodeEntity.getNodeState()){
            nodeDao.updateNodeState(nodeId, NodeState.CANDIDATE);
            runCandidateTimer(1000l, nodeEntity);
          }
          if(NodeState.CANDIDATE.getValue() == nodeEntity.getNodeState()){
            nodeDao.updateVoteCount(nodeId, 1);
            runCandidateTimer(0, nodeEntity);
          }
          runLeaderTime(0, 1000l);
        } catch (Exception e){
          e.printStackTrace();
        }
      }
    };
    start_timer.schedule(task, delay);
  }

  private void runCandidateTimer(long delay, final NodeEntity nodeEntity){
    run_candidate_timer = new Timer();
    TimerTask task = new TimerTask (){
      public void run() {
        try{
          String requestVote = requestMsg(nodeId, nodeEntity, RequestStatus.REQUEST_VOTE);
          System.out.println("node " + nodeId + " request vote " + requestVote + ", timestamp " + new Date());
          channel.basicPublish(EXCHANGE_NAME, "", null, requestVote.getBytes("UTF-8"));
        }catch (Exception e){
          e.printStackTrace();
        }
      }
    };
    run_candidate_timer.schedule(task, delay);
  }

  private void runLeaderTime(long delay, long period){
    run_leader_timer = new Timer();
    TimerTask task = new TimerTask (){
      public void run() {
        //TODO
      }
    };
    run_leader_timer.schedule(task, delay, period);
  }

  private String checkAndReturnMsg(int nodeId, Message receive) throws Exception {
    NodeEntity nodeEntity = nodeDao.queryByNodeId(nodeId);
    switch (receive.getRequestStatus()){
      case REQUEST_VOTE:
        System.out.println(" node " + nodeId + " [x] Received = " + receive + ", timestamp = " + new Date());
        if(nodeEntity == null){
          nodeEntity = buildNodeEntity(nodeId, receive.getTerm(), receive.getLogIndex(), NodeState.FOLLOWER, receive.getNodeId(), 0);
          nodeDao.insert(nodeEntity);
          return returnMsg(nodeId, nodeEntity, RequestStatus.RETURN_REQUEST_VOTE, true);
        }
        if(checkVote(nodeEntity, receive)){
          return returnMsg(nodeId, nodeEntity, RequestStatus.RETURN_REQUEST_VOTE, true);
        }
        return returnMsg(nodeId, nodeEntity, RequestStatus.RETURN_REQUEST_VOTE, false);
      case HEART_BEAT:
        System.out.println(" node " + nodeId + " [x] Received = " + receive + ", timestamp = " + new Date());
        if(NodeState.FOLLOWER.getValue() != nodeEntity.getNodeState()){
          nodeDao.updateNodeState(nodeId, NodeState.FOLLOWER);
          if(run_candidate_timer != null){
            run_candidate_timer.cancel();
          }
          if(run_leader_timer != null){
            run_leader_timer.cancel();
          }
        }
        return returnMsg(nodeId, nodeEntity, RequestStatus.RETURN_HEART_BEAT, true);
      case APPEND_LOG:
      case COMMIT_LOG:
      case RETURN_REQUEST_VOTE:
        if(receive.isAckStatus()
            && NodeState.CANDIDATE.getValue() == nodeEntity.getNodeState()){
          System.out.println(" node " + nodeId + " [x] Received = " + receive + ", timestamp = " + new Date());
          nodeDao.updateVoteCount(nodeId, nodeEntity.getVoteCount()+1);
          if(nodeEntity.getVoteCount()+1 >= Constant.NODE_COUNT_LEADER){
            nodeDao.updateNodeState(nodeId, NodeState.LEADER);
            runLeaderTime(0, 10000l);
          }
          return null;
        }
        default:
          return null;
    }
  }

  private boolean checkVote(NodeEntity nodeEntity, Message receive){
    if(NodeState.FOLLOWER.getValue() != nodeEntity.getNodeState()){
      return false;
    }
    if(nodeEntity.getTerm() > receive.getTerm()){
      return false;
    }
    if(nodeEntity.getLogIndex() > receive.getLogIndex()){
      return false;
    }
    return true;
  }

  private String returnMsg(int nodeId, NodeEntity nodeEntity, RequestStatus requestStatus, boolean status) throws Exception {
    Message message = new Message();
    message.setLogIndex(nodeEntity.getLogIndex());
    message.setNodeId(nodeId);
    message.setTerm(nodeEntity.getTerm());
    message.setRequestStatus(requestStatus);
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
