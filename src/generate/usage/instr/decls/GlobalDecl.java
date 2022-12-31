package generate.usage.instr.decls;

import java.util.ArrayList;

public class GlobalDecl extends Decl {
    private String name;
    private final boolean isConst;
    private long value = 0; // only for int a = 1
    private final ArrayList<ArrayList<Long>> array; // this for arrays

    public GlobalDecl(boolean isConst, String name) {
        super();
        this.name = name;
        this.isConst = isConst;
        this.value = 0;
        this.array = new ArrayList<>();
    }

    public void setSize1(long size1) {
        super.setSize1(size1);
    }

    public void setSize2(long size2) {
        super.setSize2(size2);
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public void addLayer() {
        array.add(new ArrayList<>());
    }

    public ArrayList<ArrayList<Long>> getArray() {
        return array;
    }

    public boolean isConst() {
        return isConst;
    }

    public long getSize1() {
        return super.getSize1();
    }

    public long getSize2() {
        return super.getSize2();
    }

    public Long getValue() {
        return value;
    }

    public String formatString() {
        if (super.getType().equals("i32")) {
            return String.valueOf(value);
        } else if (super.getType().equals("i32arr1")) {
            StringBuilder builder =
                    new StringBuilder("[ i32 " + String.valueOf(array.get(0).get(0)));
            for (int i = 1; i < array.get(0).size(); i++) {
                builder.append(", i32 ").append(array.get(0).get(i));
            }
            builder.append("]");
            return builder.toString();
        } else {
            StringBuilder builder = new StringBuilder("[[ " + super.getSize2() + " x i32 [i32 " +
                    array.get(0).get(0));
            for (int i = 1; i < array.get(0).size(); i++) {
                builder.append(", i32 ").append(array.get(0).get(i));
            }
            builder.append("]");
            for (int i = 1; i < super.getSize1(); i++) {
                builder.append(", [").append(super.getSize2()).append(" x i32 ] [");
                builder.append("i32 ").append(array.get(i).get(0));
                for (int j = 1; j < array.get(i).size(); j++) {
                    builder.append(", i32 ").append(array.get(i).get(j));
                }
                builder.append("]");
            }
            builder.append("]");
            return builder.toString();
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "@" + name + " = " + (isConst ? "constant " : "global ") + formatType() +
                " " + formatString();
    }
}
