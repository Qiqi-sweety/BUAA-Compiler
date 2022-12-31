package utils;

import java.io.Serializable;

public class Pair<H, T> implements Serializable {
    private H head;
    private T tail;

    //Constructor
    public Pair(H head, T tail) {
        this.head = head;
        this.tail = tail;
    }

    public H getHead() {
        return head;
    }

    public void setHead(H head) {
        this.head = head;
    }
    public void setTail(T tail){
        this.tail=tail;
    }
    public T getTail() {
        return tail;
    }
}
