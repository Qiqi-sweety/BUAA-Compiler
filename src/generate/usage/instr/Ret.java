package generate.usage.instr;

import generate.Mediate;
import generate.usage.Instr;
import generate.usage.Use;
import generate.usage.Value;

public class Ret extends Instr {
    private final Use value;
    private final boolean isReturn;

    public Ret(Value value, boolean isReturn) {
        this.value = Mediate.link(value,this);
        this.isReturn = isReturn;
    }

    public boolean isReturn() {
        return isReturn;
    }

    public Value getValue() {
        return value.getVal();
    }

    @Override
    public String toString() {
        if (!isReturn) {
            return "ret void";
        } else {
            return "ret " + Mediate.idOrConst(value.getVal());
        }
    }
}
