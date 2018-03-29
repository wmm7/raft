package dao;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import entity.NodeEntity;
import enums.NodeState;

public class NodeDao {

  private static Logger LOGGER = LoggerFactory.getLogger(NodeDao.class);

  public Connection getConnection(){
    try {
      BasicDataSource dataSource = new BasicDataSource();
      dataSource.setDriverClassName("com.mysql.jdbc.Driver");
      dataSource.setUrl("jdbc:mysql://localhost:3306/raft");
      dataSource.setUsername("root");
      dataSource.setPassword("12345678");
      return dataSource.getConnection();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new IllegalArgumentException("get Connection error");
    }
  }

  public NodeEntity queryByNodeId(int nodeId){
    String sql = "select * from node where nodeId = " + nodeId;
    Connection connection = getConnection();
    try {
      Statement statement = connection.createStatement();
      ResultSet resultSet = statement.executeQuery(sql);
      LOGGER.info(resultSet.toString());
      NodeEntity entity = new NodeEntity();
      while (resultSet.next()){
        entity.setNodeId(resultSet.getInt("node_id"));
        entity.setNodeState(resultSet.getInt("node_state"));
        entity.setLogIndex(resultSet.getInt("log_index"));
        entity.setTerm(resultSet.getInt("term"));
      }
      return entity;
    } catch (Exception e){
      LOGGER.error("queryByNodeId error|t=", e);
    }
    return null;
  }

  public void insert(NodeEntity nodeEntity){

  }


  public void updateNodeState(int NodeId, NodeState nodeState) {

  }

}
