
package wdmsim.util;

import java.util.ArrayList;

public class Dijkstra {
    public static int[] dijkstra(WeightedGraph G, int s) {
        final double[] dist = new double[G.size()];
        final int[] pred = new int[G.size()];
        final boolean[] visited = new boolean[G.size()]; 

        for (int i = 0; i < dist.length; i++) {
            pred[i] = -1;
            dist[i] = Integer.MAX_VALUE;
        }
        dist[s] = 0;

        for (int i = 0; i < dist.length; i++) {
            final int next = minVertex(dist, visited);
            if (next >= 0) {
                visited[next] = true;

                final int[] n = G.neighbors(next);
                for (int j = 0; j < n.length; j++) {
                    final int v = n[j];
                    final double d = dist[next] + G.getWeight(next, v);
                    if (dist[v] > d) {
                        dist[v] = d;
                        pred[v] = next;
                    }
                }
            }
        }
        return pred;
    }

    private static int minVertex(double[] dist, boolean[] v) {
        double x = Double.MAX_VALUE;
        int y = -1;
        for (int i = 0; i < dist.length; i++) {
            if (!v[i] && dist[i] < x) {
                y = i;
                x = dist[i];
            }
        }
        return y;
    }

    public static int[] getShortestPath(WeightedGraph G, int src, int dst) {
        int x;
        int[] sp;
        ArrayList<Integer> path = new ArrayList<Integer>();

        final int[] pred = dijkstra(G, src);

        x = dst;

        while (x != src) {
            path.add(0, x);
            x = pred[x];
            // No path
            if (x == -1) {
                return new int[0];
            }
        }
        path.add(0, src);
        sp = new int[path.size()];
        for (int i = 0; i < path.size(); i++) {
            sp[i] = path.get(i);
        }
        return sp;
    }

    public static int[] getShortestPath(int[] pred, int src, int dst) {
        int x;
        int[] sp;
        ArrayList<Integer> path = new ArrayList<Integer>();

        x = dst;

        while (x != src) {
            path.add(0, x);
            x = pred[x];
            // No path
            if (x == -1) {
                return new int[0];
            }
        }
        path.add(0, src);
        sp = new int[path.size()];
        for (int i = 0; i < path.size(); i++) {
            sp[i] = path.get(i);
        }
        return sp;
    }

    public static void printPath(int[] pred, int src, int dst) {
        final ArrayList<Integer> path = new ArrayList<Integer>();
        int x = dst;
        while (x != src) {
            path.add(0, x);
            x = pred[x];
        }
        path.add(0, src);
        System.out.println(path);
    }
}
