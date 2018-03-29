package enums;

public enum  NodeState {

  LEADER(1),

  FOLLOWER(2),

  CANDIDATE(3),

  ;

  private int value;

  NodeState(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }
}
