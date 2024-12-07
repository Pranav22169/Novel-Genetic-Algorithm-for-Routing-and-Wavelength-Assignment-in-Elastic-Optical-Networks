package wdmsim;

import wdmsim.rwa.RWA;
import wdmsim.rwa.ControlPlaneForRWA;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ControlPlane implements ControlPlaneForRWA {

    private RWA rwa;
    private PhysicalTopology pt;
    private VirtualTopology vt;
    private Map<Flow, Path> mappedFlows;
    private Map<Long, Flow> activeFlows;
    private Tracer tr = Tracer.getTracerObject();
    private MyStatistics st = MyStatistics.getMyStatisticsObject();

    public ControlPlane(String rwaModule, PhysicalTopology pt, VirtualTopology vt) {
        Class RWAClass;

        mappedFlows = new HashMap<Flow, Path>();
        activeFlows = new HashMap<Long, Flow>();

        this.pt = pt;
        this.vt = vt;

        try {
            RWAClass = Class.forName(rwaModule);
            rwa = (RWA) RWAClass.newInstance();
            rwa.simulationInterface(pt, vt, this);
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

    public void newEvent(Event event) {

        if (event instanceof FlowArrivalEvent) {
            newFlow(((FlowArrivalEvent) event).getFlow());
            rwa.flowArrival(((FlowArrivalEvent) event).getFlow());
        } else if (event instanceof FlowDepartureEvent) {
            removeFlow(((FlowDepartureEvent) event).getID());
            rwa.flowDeparture(((FlowDepartureEvent) event).getID());
        }
    }

    public boolean acceptFlow(long id, LightPath[] lightpaths) {
        Flow flow;

        if (id < 0 || lightpaths.length < 1) {
            throw (new IllegalArgumentException());
        } else {
            if (!activeFlows.containsKey(id)) {
                return false;
            }
            flow = activeFlows.get(id);
            if (!canAddFlowToPT(flow, lightpaths)) {
                return false;
            }
            addFlowToPT(flow, lightpaths);
            mappedFlows.put(flow, new Path(lightpaths));
            tr.acceptFlow(flow, lightpaths);
            st.acceptFlow(flow, lightpaths);
            return true;
        }
    }

    public boolean blockFlow(long id) {
        Flow flow;

        if (id < 0) {
            throw (new IllegalArgumentException());
        } else {
            if (!activeFlows.containsKey(id)) {
                return false;
            }
            flow = activeFlows.get(id);
            if (mappedFlows.containsKey(flow)) {
                return false;
            }
            activeFlows.remove(id);
            tr.blockFlow(flow);
            st.blockFlow(flow);
            return true;
        }
    }

    public boolean rerouteFlow(long id, LightPath[] lightpaths) {
        Flow flow;
        Path oldPath;

        if (id < 0 || lightpaths.length < 1) {
            throw (new IllegalArgumentException());
        } else {
            if (!activeFlows.containsKey(id)) {
                return false;
            }
            flow = activeFlows.get(id);
            if (!mappedFlows.containsKey(flow)) {
                return false;
            }
            oldPath = mappedFlows.get(flow);
            removeFlowFromPT(flow, lightpaths);
            if (!canAddFlowToPT(flow, lightpaths)) {
                addFlowToPT(flow, oldPath.getLightpaths());
                return false;
            }
            addFlowToPT(flow, lightpaths);
            mappedFlows.put(flow, new Path(lightpaths));
            return true;
        }
    }

    private void newFlow(Flow flow) {
        activeFlows.put(flow.getID(), flow);
    }

    private void removeFlow(long id) {
        Flow flow;
        LightPath[] lightpaths;

        if (activeFlows.containsKey(id)) {
            flow = activeFlows.get(id);
            if (mappedFlows.containsKey(flow)) {
                lightpaths = mappedFlows.get(flow).getLightpaths();
                removeFlowFromPT(flow, lightpaths);
                mappedFlows.remove(flow);
            }
            activeFlows.remove(id);
        }
    }

    private void removeFlowFromPT(Flow flow, LightPath[] lightpaths) {
        int[] links;
        int[] wvls;

        for (int i = 0; i < lightpaths.length; i++) {
            links = lightpaths[i].getLinks();
            wvls = lightpaths[i].getWavelengths();
            for (int j = 0; j < links.length; j++) {
                pt.getLink(links[j]).removeTraffic(wvls[j], flow.getRate());
            }
            if (vt.isLightpathIdle(lightpaths[i].getID())) {
                vt.removeLightPath(lightpaths[i].getID());
            }
        }

    }

    private boolean canAddFlowToPT(Flow flow, LightPath[] lightpaths) {
        int[] links;
        int[] wvls;

        for (int i = 0; i < lightpaths.length; i++) {
            links = lightpaths[i].getLinks();
            wvls = lightpaths[i].getWavelengths();
            for (int j = 0; j < links.length; j++) {
                if (pt.getLink(links[j]).amountBWAvailable(wvls[j]) < flow.getRate()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void addFlowToPT(Flow flow, LightPath[] lightpaths) {
        int[] links;
        int[] wvls;

        for (int i = 0; i < lightpaths.length; i++) {
            links = lightpaths[i].getLinks();
            wvls = lightpaths[i].getWavelengths();
            for (int j = 0; j < links.length; j++) {
                pt.getLink(links[j]).addTraffic(wvls[j], flow.getRate());
            }
        }
    }

    public Path getPath(Flow flow) {
        return mappedFlows.get(flow);
    }

    public Map<Flow, Path> getMappedFlows() {
        return mappedFlows;
    }

    public Flow getFlow(long id) {
        return activeFlows.get(id);
    }

    public int getLightpathFlowCount(long id) {
        int num = 0;
        Path p;
        LightPath[] lps;
        ArrayList<Path> ps = new ArrayList<Path>(mappedFlows.values());
        for (int i = 0; i < ps.size(); i++) {
            p = ps.get(i);
            lps = p.getLightpaths();
            for (int j = 0; j < lps.length; j++) {
                if (lps[j].getID() == id) {
                    num++;
                    break;
                }
            }
        }
        return num;
    }
}
