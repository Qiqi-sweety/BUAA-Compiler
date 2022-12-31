package generate.usage;

public class Use {
    Value val;
    Instr user;
    public Use(Value v, Instr u) {
        val = v;
        user = u;
    }

    public Instr getUser() {
        return user;
    }

    public Value getVal() {
        return val;
    }

    public Use setVal(Value val) {
        assert val != null;
        this.val = val;
        val.getUses().add(this);
        return this;
    }

    public void setUser(Instr user) {
        this.user = user;
    }


}
