package entity;

import enums.RequestStatus;

public class RequestVote {

  private int nodeId;

  private int term;

  private RequestStatus requestStatus;

  private int logIndex;

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

}
