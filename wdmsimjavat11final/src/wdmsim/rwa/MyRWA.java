
package wdmsim.rwa;

import wdmsim.Flow;
import wdmsim.LightPath;
import wdmsim.PhysicalTopology;
import wdmsim.VirtualTopology;
import wdmsim.util.Dijkstra;
import wdmsim.util.WeightedGraph;

public class MyRWA implements RWA {

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

    public void flowDeparture(long id) {
    }
    
}
