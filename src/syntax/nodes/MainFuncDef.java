package syntax.nodes;

import java.util.ArrayList;

public class MainFuncDef extends RootNode {
    private final Block block;

    public MainFuncDef(Block block) {
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }
}
