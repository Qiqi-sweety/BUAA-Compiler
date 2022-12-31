package generate.usage;

public class Const extends Value {
    private final Long value;
    private final int type = 0; //i32

    public Const(Long value) {
        super();
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
