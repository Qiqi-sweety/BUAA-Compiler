package mips;

public class Unsigned {
    private long value;

    public Unsigned(long value) {
        this.value = value;
    }

    public long getValue() {
        return value & 0xffffffff;
    }

    public void setValue(long value) {
        this.value = value & 0xffffffff;
    }
}
