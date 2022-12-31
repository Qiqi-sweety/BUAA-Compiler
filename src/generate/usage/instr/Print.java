package generate.usage.instr;

import generate.Mediate;
import generate.usage.Instr;
import generate.usage.Use;
import generate.usage.Value;

import java.util.ArrayList;

public class Print extends Instr {
    private ArrayList<Use> values = new ArrayList<>();
    private String format = "";

    public void addValue(Value value) {
        values.add(Mediate.link(value,this));
    }

    public String getFormat() {
        return format;
    }

    public ArrayList<Value> getValues() {
        ArrayList<Value> ret = new ArrayList<>();
        for (Use use : values) {
            ret.add(use.getVal());
        }
        return ret;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (Use u : values) {
            str.append(", ").append(Mediate.idOrConst(u.getVal()));
        }
        return "print(" + format + str + ")";
    }
}
