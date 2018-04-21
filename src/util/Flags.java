package util;

public final class Flags {
  private int flag;

  public Flags() {
    this(0);
  }

  public Flags(int flags) {
    this.flag = flags;
  }

  public boolean is(int anotherFlag) {
    return (flag & anotherFlag) == anotherFlag;
  }
}
