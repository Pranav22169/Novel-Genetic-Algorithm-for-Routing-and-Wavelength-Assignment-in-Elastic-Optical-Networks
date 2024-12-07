package wdmsim.rwa;

import java.util.TreeSet;
import wdmsim.Flow;
import wdmsim.LightPath;
import wdmsim.PhysicalTopology;
import wdmsim.VirtualTopology;
import wdmsim.util.Dijkstra;
import wdmsim.util.WeightedGraph;

public class HybridRWA implements RWA {

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
        // First, try to find the least loaded lightpath
        LightPath[] lps = new LightPath[1];
        lps[0] = getLeastLoadedLightpath(flow);
        if (lps[0] != null) {
            if (cp.acceptFlow(flow.getID(), lps)) {
                return;  // Successfully accepted the flow using the least loaded path
            }
        }

        // If no suitable lightpath was found, use Dijkstra to find the shortest path
        int[] nodes = Dijkstra.getShortestPath(graph, flow.getSource(), flow.getDestination());
        if (nodes.length == 0) {
            cp.blockFlow(flow.getID());
            return;  // No path found, block the flow
        }

        // Convert the path from nodes to links
        int[] links = new int[nodes.length - 1];
        for (int j = 0; j < nodes.length - 1; j++) {
            links[j] = pt.getLink(nodes[j], nodes[j + 1]).getID();
        }

        // Try to create a lightpath with available wavelengths
        int[] wvls = new int[links.length];
        for (int i = 0; i < pt.getNumWavelengths(); i++) {
            for (int j = 0; j < links.length; j++) {
                wvls[j] = i;
            }
            long id = vt.createLightpath(links, wvls);
            if (id >= 0) {
                lps[0] = vt.getLightpath(id);
                cp.acceptFlow(flow.getID(), lps);
                return;  // Successfully accepted the flow
            }
        }
        cp.blockFlow(flow.getID());  // No wavelength available, block the flow
    }

    private LightPath getLeastLoadedLightpath(Flow flow) {
        long abw_aux, abw = 0;
        LightPath lp_aux, lp = null;

        // Get available lightpaths between source and destination with enough bandwidth
        TreeSet<LightPath> lps = vt.getAvailableLightpaths(flow.getSource(), flow.getDestination(), flow.getRate());
        if (lps != null && !lps.isEmpty()) {
            while (!lps.isEmpty()) {
                lp_aux = lps.pollFirst();  // Retrieve the first lightpath in sorted order
                abw_aux = vt.getLightpathBWAvailable(lp_aux.getID());
                if (abw_aux > abw) {  // Choose the one with the most available bandwidth
                    abw = abw_aux;
                    lp = lp_aux;
                }
            }
        }
        return lp;  // Return the best available lightpath, or null if none found
    }

    public void flowDeparture(long id) {
        // Handle flow departure if necessary (no specific action needed in this example)
    }
}
