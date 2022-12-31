package mips;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class LiveInterval {


    private static class LiveRange{
        public int start,end;
        public LiveRange(int start,int end){
            this.start=start;
            this.end=end;

        }
    }
    private ArrayList<LiveRange> ranges=new ArrayList<>();
    private int assignedRegister=-1;
    public void addRange(int start,int end){
        ranges.add(new LiveRange(start, end));
        merge();
    }
    private void merge(){
        PriorityQueue<LiveRange>  ranges=new PriorityQueue<>(Comparator.comparingInt(o -> o.start));
        ArrayList<LiveRange> res=new ArrayList<>();
        ranges.addAll(this.ranges);
        LiveRange temp=ranges.poll();
        while(!ranges.isEmpty()){
            LiveRange temp2=ranges.poll();
            assert temp != null;
            if(temp2.start>temp.end){
                res.add(temp);
                temp=temp2;
            }
            else{
                temp.end=max(temp.end,temp2.end);
            }
        }
        res.add(temp);
        this.ranges=res;
    }
    public int getLastEnd(){
        return ranges.get(ranges.size()-1).end;
    }
    public int getFirstStart(){
        return ranges.get(0).start;
    }
    public void setStart(int start){
        assert start<ranges.get(0).end;
        ranges.get(0).start=start;
    }

    public int getAssignedRegister() {
        return assignedRegister;
    }

    public void setAssignedRegister(int assignedRegister) {
        this.assignedRegister = assignedRegister;
    }

    @Override
    public String toString() {
        StringBuilder res= new StringBuilder();
        res.append("LiveInterval{");
        for(LiveRange i:ranges){
           res.append(i.start).append("-").append(i.end).append(";");
        }
        res.append("}");
        return res.toString();
    }
    public int getRangeNum() {
        return ranges.size();
    }
}
