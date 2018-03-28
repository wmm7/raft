package node;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;

import java.io.IOException;

import entity.RequestVote;

public class Node1 {

  static ObjectMapper objectMapper = new ObjectMapper();

  private static final String EXCHANGE_NAME = "raft";

  private static final String NODE_NAME = "NODE_1";

  static Channel channel;

  static {
    try{
      ConnectionFactory factory = new ConnectionFactory();
      factory.setHost("localhost");
      Connection connection = factory.newConnection();
      channel = connection.createChannel();
      channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
      System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
    } catch (Exception e){
      e.printStackTrace();
    }
  }

  public static void start(){
    try{
      //发起投票
      channel.basicPublish(EXCHANGE_NAME, "", null, buildRequestVote().getBytes("UTF-8"));
      //接收消息
      String queueName = channel.queueDeclare().getQueue();
      channel.queueBind(queueName, EXCHANGE_NAME, "");
      Consumer consumer = new DefaultConsumer(channel) {
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope,
                                   AMQP.BasicProperties properties, byte[] body) throws IOException {
          String message = new String(body, "UTF-8");
          System.out.println(" [x] Received '" + message + "'");
        }
      };
      channel.basicConsume(queueName, true, consumer);
    } catch (Exception e){
      e.printStackTrace();
    }

  }


  private static String buildRequestVote() throws JsonProcessingException {
    RequestVote requestVote = new RequestVote();

    return objectMapper.writeValueAsString(requestVote);
  }

}
