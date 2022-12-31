package generate.usage.instr.pointers;

import generate.usage.Instr;

public class CallPointer extends Instr {
    private IdentPointer identPointer;

    public CallPointer(IdentPointer identPointer) {
        this.identPointer = identPointer;
    }

    public IdentPointer getIdentPointer() {
        return identPointer;
    }
}

// private ArrayList<IdentPointer> pointers = new ArrayList<>();
