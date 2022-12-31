package generate.usage;

public class BlockType {
    public static final BlockType NORMAL = new BlockType("NORMAL");
    public static final BlockType WHILEHEAD = new BlockType("WHILEHEAD");
    public static final BlockType WHILEBODY = new BlockType("WHILEBODY");

    private String type;

    public BlockType(String type) {
        this.type = type;
    }
}
