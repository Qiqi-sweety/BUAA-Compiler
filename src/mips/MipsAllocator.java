package mips;

import generate.usage.Block;
import generate.usage.Const;
import generate.usage.Function;
import generate.usage.Instr;
import generate.usage.Phi;
import generate.usage.Program;
import generate.usage.Use;
import generate.usage.Value;
import generate.usage.instr.decls.GlobalDecl;
import generate.usage.instr.decls.InlineDecl;
import utils.Pair;

import java.io.BufferedOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;
import java.util.Stack;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class MipsAllocator {
    private static LinkedHashMap<Value, LiveInterval> V2L = new LinkedHashMap<>();
    private static PriorityQueue<LiveInterval> active =
        new PriorityQueue<>(Comparator.comparingInt(LiveInterval::getLastEnd));
    private static PriorityQueue<LiveInterval> intervals =
        new PriorityQueue<>(Comparator.comparingInt(LiveInterval::getFirstStart));

    private static void number_function(Function function) {
        int local_number = 0;
        for (Block i : function.getLinearList()) {
            i.setStart_interval(local_number);
            local_number += 2;
            for (Instr j : i.getInstructions()) {
                j.setFatherBlock(i);
                j.setAllocateNumber(local_number);
                local_number += 2;
            }
            i.setEnd_interval(local_number - 2);
        }
    }

    private static void generate_interval_block_only(Function function) {
        for (Block i : function.getLinearList()) {
            for (Instr j : i.getInstructions()) {
//                if (j instanceof GetAddress) {
//                    j.setLiveInterval(new LiveInterval());
//                    V2L.put(j, j.getLiveInterval());
//                    j.getLiveInterval().addRange(0,
//                        function.getLinearList().get(function.getLinearList().size() - 1)
//                            .getEnd_interval());
//                }
                HashSet<Block> blocks1 = new HashSet<>();
                int farthest = -1;
                for (Use k : j.getUses()) {
                    blocks1.add(k.getUser().getFatherBlock());
                    farthest = max(farthest, k.getUser().getAllocateNumber());
                }
                if (blocks1.size() == 1 && blocks1.contains(i)) {
                    j.setLiveInterval(new LiveInterval());
                    V2L.put(j, j.getLiveInterval());
                    j.getLiveInterval().addRange(j.getAllocateNumber()+1, farthest + 1);
                }

            }
        }
    }

    private static void generate_interval(Function function) {
        V2L.clear();
        LinkedHashSet<Value> live = new LinkedHashSet<>();
        ArrayList<Block> blocks = function.getLinearList();
        for (int i = blocks.size() - 1; i >= 0; i--) {
            Block block = blocks.get(i);
            live.clear();
            for (Block successor : block.getFollows()) {
                live.addAll(successor.getLiveIn());
                for (Phi phi : successor.getPhis()) {
                    if (V2L.get(phi) == null) {
                        LiveInterval interval = new LiveInterval();
                        V2L.put(phi, interval);
                        phi.setLiveInterval(interval);
                    }
                    for (Pair<Use, Block> item : phi.getOperands()) {
                        if (item.getTail() == block) {
                            if (item.getHead().getVal() instanceof Instr) {
                                Value v=item.getHead().getVal();
                                if(v instanceof InlineDecl){
                                    v=((InlineDecl) v).getValue();
                                }
                                live.add(v);
                            }
                        }
                    }
                }
            }
            for (Value value : live) {
                addRange(value,block.getStart_interval(), block.getEnd_interval() + 1);
            }
            for (int j = block.getInstructions().size() - 1; j >= 0; j--) {
                Instr it = block.getInstructions().get(j);
                if (V2L.get(block.getInstructions().get(j)) != null) {
                    V2L.get(block.getInstructions().get(j)).setStart(it.getAllocateNumber()+1);
                }
                live.remove(it);
                for (Use use : it.getOperand()) {
                    if (use.getVal() == null) {
                        continue;
                    }
                    addRange(use.getVal(),block.getStart_interval(), it.getAllocateNumber() + 1);
                    Value v=use.getVal();
                    if(v instanceof InlineDecl){
                        v=((InlineDecl) v).getValue();
                    }
                    if ((!(v instanceof Const)) &&
                        (!(v instanceof GlobalDecl) )){
                        live.add(v);
                    }
                }
            }
            for (Phi phi : block.getPhis()) {
                phi.getLiveInterval().setStart(block.getStart_interval());
                live.remove(phi);
            }
            if (block.getWhileHead()) {
                for (Value value : live) {
                    if (V2L.get(value) == null) {
                        LiveInterval interval = new LiveInterval();
                        V2L.put(value, interval);
                        value.setLiveInterval(interval);
                    }
                    V2L.get(value).addRange(block.getStart_interval(), block.getWhileEndBlock()
                        .getEnd_interval());
                }
            }
            block.addLiveIn(live);
        }

    }
    private static void addRange(Value v,int start,int end){
        if(v instanceof InlineDecl){
            v=((InlineDecl) v).getValue();
        }
        if ((!(v instanceof Const)) &&
            (!(v instanceof GlobalDecl) )) {
            if (V2L.get(v) == null) {
                LiveInterval interval = new LiveInterval();
                V2L.put(v, interval);
                v.setLiveInterval(interval);
            }
            V2L.get(v).addRange(start,end);
        }

    }
    private static void print_interval(Function function) {
        System.out.println("");
        System.out.println(function.getName() + " Block sequence");
        for (Block block : function.getLinearList()) {
            System.out.println(
                block.getLabel() + ":" + block.getStart_interval() + " " + block.getEnd_interval());
            for (Phi phi : block.getPhis()) {
                System.out.println(phi);
            }
            for (Instr instr : block.getInstructions()) {
                System.out.println(instr.getAllocateNumber() + ":" + instr);
            }
        }
        System.out.println("------------------------------------------------------------");
        for (Value value : V2L.keySet()) {
            System.out.println(value.toString() + " " + V2L.get(value).toString());
        }
    }

    private static final Stack<Integer> freeReg = new Stack<>();

    private static void expiredOldIntervals(int pos) {
        PriorityQueue<LiveInterval> newActive =
            new PriorityQueue<>(Comparator.comparingInt(LiveInterval::getLastEnd));
        while (!active.isEmpty()) {
            LiveInterval I = active.poll();
            if (I.getLastEnd() <= pos) {
                if (I.getAssignedRegister() != -1) {
                    freeReg.push(I.getAssignedRegister());
                }
            } else {
                newActive.add(I);
            }
        }
        ;
        active.clear();
        active.addAll(newActive);
    }

    private static void spillAtInterval(LiveInterval interval) {
        LiveInterval spill = active.peek();
        assert spill != null;
        if (spill.getLastEnd() > interval.getLastEnd()) {
            interval.setAssignedRegister(spill.getAssignedRegister());
            spill.setAssignedRegister(-1);
            active.poll();
            active.add(interval);
        } else {
            interval.setAssignedRegister(-1);
        }
    }

    private static void linearScanAllocate(Function function) {
        active.clear();
        intervals.clear();
        freeReg.clear();
        for (Value value : V2L.keySet()) {
            if (V2L.get(value).getRangeNum() == 0) {
                V2L.get(value).setAssignedRegister(-1);
            } else {
                intervals.add(V2L.get(value));
            }
        }
        for (int i = 8; i <= 25; i++) {
            freeReg.push(i);
        }
        while (!intervals.isEmpty()) {
            LiveInterval interval = intervals.poll();
            expiredOldIntervals(interval.getFirstStart());
            if (active.size() == 18) {
                spillAtInterval(interval);
            } else {
                int r = freeReg.pop();
                interval.setAssignedRegister(r);
                function.addUsedReg(r);
                active.add(interval);
            }
        }
    }

    private static void print_allocat(Function function) {
        for (Value v : V2L.keySet()) {
            System.out.println(v.getId() + " allocated to:" + V2L.get(v).getAssignedRegister());
        }
    }

    public static void mipsAllocate(Program p) {
        try {
            System.setOut(new PrintStream(new BufferedOutputStream(
                Files.newOutputStream(Paths.get("interval.txt"))), true));
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Function function : p.getFunctions()) {
            number_function(function);
            generate_interval(function);
//            generate_interval_block_only(function);
            print_interval(function);
            linearScanAllocate(function);
            print_allocat(function);

        }
        number_function(p.getMain());
        generate_interval(p.getMain());
//        generate_interval_block_only(p.getMain());
        print_interval(p.getMain());
        linearScanAllocate(p.getMain());
        print_allocat(p.getMain());
    }
}
