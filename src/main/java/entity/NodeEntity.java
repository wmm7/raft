package entity;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class NodeEntity {

  private int nodeId;

  private int term;

  private int nodeState;

  private int logIndex;

  private int voteFor;

  private int voteCount;

  public int getNodeId() {
    return nodeId;
  }

  public void setNodeId(int nodeId) {
    this.nodeId = nodeId;
  }

  public int getTerm() {
    return term;
  }

  public void setTerm(int term) {
    this.term = term;
  }

  public int getNodeState() {
    return nodeState;
  }

  public void setNodeState(int nodeState) {
    this.nodeState = nodeState;
  }

  public int getLogIndex() {
    return logIndex;
  }

  public void setLogIndex(int logIndex) {
    this.logIndex = logIndex;
  }

  public int getVoteFor() {
    return voteFor;
  }

  public void setVoteFor(int voteFor) {
    this.voteFor = voteFor;
  }

  public int getVoteCount() {
    return voteCount;
  }

  public void setVoteCount(int voteCount) {
    this.voteCount = voteCount;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
