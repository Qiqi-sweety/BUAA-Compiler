package generate.usage.instr.exps;

import generate.Mediate;
import generate.usage.Function;
import generate.usage.Instr;
import generate.usage.Use;
import generate.usage.Value;

import java.util.ArrayList;

import static generate.Mediate.link;

public class Call extends Instr {
    private final Function target;
    private final ArrayList<Use> args;

    public Call(Function target) {
        this.target = target;
        this.args = new ArrayList<>();
    }

    public void addArg(Value arg) {
        this.args.add(link(arg,this));
    }

    public Function getTarget() {
        return target;
    }

    public int getParamSize() {
        return args.size();
    }

    public ArrayList<Value> getArgs() {
        ArrayList<Value> ret = new ArrayList<>();
        for (Use use : args) {
            ret.add(use.getVal());
        }
        return ret;
    }

    public boolean isReturn() {
        return !target.getReturnType().equals("void");
    }

    public String printParas() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < args.size(); i++) {
            str.append("i32 ").append(Mediate.idOrConst(args.get(i).getVal()));
            if (i != args.size() - 1) {
                str.append(", ");
            }
        }
        return str.toString();
    }

    @Override
    public String toString() {
        return getId() + " = call " + target.getReturnType() + " @" + target.getName() + "(" +
                printParas() + ")";
    }
}
