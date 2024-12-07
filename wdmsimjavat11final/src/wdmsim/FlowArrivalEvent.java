package wdmsim;
public class FlowArrivalEvent extends Event {
	
    private Flow flow;
    public FlowArrivalEvent(Flow flow) {
        this.flow = flow;
    }
    public Flow getFlow() {
        return this.flow;
    }
    public String toString() {
        return "Arrival: "+flow.toString();
    }
}
