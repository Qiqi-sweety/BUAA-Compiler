package generate.usage.instr;

import generate.usage.Instr;

public class Read extends Instr {
    @Override
    public String toString() {
        return getId() + " = getint()";
    }
}
