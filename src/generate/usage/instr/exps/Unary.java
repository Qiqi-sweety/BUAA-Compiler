package generate.usage.instr.exps;

import generate.Mediate;
import generate.usage.Instr;
import generate.usage.Use;
import generate.usage.Value;

import static generate.Mediate.link;

public class Unary extends Instr {
    // super id
    private final String op;
    private Use value = null;

    public Unary(String op) {
        this.op = op;
    }

    public void setValue(Value value) {
        this.value = link(value,this);
    }

    public Value getValue() {
        return value.getVal();
    }

    public String getOp() {
        return op;
    }

    @Override
    public String toString() {
        String kind = "neg";
        return getId() + " = " + kind + " i32 " + Mediate.idOrConst(value.getVal());
    }
}
