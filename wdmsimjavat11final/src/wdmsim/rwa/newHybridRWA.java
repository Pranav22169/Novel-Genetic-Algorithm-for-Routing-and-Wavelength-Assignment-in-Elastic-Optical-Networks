package wdmsim.rwa;

import wdmsim.Flow;
import wdmsim.LightPath;
import wdmsim.PhysicalTopology;
import wdmsim.VirtualTopology;
import wdmsim.util.Dijkstra;
import wdmsim.util.WeightedGraph;

import java.util.ArrayList;
import java.util.Random;

public class newHybridRWA implements RWA {

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
        LightPath[] lps = new LightPath[1];

        // Use Dijkstra to find the shortest path (base path evaluation)
        nodes = Dijkstra.getShortestPath(graph, flow.getSource(), flow.getDestination());

        if (nodes.length == 0) {
            cp.blockFlow(flow.getID());
            return;
        }

        links = new int[nodes.length - 1];
        for (int i = 0; i < nodes.length - 1; i++) {
            links[i] = pt.getLink(nodes[i], nodes[i + 1]).getID(); // This could be adapted
        }

        // Evaluate path score based on available wavelengths and congestion
        double pathScore = evaluatePathScore(flow, nodes);

        // If the path score is favorable (low congestion), try to allocate the flow
        if (pathScore < 1.0) { // Threshold could be adjusted based on experimentation
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
        }

        // If no suitable path found, block the flow
        cp.blockFlow(flow.getID());
    }

    // Evaluate path score based on path length and available resources (e.g., wavelengths)
    private double evaluatePathScore(Flow flow, int[] nodes) {
        double pathScore = 0;

        // Reward shorter paths
        pathScore += nodes.length;

        // Penalize paths with high congestion (in this case, just the number of available wavelengths)
        for (int i = 0; i < nodes.length - 1; i++) {
            // Here, we would calculate the load factor based on available wavelengths on the link
            int availableWavelengths = pt.getNumWavelengths(); // Example placeholder
            pathScore += (availableWavelengths - flow.getRate()) * 0.1; // Basic congestion handling
        }

        // Reward higher available bandwidth (fewer flows occupying the resources)
        pathScore -= flow.getRate() * 0.1; // Penalize more traffic, reward less

        return pathScore;
    }

    public void flowDeparture(long id) {
        // Clean-up flow-related resources (not implemented here)
    }
}
