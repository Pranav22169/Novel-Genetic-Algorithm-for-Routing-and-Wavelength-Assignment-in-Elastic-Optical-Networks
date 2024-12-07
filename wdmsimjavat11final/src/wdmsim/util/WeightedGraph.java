package wdmsim.util;

public class WeightedGraph {

    private int numNodes;
    private double[][] edges;  
    
    public WeightedGraph(int n) {
        edges = new double[n][n];
        numNodes = n;
    }
    
    public WeightedGraph(WeightedGraph g) {
        numNodes = g.numNodes;
        edges = new double[numNodes][numNodes];
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                edges[i][j] = g.getWeight(i, j);
            }
        }
    }
    
    public int size() {
        return numNodes;
    }
    
    public void addEdge(int source, int target, double w) {
        edges[source][target] = w;
    }
    
    public boolean isEdge(int source, int target) {
        return edges[source][target] > 0;
    }
    
    public void removeEdge(int source, int target) {
        edges[source][target] = 0;
    }
    
    public double getWeight(int source, int target) {
        return edges[source][target];
    }
    
    public void setWeight(int source, int target, double w) {
        edges[source][target] = w;
    }
    
    public int[] neighbors(int vertex) {
        int count = 0;
        for (int i = 0; i < edges[vertex].length; i++) {
            if (edges[vertex][i] > 0) {
                count++;
            }
        }
        final int[] answer = new int[count];
        count = 0;
        for (int i = 0; i < edges[vertex].length; i++) {
            if (edges[vertex][i] > 0) {
                answer[count++] = i;
            }
        }
        return answer;
    }
    
    @Override
    public String toString() {
        String s = "";
        for (int j = 0; j < edges.length; j++) {
            s += Integer.toString(j) + ": ";
            for (int i = 0; i < edges[j].length; i++) {
                if (edges[j][i] > 0) {
                    s += Integer.toString(i) + ":" + Double.toString(edges[j][i]) + " ";
                }
            }
            s += "\n";
        }
        return s;
    }
}
