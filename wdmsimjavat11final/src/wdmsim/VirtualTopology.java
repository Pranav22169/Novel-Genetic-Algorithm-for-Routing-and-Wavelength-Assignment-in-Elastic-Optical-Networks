package wdmsim;

import wdmsim.util.WeightedGraph;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import org.w3c.dom.*;

public class VirtualTopology {

    private long nextLightpathID = 0;
    private TreeSet<LightPath>[][] adjMatrix;
    private int adjMatrixSize;
    private Map<Long, LightPath> lightPaths;
    private PhysicalTopology pt;
    private Tracer tr = Tracer.getTracerObject();
    private MyStatistics st = MyStatistics.getMyStatisticsObject();
    
    private static class LightPathSort implements Comparator<LightPath> {

    	@Override
        public int compare(LightPath lp1, LightPath lp2) {
            if (lp1.getID() < lp2.getID()) {
                return -1;
            }
            if (lp1.getID() > lp2.getID()) {
                return 1;
            }
            return 0;
        }
    }
    
    @SuppressWarnings("unchecked")
    public VirtualTopology(Element xml, PhysicalTopology pt) {
        int nodes, lightpaths;

        lightPaths = new HashMap<Long, LightPath>();

        try {
            this.pt = pt;
            if (Simulator.verbose) {
                System.out.println(xml.getAttribute("name"));
            }

            adjMatrixSize = nodes = pt.getNumNodes();

            adjMatrix = new TreeSet[nodes][nodes];
            for (int i = 0; i < nodes; i++) {
                for (int j = 0; j < nodes; j++) {
                    if (i != j) {
                        adjMatrix[i][j] = new TreeSet<LightPath>(new LightPathSort());
                    }
                }
            }
            NodeList lightpathlist = xml.getElementsByTagName("lightpath");
            lightpaths = lightpathlist.getLength();
            if (Simulator.verbose) {
                System.out.println(Integer.toString(lightpaths) + " lightpath(s)");
            }
            if (lightpaths > 0) {
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    public long createLightpath(int[] links, int[] wavelengths) {
        LightPath lp;
        int src, dst;
        long id;
        if (links.length < 1 || wavelengths.length != links.length) {
            throw (new IllegalArgumentException());
        } else {
            if (!canCreateLightpath(links, wavelengths)) {
                return -1;
            }
            createLightpathInPT(links, wavelengths);
            src = pt.getLink(links[0]).getSource();
            dst = pt.getLink(links[links.length - 1]).getDestination();
            id = this.nextLightpathID;
            lp = new LightPath(id, src, dst, links, wavelengths);

            adjMatrix[src][dst].add(lp);
            lightPaths.put(nextLightpathID, lp);
            tr.createLightpath(lp);
            this.nextLightpathID++;

            return id;
        }
    }
    
    public boolean removeLightPath(long id) {
        int src, dst;
        LightPath lp;

        if (id < 0) {
            throw (new IllegalArgumentException());
        } else {
            if (!lightPaths.containsKey(id)) {
                return false;
            }
            lp = lightPaths.get(id);
            removeLightpathFromPT(lp.getLinks(), lp.getWavelengths());
            src = lp.getSource();
            dst = lp.getDestination();

            lightPaths.remove(id);
            adjMatrix[src][dst].remove(lp);
            tr.removeLightpath(lp);

            return true;
        }
    }
    
    public boolean rerouteLightPath(long id, int[] links, int[] wavelengths) {
        int src, dst;
        LightPath old, lp;
        if (links.length < 1 || wavelengths.length != links.length) {
            throw (new IllegalArgumentException());
        } else {
            if (!lightPaths.containsKey(id)) {
                return false;
            }
            old = lightPaths.get(id);
            removeLightpathFromPT(old.getLinks(), old.getWavelengths());
            if (!canCreateLightpath(links, wavelengths)) {
                createLightpathInPT(old.getLinks(), old.getWavelengths());
                return false;
            }
            createLightpathInPT(links, wavelengths);
            src = pt.getLink(links[0]).getSource();
            dst = pt.getLink(links[links.length - 1]).getDestination();
            adjMatrix[src][dst].remove(old);
            lp = new LightPath(id, src, dst, links, wavelengths);
            adjMatrix[src][dst].add(lp);
            lightPaths.put(id, lp);
            return true;
        }
    }
    
    public boolean isLightpathAvailable(int src, int dst, int bw) {
        TreeSet<LightPath> lps = getLightpaths(src, dst);

        for (LightPath lp : lps) {
            if (getLightpathBWAvailable(lp.getID()) >= bw) {
                return true;
            }
        }
        return false;
    }
    
    public TreeSet<LightPath> getAvailableLightpaths(int src, int dst, int bw) {
        TreeSet<LightPath> lps = getLightpaths(src, dst);

        if (lps != null && !lps.isEmpty()) {
            Iterator<LightPath> it = lps.iterator();

            while (it.hasNext()) {
                if (getLightpathBWAvailable(it.next().getID()) < bw) {
                    it.remove();
                }
            }
            return lps;
        } else {
            return null;
        }
    }
    
    public int getLightpathBWAvailable(long id) {
        int aux, bw = Integer.MAX_VALUE;

        int[] links, wvls;
        links = getLightpath(id).getLinks();
        wvls = getLightpath(id).getWavelengths();
        for (int i = 0; i < links.length; i++) {
            aux = pt.getLink(links[i]).amountBWAvailable(wvls[i]);
            if (aux < bw) {
                bw = aux;
            }
        }
        return bw;
    }
    
    public boolean isLightpathIdle(long id) {
        int[] links, wvls;
        links = getLightpath(id).getLinks();
        wvls = getLightpath(id).getWavelengths();
        return pt.getLink(links[0]).getBandwidth() - pt.getLink(links[0]).amountBWAvailable(wvls[0]) == 0;
    }
    
    public boolean isLightpathFull(long id) {
        int[] links, wvls;
        links = getLightpath(id).getLinks();
        wvls = getLightpath(id).getWavelengths();
        return pt.getLink(links[0]).amountBWAvailable(wvls[0]) == 0;
    }
    
    public LightPath getLightpath(long id) {
        if (id < 0) {
            throw (new IllegalArgumentException());
        } else {
            if (lightPaths.containsKey(id)) {
                return lightPaths.get(id);
            } else {
                return null;
            }
        }
    }
    
    public TreeSet<LightPath> getLightpaths(int src, int dst) {
        return new TreeSet<LightPath>(adjMatrix[src][dst]);
    }
    
    public TreeSet<LightPath>[][] getAdjMatrix() {
        return adjMatrix;
    }
    
    public boolean hasLightpath(int src, int dst) {
        if (adjMatrix[src][dst] != null) {
            if (!adjMatrix[src][dst].isEmpty()) {
                return true;
            }
        }
        return false;
    }
    
    public boolean canCreateLightpath(int[] links, int[] wavelengths) {
        int wvl1, wvl2, d, src, dst;
        wvl1 = wavelengths[0];
        for (int i = 1; i < wavelengths.length; i++) {
            wvl2 = wavelengths[i];
            if (wvl1 != wvl2) {
                d = Math.max(wvl1, wvl2) - Math.min(wvl1, wvl2);
                src = pt.getLink(links[i]).getSource();
                if (!pt.getNode(src).hasFreeWvlConverters() || (d > pt.getNode(src).getWvlConversionRange())) {
                    return false;
                }
                if (i < wavelengths.length - 1) {
                    dst = pt.getLink(links[i]).getDestination();
                    if (!pt.getNode(dst).hasFreeWvlConverters() || (d > pt.getNode(dst).getWvlConversionRange())) {
                        return false;
                    }
                }
                wvl1 = wvl2;
            }
        }
        if (!pt.getNode(pt.getLink(links[0]).getSource()).hasFreeGroomingInputPort()) {
            return false;
        }
        if (!pt.getNode(pt.getLink(links[links.length - 1]).getDestination()).hasFreeGroomingOutputPort()) {
            return false;
        }
        for (int i = 0; i < links.length; i++) {
            if (!pt.getLink(links[i]).isWLAvailable(wavelengths[i])) {
                return false;
            }
        }
        return true;
    }
    
    private void createLightpathInPT(int[] links, int[] wavelengths) {
        int wvl1, wvl2;
        wvl1 = wavelengths[0];
        for (int i = 1; i < wavelengths.length; i++) {
            wvl2 = wavelengths[i];
            if (wvl1 != wvl2) {
                pt.getNode(pt.getLink(links[i]).getSource()).reserveWvlConverter();
                if (i < wavelengths.length - 1) {
                    pt.getNode(pt.getLink(links[i]).getDestination()).reserveWvlConverter();
                }
                wvl1 = wvl2;
            }
        }
        pt.getNode(pt.getLink(links[0]).getSource()).reserveGroomingInputPort();
        pt.getNode(pt.getLink(links[links.length - 1]).getDestination()).reserveGroomingOutputPort();
        for (int i = 0; i < links.length; i++) {
            pt.getLink(links[i]).reserveWavelength(wavelengths[i]);
        }
    }
    
    private void removeLightpathFromPT(int[] links, int[] wavelengths) {
        int wvl1, wvl2;
        wvl1 = wavelengths[0];
        for (int i = 1; i < wavelengths.length; i++) {
            wvl2 = wavelengths[i];
            if (wvl1 != wvl2) {
                pt.getNode(pt.getLink(links[i]).getSource()).releaseWvlConverter();
                if (i < wavelengths.length - 1) {
                    pt.getNode(pt.getLink(links[i]).getDestination()).releaseWvlConverter();
                }
                wvl1 = wvl2;
            }
        }
        pt.getNode(pt.getLink(links[0]).getSource()).releaseGroomingInputPort();
        pt.getNode(pt.getLink(links[links.length - 1]).getDestination()).releaseGroomingOutputPort();
        for (int i = 0; i < links.length; i++) {
            pt.getLink(links[i]).releaseWavelength(wavelengths[i]);
        }
    }
    
    public int hopCount(LightPath lp) {
        return lp.getLinks().length;
    }
    
    public int usedConverters(LightPath lp) {
        int[] wvls = lp.getWavelengths();
        int numConv = 0;
        int wvl = wvls[0];
        
        for (int i = 1; i < wvls.length; i++) {
            if (wvl != wvls[i]) {
                numConv++;
                wvl = wvls[i];
            }
        }
        return numConv;
    }

    public WeightedGraph getWeightedGraph(int wvl, int bw) {
        WDMLink link;
        int nodes = pt.getNumNodes();
        WeightedGraph g = new WeightedGraph(nodes);
        for (int i = 0; i < nodes; i++) {
            for (int j = 0; j < nodes; j++) {
                if (pt.hasLink(i, j)) {
                    link = pt.getLink(i, j);
                    if (link.amountBWAvailable(wvl) >= bw) {
                        g.addEdge(i, j, link.getWeight());
                    }
                }
            }
        }
        return g;
    }
    
    public WeightedGraph getLightpathsGraph(int bw) {
        int nodes = pt.getNumNodes();
        WeightedGraph g = new WeightedGraph(nodes);
        for (int i = 0; i < nodes; i++) {
            for (int j = 0; j < nodes; j++) {
                if (i != j) {
                    if (getAvailableLightpaths(i, j, bw) != null) {
                        g.addEdge(i, j, 1);
                    }
                }
            }
        }
        return g;
    }
    
    @Override
    public String toString() {
        String vtopo = "";
        for (int i = 0; i < adjMatrixSize; i++) {
            for (int j = 0; j < adjMatrixSize; j++) {
                if (adjMatrix[i][j] != null) {
                    if (!adjMatrix[i][j].isEmpty()) {
                        vtopo += adjMatrix[i][j].toString() + "\n\n";
                    }
                }
            }
        }
        return vtopo;
    }
}