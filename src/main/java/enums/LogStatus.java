package enums;

public enum  LogStatus {

  APPEND(0),
  COMMIT(1);

  private int value;

  LogStatus(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }

}
