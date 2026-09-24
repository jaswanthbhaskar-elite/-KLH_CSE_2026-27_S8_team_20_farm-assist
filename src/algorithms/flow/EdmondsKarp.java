package algorithms.flow;

import models.Fertilizer;
import models.Requirement;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Edmonds-Karp max-flow algorithm (Ford-Fulkerson with BFS for augmenting paths).
 *
 * Graph layout (node indices):
 *   0                              = source
 *   1..F                           = fertilizer nodes
 *   F+1..F+C                       = crop nodes
 *   F+C+1                          = sink
 *
 * Time complexity : O(V * E^2)
 * Space complexity: O(V^2)   (adjacency-matrix residual graph)
 */
public class EdmondsKarp {

    public static class AllocationResult {
        public final int maxFlow;
        public final List<String[]> allocations; // {fertilizer, crop, amount}
        public final int totalDemand;

        public AllocationResult(int maxFlow, List<String[]> allocations, int totalDemand) {
            this.maxFlow = maxFlow;
            this.allocations = allocations;
            this.totalDemand = totalDemand;
        }

        public int getUnmetDemand() {
            return totalDemand - maxFlow;
        }
    }

    public static AllocationResult allocate(List<Fertilizer> fertilizers, List<Requirement> requirements) {
        // Preserve insertion order, but de-duplicate.
        List<String> fertNames = new ArrayList<>();
        Set<String> seenFert = new LinkedHashSet<>();
        for (Fertilizer f : fertilizers) {
            if (seenFert.add(f.getName())) fertNames.add(f.getName());
        }

        List<String> cropNames = new ArrayList<>();
        Set<String> seenCrop = new LinkedHashSet<>();
        for (Requirement r : requirements) {
            if (seenCrop.add(r.getCropName())) cropNames.add(r.getCropName());
        }

        int F = fertNames.size();
        int C = cropNames.size();
        int source = 0;
        int sink = F + C + 1;
        int totalNodes = F + C + 2;

        int[][] capacity = new int[totalNodes][totalNodes];

        // source -> fertilizer : capacity = supply
        for (int i = 0; i < F; i++) {
            String name = fertNames.get(i);
            int supply = fertilizers.stream()
                    .filter(f -> f.getName().equals(name))
                    .mapToInt(Fertilizer::getAvailableSupply)
                    .findFirst().orElse(0);
            capacity[source][1 + i] = supply;
        }

        // fertilizer -> crop : capacity = requirement demand
        for (Requirement r : requirements) {
            int fIdx = 1 + fertNames.indexOf(r.getFertilizerName());
            int cIdx = 1 + F + cropNames.indexOf(r.getCropName());
            if (fertNames.indexOf(r.getFertilizerName()) == -1) continue; // fertilizer not in supply list
            capacity[fIdx][cIdx] += r.getDemand();
        }

        // crop -> sink : capacity = total demand for that crop (sum over its fertilizer requirements)
        int totalDemand = 0;
        for (int j = 0; j < C; j++) {
            String cropName = cropNames.get(j);
            int demandSum = requirements.stream()
                    .filter(r -> r.getCropName().equals(cropName))
                    .mapToInt(Requirement::getDemand)
                    .sum();
            capacity[1 + F + j][sink] = demandSum;
            totalDemand += demandSum;
        }

        // Keep a copy of original fertilizer->crop capacities so we can compute flow later
        // (flow = original capacity - remaining residual capacity).
        int[][] originalFertToCrop = new int[F][C];
        for (int i = 0; i < F; i++) {
            for (int j = 0; j < C; j++) {
                originalFertToCrop[i][j] = capacity[1 + i][1 + F + j];
            }
        }

        int maxFlow = 0;
        int[] parent = new int[totalNodes];

        // Repeatedly find an augmenting path via BFS and push flow along it.
        while (bfs(capacity, source, sink, parent, totalNodes)) {
            // Find the bottleneck (minimum residual capacity) along the found path.
            int pathFlow = Integer.MAX_VALUE;
            int v = sink;
            while (v != source) {
                int u = parent[v];
                pathFlow = Math.min(pathFlow, capacity[u][v]);
                v = u;
            }

            // Update residual capacities: reduce forward edges, increase reverse edges.
            v = sink;
            while (v != source) {
                int u = parent[v];
                capacity[u][v] -= pathFlow;
                capacity[v][u] += pathFlow;
                v = u;
            }

            maxFlow += pathFlow;
        }

        // Extract per-edge allocation: flow = original capacity - remaining residual capacity.
        List<String[]> allocations = new ArrayList<>();
        for (int i = 0; i < F; i++) {
            for (int j = 0; j < C; j++) {
                if (originalFertToCrop[i][j] > 0) {
                    int flow = originalFertToCrop[i][j] - capacity[1 + i][1 + F + j];
                    if (flow > 0) {
                        allocations.add(new String[]{fertNames.get(i), cropNames.get(j), String.valueOf(flow)});
                    }
                }
            }
        }

        return new AllocationResult(maxFlow, allocations, totalDemand);
    }

    /**
     * BFS over the residual graph. Finds a shortest (fewest-edges) source-to-sink
     * path where every edge still has residual capacity > 0.
     * Returns true if a path was found; parent[] records the path.
     */
    private static boolean bfs(int[][] capacity, int source, int sink, int[] parent, int totalNodes) {
        boolean[] visited = new boolean[totalNodes];
        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(source);
        visited[source] = true;
        parent[source] = -1;

        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int v = 0; v < totalNodes; v++) {
                if (!visited[v] && capacity[u][v] > 0) {
                    visited[v] = true;
                    parent[v] = u;
                    if (v == sink) return true;
                    queue.add(v);
                }
            }
        }
        return false;
    }
}