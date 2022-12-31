package generate.usage;

import mips.LiveInterval;

public class TempValue extends Value{
    public TempValue(int register){
        this.setLiveInterval(new LiveInterval());
        this.getLiveInterval().setAssignedRegister(register);
    }
}
