package wdmsim;

import java.util.PriorityQueue;
import java.util.Comparator;
public class EventScheduler {

    private PriorityQueue<Event> eventQueue;
    private static class EventSort implements Comparator<Event> {
        @Override
        public int compare(Event o1, Event o2) {
            if (o1.getTime() < o2.getTime()) {
                return -1;
            }
            if (o1.getTime() > o2.getTime()) {
                return 1;
            }
            return 0;
        }
    }
    public EventScheduler() {
        EventSort eventSort = new EventSort();
        eventQueue = new PriorityQueue<Event>(100, eventSort);
    }
    public boolean addEvent(Event event) {
        return eventQueue.add(event);
    }
    public Event popEvent() {
        return eventQueue.poll();
    }
    public int numEvents() {
        return eventQueue.size();
    }
    
}
