package syntax.nodes;

import syntax.stmts.Assign;
import syntax.stmts.If;
import syntax.stmts.Print;
import syntax.stmts.Return;
import syntax.stmts.Stdin;
import syntax.stmts.While;

import java.util.ArrayList;

public class Block extends Stmt {
    private final ArrayList<RootNode> items;

    public Block(ArrayList<RootNode> items) {
        this.items = items;
    }

    public boolean isErrG() {
        if (items.size() == 0) {
            return true;
        }
        return !(items.get(items.size() - 1) instanceof Return);
    }

    public ArrayList<RootNode> getItems() {
        return items;
    }

}

