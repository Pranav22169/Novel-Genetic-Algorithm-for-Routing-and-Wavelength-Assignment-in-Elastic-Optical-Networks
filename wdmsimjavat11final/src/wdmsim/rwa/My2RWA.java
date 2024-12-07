package wdmsim.rwa;

import java.util.TreeSet;
import wdmsim.Flow;
import wdmsim.LightPath;
import wdmsim.PhysicalTopology;
import wdmsim.VirtualTopology;
import wdmsim.util.Dijkstra;
import wdmsim.util.WeightedGraph;

public class My2RWA implements RWA {

    private PhysicalTopology pt;
    private VirtualTopology vt;
    private ControlPlaneForRWA cp;
    private WeightedGraph graph;

    public void simulationInterface(PhysicalTopology pt, VirtualTopology vt, ControlPlaneForRWA cp) {
        this.pt = pt;
        this.vt = vt;
        this.cp = cp;
        this.graph = pt.getWeightedGraph();
    }

    public void flowArrival(Flow flow) {
        int[] nodes;
        int[] links;
        int[] wvls;
        long id;
        LightPath[] lps = new LightPath[1];;

        lps[0] = getLeastLoadedLightpath(flow);
        if (lps[0] instanceof LightPath) {
            if (cp.acceptFlow(flow.getID(), lps)) {
                return;
            }
        }
        nodes = Dijkstra.getShortestPath(graph, flow.getSource(), flow.getDestination());

        if (nodes.length == 0) {
            cp.blockFlow(flow.getID());
            return;
        }

        links = new int[nodes.length - 1];
        for (int j = 0; j < nodes.length - 1; j++) {
            links[j] = pt.getLink(nodes[j], nodes[j + 1]).getID();
        }

        wvls = new int[links.length];
        for (int i = 0; i < pt.getNumWavelengths(); i++) {
            // Create the wavelengths vector
            for (int j = 0; j < links.length; j++) {
                wvls[j] = i;
            }
            if ((id = vt.createLightpath(links, wvls)) >= 0) {
                lps[0] = vt.getLightpath(id);
                cp.acceptFlow(flow.getID(), lps);
                return;
            }
        }
        cp.blockFlow(flow.getID());
    }

    private LightPath getLeastLoadedLightpath(Flow flow) {
        long abw_aux, abw = 0;
        LightPath lp_aux, lp = null;

        TreeSet<LightPath> lps = vt.getAvailableLightpaths(flow.getSource(),
                flow.getDestination(), flow.getRate());
        if (lps != null && !lps.isEmpty()) {
            while (!lps.isEmpty()) {
                lp_aux = lps.pollFirst();
                abw_aux = vt.getLightpathBWAvailable(lp_aux.getID());
                if (abw_aux > abw) {
                    abw = abw_aux;
                    lp = lp_aux;
                }
            }
        }
        return lp;
    }

    public void flowDeparture(long id) {
    }
    
}
