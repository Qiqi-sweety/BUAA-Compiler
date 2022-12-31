package generate.usage.instr.decls;

import generate.usage.Instr;

public class Decl extends Instr {
    private String name = "";
    private int type = 0;
    private long size1 = -1;
    private long size2 = -1;

    public void setName(String name) {
        this.name = name;
    }

    public void setType(int type) {
        this.type = type;
    }
    public int getTypeInt() {
        return this.type;
    }
    public void setSize1(long size1) {
        this.size1 = size1;
    }

    public void setSize2(long size2) {
        this.size2 = size2;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getType() {
        return type == 0 ? "i32" : (type == 1 ? "i32arr1" : "i32arr2");
    }

    public long getSize1() {
        return size1;
    }

    public long getSize2() {
        return size2;
    }

    public String formatType() {
        if (type == 0) {
            return "i32";
        } else if (type == 1) {
            return "[ " + size1 + " x i32 ]";
        } else if (type == 2) {
            return "[ " + size1 + " [ " + size2 + " x i32 ]" +
                    " x i32 ]";
        } else {
            return "?????";
        }
    }
}
