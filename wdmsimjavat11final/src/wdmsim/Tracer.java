package wdmsim;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Tracer {

    private PrintWriter trace;
    private static Tracer singletonObject;
    private boolean writeTrace;

    private Tracer() {
    	
    	writeTrace = true;
    }
    
    public static synchronized Tracer getTracerObject() {
        if (singletonObject == null) {
            singletonObject = new Tracer();
        }
        return singletonObject;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    
    
    public void setTraceFile(String filename) throws IOException {
        trace = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
    }

    
    public void toogleTraceWriting(boolean write) {
        writeTrace = write;
    }
    
    public void add(Object o) {
        try {
            if (o instanceof String) {
                if (writeTrace) {
                    trace.println((String) o);
                }
            } else if (o instanceof Event) {
                addEvent((Event) o);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void acceptFlow(Flow flow, LightPath[] lightpaths) {
        
    	String str;

        str = "flow-accepted " + "- " + flow.toTrace();
        for (int i = 0; i < lightpaths.length; i++) {
            str += " " + Long.toString(lightpaths[i].getID());
        }
        if (writeTrace) {
            trace.println(str);
        }
    }
    
    public void blockFlow(Flow flow) {
        if (writeTrace) {
            trace.println("flow-blocked " + "- " + flow.toTrace());
        }
    }
    
    public void createLightpath(LightPath lp) {
        if (writeTrace) {
            trace.println("lightpath-created " + lp.toTrace());
        }
    }
    
    public void removeLightpath(LightPath lp) {
        if (writeTrace) {
            trace.println("lightpath-removed " + lp.toTrace());
        }
    }
    
    private void addEvent(Event event)
    {
        try
        {
        	if (event instanceof FlowArrivalEvent)
        	{
                if (writeTrace)
                {
                    trace.println("flow-arrived " + Double.toString(event.getTime()) + " " + ((FlowArrivalEvent) event).getFlow().toTrace());
                }
            }
        	else if (event instanceof FlowDepartureEvent)
        	{
                if (writeTrace)
                {
                    trace.println("flow-departed " + Double.toString(event.getTime()) + " " + Long.toString(((FlowDepartureEvent) event).getID()) + " - - - - -");
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void finish()
    {
        trace.flush();
        trace.close();
        singletonObject = null;
    }
}
