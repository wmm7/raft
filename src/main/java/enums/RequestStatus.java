package enums;

public enum  RequestStatus {
  HEART_BEAT(0),
  RETURN_HEART_BEAT(1),
  APPEND_LOG(2),
  RETURN_APPEND_LOG(3),
  REQUEST_VOTE(4),
  RETURN_REQUEST_VOTE(5),
  COMMIT_LOG(6),
  RETURN_COMMIT_LOG(7),

  ;


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
