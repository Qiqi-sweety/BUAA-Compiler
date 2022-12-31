package utils;

public class Sym {
    private boolean isConst;
    private int dim;

    public Sym(boolean isConst, int dim) {
        this.isConst = isConst;
        this.dim = dim;
    }

    public boolean isConst() {
        return isConst;
    }

    public int getDim() {
        return dim;
    }
}
