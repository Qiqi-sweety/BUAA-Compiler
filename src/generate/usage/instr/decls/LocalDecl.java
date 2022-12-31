package generate.usage.instr.decls;

import generate.Mediate;
import generate.usage.Use;
import generate.usage.Value;

import java.util.ArrayList;

public class LocalDecl extends Decl {
    private Value value; // just for a = 1
    private ArrayList<ArrayList<Use>> array; // for arrays
    private boolean isConst;
    private int type;

    public LocalDecl() {
        super();
        array = new ArrayList<>();
    }

    public LocalDecl(Value value, boolean isConst, int type) {
        this.value = value;
        array = new ArrayList<>();
        this.isConst = isConst;
        this.type = type;
    }

    public void addLayer() {
        array.add(new ArrayList<>());
    }

    public Value getValue() {
        return value;
    }

    public ArrayList<ArrayList<Use>> getArray() {
        return array;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public String formatString() {
        if (super.getType().equals("i32")) {
            return Mediate.idOrConst(value);
        } else if (super.getType().equals("i32arr1")) {
            StringBuilder builder =
                    new StringBuilder("[ i32" + Mediate.idOrConst(array.get(0).get(0).getVal()));
            for (int i = 1; i < array.get(0).size(); i++) {
                builder.append(", i32 ").append(Mediate.idOrConst(array.get(0).get(i).getVal()));
            }
            builder.append("]");
            return builder.toString();
        } else {
            StringBuilder builder = new StringBuilder("[[ " + super.getSize2() + " x i32 [i32 " +
                    Mediate.idOrConst(array.get(0).get(0).getVal()));
            for (int i = 1; i < array.get(0).size(); i++) {
                builder.append(", i32 ").append(Mediate.idOrConst(array.get(0).get(i).getVal()));
            }
            builder.append("]");
            for (int i = 1; i < super.getSize1(); i++) {
                builder.append(", [").append(super.getSize2()).append(" x i32 ] [");
                builder.append("i32 ").append(Mediate.idOrConst(array.get(i).get(0).getVal()));
                for (int j = 1; j < array.get(i).size(); j++) {
                    builder.append(", i32 ").append(Mediate.idOrConst(array.get(i).get(j).getVal()));
                }
                builder.append("]");
            }
            builder.append("]");
            return builder.toString();
        }
    }

    @Override
    public String toString() {
        return getId() + " = " + super.formatType() + " " + formatString();
    }
}
