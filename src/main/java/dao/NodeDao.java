package dao;

import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import entity.NodeEntity;
import enums.NodeState;

public class NodeDao {

  private Connection getConnection(){
    try {
      BasicDataSource dataSource = new BasicDataSource();
      dataSource.setDriverClassName("com.mysql.jdbc.Driver");
      dataSource.setUrl("jdbc:mysql://localhost:3306/raft");
      dataSource.setUsername("root");
      dataSource.setPassword("123qwe");
      return dataSource.getConnection();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new IllegalArgumentException("get Connection error");
    }
  }

  private void closeConnection(Connection connection){
    try {
      connection.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public NodeEntity queryByNodeId(int nodeId){
//    System.out.println("queryByNodeId, nodeId="+nodeId);
    String sql = "select * from node where node_id = " + nodeId;
    Connection connection = getConnection();
    try {
      Statement statement = connection.createStatement();
      ResultSet resultSet = statement.executeQuery(sql);
      NodeEntity entity = null;
      while (resultSet.next()){
        entity = new NodeEntity();
        entity.setNodeId(resultSet.getInt("node_id"));
        entity.setNodeState(resultSet.getInt("node_state"));
        entity.setLogIndex(resultSet.getInt("log_index"));
        entity.setTerm(resultSet.getInt("term"));
        entity.setVoteFor(resultSet.getInt("vote_for"));
        entity.setVoteCount(resultSet.getInt("vote_count"));
//        System.out.println("queryByNodeId, " + entity.toString());
      }
      return entity;
    } catch (Exception e){
      e.printStackTrace();
    } finally {
      closeConnection(connection);
    }
    return null;
  }

  public void insert(NodeEntity nodeEntity){
    Connection connection = getConnection();
    try{
      PreparedStatement preparedStatement =
          connection.prepareStatement("INSERT INTO node (node_id, node_state, term, log_index, vote_for, vote_count) VALUE (?,?,?,?,?,?)");
      preparedStatement.setInt(1, nodeEntity.getNodeId());
      preparedStatement.setInt(2, nodeEntity.getNodeState());
      preparedStatement.setInt(3, nodeEntity.getTerm());
      preparedStatement.setInt(4, nodeEntity.getLogIndex());
      preparedStatement.setInt(5, nodeEntity.getVoteFor());
      preparedStatement.setInt(6, nodeEntity.getVoteCount());
      preparedStatement.executeUpdate();
    }catch (Exception e){
      e.printStackTrace();
    } finally {
      closeConnection(connection);
    }
  }


  public void updateNodeState(int nodeId, NodeState nodeState) {
//    System.out.println("updateNodeState, nodeId="+nodeId+", nodeState="+nodeState);
    Connection connection = getConnection();
    try{
      PreparedStatement preparedStatement =
          connection.prepareStatement("UPDATE node set node_state = ? , modify_time = ? where node_id = ? ");
      preparedStatement.setInt(1, nodeState.getValue());
      preparedStatement.setDate(2, new java.sql.Date(new Date().getTime()));
      preparedStatement.setInt(3, nodeId);
      preparedStatement.executeUpdate();
    }catch (Exception e){
      e.printStackTrace();
    } finally {
      closeConnection(connection);
    }
  }

  public void updateVoteCount(int nodeId, int voteCount){
//    System.out.println("updateVoteCount, nodeId="+nodeId+", voteCount="+voteCount);
    Connection connection = getConnection();
    try{
      PreparedStatement preparedStatement =
          connection.prepareStatement("UPDATE node set vote_count = ? , modify_time = ? where node_id = ? ");
      preparedStatement.setInt(1, voteCount);
      preparedStatement.setDate(2, new java.sql.Date(new Date().getTime()));
      preparedStatement.setInt(3, nodeId);
      preparedStatement.executeUpdate();
    }catch (Exception e){
      e.printStackTrace();
    } finally {
      closeConnection(connection);
    }
  }

}
