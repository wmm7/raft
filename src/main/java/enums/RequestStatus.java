package enums;

public enum  RequestStatus {
  APPEND_LOG(1),
  RQUEST_VOTE(2),
  COMMIT_LOG(3);


  private int value;

  RequestStatus(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }
}
