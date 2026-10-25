import java.util.*;

public final class Algorithms {

    
    static class Edge {
        int u, v, id;
        long weight;
        Edge(int u, int v, long weight, int id) {
            this.u = u; this.v = v; this.weight = weight; this.id = id;
        }
    }

    private static int find(int[] parent, int i) {
        int root = i;
        while (root != parent[root]) {
            root = parent[root];
        }
        int curr = i;
        while (curr != root) {
            int nxt = parent[curr];
            parent[curr] = root;
            curr = nxt;
        }
        return root;
    }

    /** DSU with path compression and union by size/rank; return states. */
    static Map<String, Object> dsu(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] operations = Input.operations(input);

        int[] parent = new int[n];
        int[] size = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }

        int components = n;
        int maxSize = n > 0 ? 1 : 0;
        List<int[]> states = new ArrayList<>();

        for (int[] op : operations) {
            if (n > 0) {
                int rootU = find(parent, op[0]);
                int rootV = find(parent, op[1]);
                if (rootU != rootV) {
                    if (size[rootU] < size[rootV]) {
                        int temp = rootU; rootU = rootV; rootV = temp;
                    }
                    parent[rootV] = rootU;
                    size[rootU] += size[rootV];
                    if (size[rootU] > maxSize) {
                        maxSize = size[rootU];
                    }
                    components--;
                }
                states.add(new int[]{components, maxSize});
            }
        }

        return Map.<String, Object>of("states", states);
    }

    /** Separate Kruskal implementation; return cost, edge_ids, components. */
    static Map<String, Object> kruskal(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        List<Edge> edgeList = new ArrayList<>();
        for (int i = 0; i < edges.length; i++) {
            edgeList.add(new Edge(edges[i][0], edges[i][1], edges[i][2], i));
        }
        edgeList.sort(Comparator.comparingLong(e -> e.weight));

        int[] parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;

        long cost = 0;
        int components = n;
        List<Integer> edgeIds = new ArrayList<>();

        for (Edge e : edgeList) {
            int rootU = find(parent, e.u);
            int rootV = find(parent, e.v);
            if (rootU != rootV) {
                parent[rootV] = rootU;
                cost += e.weight;
                edgeIds.add(e.id);
                components--;
            }
        }

        return Map.<String, Object>of("cost", cost, "edge_ids", edgeIds, "components", components);
    }

    /** Separate Prim implementation; restart for every unvisited component. */
    static Map<String, Object> prim(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        List<List<Edge>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (int i = 0; i < edges.length; i++) {
            int u = edges[i][0], v = edges[i][1];
            long w = edges[i][2];
            adj.get(u).add(new Edge(u, v, w, i));
            adj.get(v).add(new Edge(v, u, w, i));
        }

        boolean[] visited = new boolean[n];
        long cost = 0;
        int components = 0;
        List<Integer> edgeIds = new ArrayList<>();

        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingLong(e -> e.weight));

        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                components++;
                visited[i] = true;
                pq.addAll(adj.get(i));

                while (!pq.isEmpty()) {
                    Edge e = pq.poll();
                    if (!visited[e.v]) {
                        visited[e.v] = true;
                        cost += e.weight;
                        edgeIds.add(e.id);
                        pq.addAll(adj.get(e.v));
                    }
                }
            }
        }

        return Map.<String, Object>of("cost", cost, "edge_ids", edgeIds, "components", components);
    }

    /** Kosaraju's two DFS passes; return component and exact condensation. */
    static Map<String, Object> scc(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        List<List<Integer>> adj = new ArrayList<>(n);
        List<List<Integer>> revAdj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
            revAdj.add(new ArrayList<>());
        }
        for (int[] e : edges) {
            adj.get(e[0]).add(e[1]);
            revAdj.get(e[1]).add(e[0]);
        }

        List<Integer> order = new ArrayList<>();
        boolean[] visited = new boolean[n];
        for (int i = 0; i < n; i++) {
            if (!visited[i]) dfs1(i, adj, visited, order);
        }

        Collections.reverse(order);
        Arrays.fill(visited, false);
        int[] component = new int[n];
        
        for (int u : order) {
            if (!visited[u]) {
                dfs2(u, u, revAdj, visited, component);
            }
        }

        Set<String> uniqueEdges = new HashSet<>();
        List<int[]> condensation = new ArrayList<>();
        for (int[] e : edges) {
            int cu = component[e[0]];
            int cv = component[e[1]];
            if (cu != cv) {
                String key = cu + "," + cv;
                if (uniqueEdges.add(key)) {
                    condensation.add(new int[]{cu, cv});
                }
            }
        }

        List<Integer> compList = new ArrayList<>();
        for (int c : component) compList.add(c);

        return Map.<String, Object>of("component", compList, "condensation", condensation);
    }

    private static void dfs1(int start, List<List<Integer>> adj, boolean[] visited, List<Integer> order) {
    Deque<int[]> stack = new ArrayDeque<>(); // {vertex, next_child_index}
    stack.push(new int[]{start, 0});
    visited[start] = true;
    while (!stack.isEmpty()) {
        int[] top = stack.peek();
        int u = top[0];
        if (top[1] < adj.get(u).size()) {
            int v = adj.get(u).get(top[1]++);
            if (!visited[v]) {
                visited[v] = true;
                stack.push(new int[]{v, 0});
            }
        } else {
            order.add(u);
            stack.pop();
        }
    }
    }

    private static void dfs2(int start, int root, List<List<Integer>> revAdj, boolean[] visited, int[] component) {
    Deque<Integer> stack = new ArrayDeque<>();
    stack.push(start);
    visited[start] = true;
    component[start] = root;
    while (!stack.isEmpty()) {
        int u = stack.pop();
        for (int v : revAdj.get(u)) {
            if (!visited[v]) {
                visited[v] = true;
                component[v] = root;
                stack.push(v);
            }
        }
    }
    }

    /** Three-state DFS and reverse finish order; return acyclic and order. */
    static Map<String, Object> topo(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        List<List<Integer>> adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (int[] e : edges) adj.get(e[0]).add(e[1]);

        int[] state = new int[n]; // 0=unvisited, 1=visiting, 2=visited
        List<Integer> order = new ArrayList<>();
        
        for (int i = 0; i < n; i++) {
            if (state[i] == 0) {
                if (!topoDfs(i, adj, state, order)) {
                    return Map.<String, Object>of("acyclic", false, "order", Collections.emptyList());
                }
            }
        }

        Collections.reverse(order);
        return Map.<String, Object>of("acyclic", true, "order", order);
    }

private static boolean topoDfs(int start, List<List<Integer>> adj, int[] state, List<Integer> order) {
    Deque<int[]> stack = new ArrayDeque<>();
    stack.push(new int[]{start, 0});
    state[start] = 1;
    while (!stack.isEmpty()) {
        int[] top = stack.peek();
        int u = top[0];
        if (top[1] < adj.get(u).size()) {
            int v = adj.get(u).get(top[1]++);
            if (state[v] == 1) return false;
            if (state[v] == 0) {
                state[v] = 1;
                stack.push(new int[]{v, 0});
            }
        } else {
            state[u] = 2;
            order.add(u);
            stack.pop();
        }
    }
    return true;
}

    /** Whole-graph cycle check and DAG relaxation; return acyclic, distance, parent_edge. */
    static Map<String, Object> dag(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int source = Input.source(input);
        int[][] edges = Input.edges(input);

        List<List<Edge>> adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (int i = 0; i < edges.length; i++) {
            adj.get(edges[i][0]).add(new Edge(edges[i][0], edges[i][1], edges[i][2], i));
        }

        int[] state = new int[n];
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (state[i] == 0) {
                if (!topoDfsEdge(i, adj, state, order)) {
                    return Map.<String, Object>of("acyclic", false, "distance", Collections.emptyList(), "parent_edge", Collections.emptyList());
                }
            }
        }
        Collections.reverse(order);

        Long[] dist = new Long[n];
        int[] parentEdge = new int[n];
        Arrays.fill(parentEdge, -1);
        dist[source] = 0L;

        for (int u : order) {
            if (dist[u] != null) {
                for (Edge e : adj.get(u)) {
                    if (dist[e.v] == null || dist[u] + e.weight < dist[e.v]) {
                        dist[e.v] = dist[u] + e.weight;
                        parentEdge[e.v] = e.id;
                    }
                }
            }
        }

        List<Long> distanceList = Arrays.asList(dist);
        List<Integer> parentEdgeList = new ArrayList<>(n);
        for (int p : parentEdge) parentEdgeList.add(p);

        return Map.<String, Object>of("acyclic", true, "distance", distanceList, "parent_edge", parentEdgeList);
    }
    //Helper method
    public static java.util.List<Integer> reconstructRoute(int target, int source, int[] parentEdge, int[][] edges) {
        java.util.List<Integer> route = new java.util.ArrayList<>();
        if (target == source) {
            route.add(source);
            return route;
        }
    
    int current = target;
    java.util.List<Integer> reverseRoute = new java.util.ArrayList<>();
    reverseRoute.add(current);

    while (current != source) {
        int edgeId = parentEdge[current];
        if (edgeId == -1) {
            return new java.util.ArrayList<>(); // Unreachable target, return empty route
        }
        // Assuming edges array format is [u, v, weight]
        int u = edges[edgeId][0]; 
        reverseRoute.add(u);
        current = u;
    }
    
    java.util.Collections.reverse(reverseRoute);
    return reverseRoute;
    }

private static boolean topoDfsEdge(int start, List<List<Edge>> adj, int[] state, List<Integer> order) {
    Deque<int[]> stack = new ArrayDeque<>();
    stack.push(new int[]{start, 0});
    state[start] = 1;
    while (!stack.isEmpty()) {
        int[] top = stack.peek();
        int u = top[0];
        if (top[1] < adj.get(u).size()) {
            Edge e = adj.get(u).get(top[1]++);
            if (state[e.v] == 1) return false;
            if (state[e.v] == 0) {
                state[e.v] = 1;
                stack.push(new int[]{e.v, 0});
            }
        } else {
            state[u] = 2;
            order.add(u);
            stack.pop();
        }
    }
    return true;
}
}