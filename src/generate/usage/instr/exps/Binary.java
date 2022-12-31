package generate.usage.instr.exps;

import generate.Mediate;
import generate.usage.Instr;
import generate.usage.Use;
import generate.usage.Value;

import java.util.Objects;

import static generate.Mediate.link;

public class Binary extends Instr {
    private final String op;
    private final Use left;
    private final Use right;

    public Binary(String op, Value left, Value right) {
        this.op = op;
        this.left = link(left, this);
        this.right = link(right, this);
    }

    public String getOp() {
        return op;
    }

    public Value getLeft() {
        return left.getVal();
    }

    public Value getRight() {
        return right.getVal();
    }

    @Override
    public String toString() {
        String kind;
        switch (op) {
            case "PLUS":
                kind = "add";
                break;
            case "MINU":
                kind = "sub";
                break;
            case "DIV":
                kind = "div";
                break;
            case "MULT":
                kind = "mul";
                break;
            case "MOD":
                kind = "mod";
                break;
            case "AND":
                kind = "and";
                break;
            case "OR":
                kind = "or";
                break;
            case "GRE":
                kind = "gre";
                break;
            case "LSS":
                kind = "lss";
                break;
            case "GEQ":
                kind = "geq";
                break;
            case "LEQ":
                kind = "leq";
                break;
            case "EQL":
                kind = "eql";
                break;
            case "NEQ":
                kind = "neq";
                break;
            case "SLL":
                kind = "sll";
                break;
            default:
                kind = "warning";
                break;
        }
        return getId() + " = " + kind + " i32 " + Mediate.idOrConst(left.getVal()) + ", " +
            Mediate.idOrConst(right.getVal());
    }

}