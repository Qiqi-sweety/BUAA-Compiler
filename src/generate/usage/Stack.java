package generate.usage;

public class Stack {
    private boolean isAssigned;
    private boolean isInStack;
    private int no;
    private int offset;

    public Stack() {
        isAssigned = false;
        isInStack = false;
        no = 0;
        offset = 0;
    }

    public boolean isAssigned() {
        return isAssigned;
    }

    public void setAssigned(boolean assigned) {
        isAssigned = assigned;
    }

    public boolean isInStack() {
        return isInStack;
    }

    public void setInStack(boolean inStack) {
        isInStack = inStack;
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
