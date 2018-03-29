package entity;

import enums.RequestStatus;

public class Message {
  private int nodeId;

  private int term;

  private RequestStatus requestStatus;

  private int logIndex;

  private boolean ackStatus;

  public int getTerm() {
    return term;
  }

  public void setTerm(int term) {
    this.term = term;
  }

  public RequestStatus getRequestStatus() {
    return requestStatus;
  }

  public void setRequestStatus(RequestStatus requestStatus) {
    this.requestStatus = requestStatus;
  }

  public int getLogIndex() {
    return logIndex;
  }

  public void setLogIndex(int logIndex) {
    this.logIndex = logIndex;
  }

  public int getNodeId() {
    return nodeId;
  }

  public void setNodeId(int nodeId) {
    this.nodeId = nodeId;
  }

  public boolean isAckStatus() {
    return ackStatus;
  }

  public void setAckStatus(boolean ackStatus) {
    this.ackStatus = ackStatus;
  }
}
