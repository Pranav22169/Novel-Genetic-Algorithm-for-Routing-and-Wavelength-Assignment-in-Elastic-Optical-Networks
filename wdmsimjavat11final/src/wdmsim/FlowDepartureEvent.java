package wdmsim;
public class FlowDepartureEvent extends Event{

    private long id;
    public FlowDepartureEvent(long id) {
        this.id = id;
    }
    public long getID() {
        return this.id;
    }
    public String toString() {
        return "Departure: "+Long.toString(id);
    }
}
