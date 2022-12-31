package mips;

public class DivInfo {
    private int mul;
    private Unsigned shift;

    public DivInfo() {
        this.mul = 0;
        this.shift = new Unsigned(0);
    }

    public int getMul() {
        return mul;
    }

    public void setMul(int num) {
        mul = num;
    }

    public Long getShift() {
        return shift.getValue();
    }

    public void setShift(long num) {
        shift.setValue(num);
    }
}
