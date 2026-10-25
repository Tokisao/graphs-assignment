# Assignment 3 Analysis

## 1. Disjoint-Set Union (DSU)

- **Steps:** Maintains a parent array and a size/rank array. `find` uses path
  compression by updating parent pointers directly to the root. `union` merges
  the smaller tree under the root of the larger tree (union by size/rank) and
  updates the component count and maximum component size.
- **Invariant:** Every vertex points to an ancestor; the root of a tree
  uniquely identifies the connected component.
- **Correctness:** Path compression ensures shallow trees. Merging by size
  ensures the tree depth grows logarithmically in the worst case.
- **Boundary Cases:** `n=0` yields `largest=0` and no states. Self-unions
  (`u == v`) and already connected vertices skip merging and do not alter state.
- **Time/Memory Complexity:** Time `O(n + q α(n))` amortized. Space `O(n + q)`
  to store parent/size arrays and the accumulated states array.

## 2. Minimum Spanning Forest (Kruskal & Prim)

- **Steps (Kruskal):** Sorts edges by weight. Iterates through edges, adding
  them to the MSF if their endpoints belong to different components (using DSU).
- **Steps (Prim):** Uses an adjacency list and a priority queue. Starts from
  vertex 0, greedily adding the minimum-weight edge that connects a visited
  vertex to an unvisited one. Restarts the search for any remaining unvisited
  vertices to handle disconnected forests.
- **Invariant:** The selected edges form an acyclic forest minimizing total
  edge weight.
- **Correctness (Kruskal):** The cut property guarantees that the cheapest
  edge across any cut is safe; DSU rejects edges that would form a cycle.
- **Correctness (Prim):** The cut property applied to the growing tree; the
  lazy heap always exposes the cheapest outgoing edge of the current component.
- **Boundary Cases:** Unconnected components are safely skipped in Kruskal and
  triggered by the outer loop in Prim. Isolated vertices count as components.
  Negative edge weights are processed correctly without endless loops.
  Self-loops are rejected by DSU (Kruskal) or never leave the heap (Prim).
- **Time/Memory Complexity:** Both are bounded by `O(n + m log m)`. Space is
  `O(n + m)`. Total costs use 64-bit integers to prevent overflow.

## 3. Strongly Connected Components (Kosaraju SCC)

- **Steps:** Two DFS passes. The first pass records the finish order of
  vertices on the original graph. The second pass processes vertices in reverse
  finish order on the transposed (reversed edges) graph.
- **Invariant:** Vertices reached during a single traversal in the second pass
  form a maximal strongly connected component.
- **Correctness:** In the condensation DAG, the vertex finished last in the
  first pass belongs to a source SCC. Traversing the transpose in decreasing
  finish order visits exactly one SCC per call.
- **Deep DFS:** See §9; DFS is iterative (explicit stack), so deep chains do
  not cause `StackOverflowError`.
- **Time/Memory Complexity:** `O(n + m)` time. `O(n + m)` space for adjacency
  lists, transposed lists, and condensation sets. Deduplication of
  cross-component edges is handled via hashing.

## 4. Topological Ordering (Topo)

- **Steps:** Performs a DFS keeping track of three states: 0 (unvisited),
  1 (active/visiting), 2 (finished). If an active node is encountered, a cycle
  exists.
- **Invariant:** If no cycle is found, appending a node upon its DFS finish
  and reversing the result yields a valid topological order.
- **Correctness:** A directed graph is acyclic iff DFS has no back edge. The
  reverse finish order is a topological order for any DAG.
- **Boundary Cases:** `n=0` is acyclic. Self-loops and disconnected cycles are
  detected. Duplicate edges do not affect the result.
- **Time/Memory Complexity:** `O(n + m)` time and `O(n + m)` space.

## 5. Shortest Paths in DAG (DAG)

- **Steps:** First checks if the graph is acyclic using the Topo logic. If
  acyclic, relaxes outgoing edges for all vertices in topological order.
  Unreachable vertices maintain a `null` distance.
- **Invariant:** When a vertex is processed, all its predecessors have already
  been processed, so its distance is final.
- **Correctness:** The whole graph is checked for cycles, including components
  unreachable from the source, as required. Relaxation in topological order
  handles negative weights correctly.
- **Route Helper:** `reconstructRoute(target, source, parentEdge, edges)`
  walks backward from `target` using `parentEdge[id]` to find the predecessor,
  then reverses the list. Returns `[source]` when `target == source`, and `[]`
  for unreachable targets. See §10 for four worked calls.
- **Boundary Cases:** Self-loop makes the graph cyclic. Source has distance 0
  and parent `-1`. Unreachable vertices have `null` distance and `-1` parent.
  Parallel/tied edges may produce different valid parent arrays.
- **Time/Memory Complexity:** `O(n + m)` time and space. Helper `O(L)`.

## 6. State Reset

State reset is handled natively by processing each JSON request within a fresh
method invocation. No global or static mutable state is preserved between
inputs. `run.py --check` verifies this by running the full public batch twice
in the same process.

## 7. Kruskal vs Prim Benchmark
**Status:** performed. Numbers below are actual medians from 3 runs per
method per size, excluding compilation; each run is a separate JVM process
(`subprocess.run`), so JVM startup and JSON parsing are included in the
measurement boundary.

- **Environment:** Java 17 (`javac --release 17`), Windows 11 Home Single 
Language (build 10.0.26200), 13th Gen Intel Core i5-13420H, 16 GB RAM.
- **Timer:** `time.perf_counter()` around `subprocess.run` (process-level).
- **Generator:** `random.seed(42)`, edges `u,v ∈ [0,n)`, `w ∈ [-500,500]`, `m = 5n`.
- **Warm-up:** 2 full runs, discarded.
- **Boundary:** process-level wall time (includes JVM startup + JSON parsing); compilation excluded.
- **Sizes:** `(n,m) = (1000,5000), (10000,50000), (50000,200000)`.
- **Timings:** 3 per method per size; median reported.

| Size | n | m | Kruskal median (ms) | Prim median (ms) |
|---|---:|---:|---:|---:|
| Small  | 1 000  | 5 000   | 240.3 | 227.5 |
| Medium | 10 000 | 50 000  | 314.4 | 350.4 |
| Large  | 50 000 | 200 000 | 597.0 | 769.7 |

**Observations:** At the small size Prim is slightly faster (227.5 ms vs
240.3 ms); at the medium and large sizes Kruskal is faster (314.4 vs 350.4 ms
and 597.0 vs 769.7 ms respectively). The gap grows with size, which is
consistent with Prim's lazy heap performing more heap operations per vertex
as the graph gets denser, while Kruskal's `O(m log m)` sort dominates but
scales smoothly. Both algorithms stay well below the 20-second batch limit.
The asymptotic argument in §2 (`O(n + m log m)` for both) predicts the same
growth order; constant factors and heap overhead explain the observed
difference.

## 8. Build / Run / Test Commands

```sh
# Build (Java 17)
javac --release 17 -encoding UTF-8 -d .build/Java src/Java/*.java

# Run interactively (JSON Lines on stdin/stdout)
java -Xmx384m -cp .build/Java Main < input.jsonl

# Provided launcher (recommended)
python tools/run.py --setup
python tools/run.py --check
python tools/run.py --input tests/my_cases.jsonl
```

## 9. Deep DFS: why the DFS passes are iterative

The DFS passes in `scc` (Kosaraju first/second pass), `topo` (three-state DFS)
and `dag` (cycle check) are implemented with an **explicit stack**
(`Deque<int[]>` holding `[vertex, next_child_index]`), not with Java recursion.

**Reason:** the published limit is `n ≤ 100000`. A recursive DFS on a chain
of 100000 vertices would create 100000 JVM call frames. The default HotSpot
call stack is roughly 512 KB – 1 MB, which allows only ~3000–10000 frames
before `StackOverflowError`. The launcher (`tools/run.py`) fixes `-Xmx384m` and
does **not** set `-Xss`, so increasing the call stack is not an option. The
explicit-stack DFS stores frames on the heap (a `Deque` of `int[2]`), where
384 MB is more than enough for 100000 vertices (~10 MB including `ArrayDeque`
overhead). Time and space complexity remain `O(n + m)`; only the storage
location of the stack changes, which removes the depth limit.

**Semantics preserved:** the post-order `order.add(u)` (SCC/topo) and
`state[u] = 2` (three-state) happen in the `else` branch, i.e. after all
children of `u` have been pushed and popped. This preserves the exact
semantics of the recursive version, so Kosaraju's finish-order argument and
the topological order are unchanged.

**Deep-chain verification:** locally generated chains of `n = 100000` for
`scc`, `topo` and `dag` complete without stack overflow (the generator script
was run once and not committed to avoid a multi-MB `my_cases.jsonl`). The
2000-vertex deep chains in `tools/public.json` are covered by
`python tools/run.py --check`.

## 10. Route-helper calls

`Algorithms.reconstructRoute(target, source, parentEdge, edges)` returns a
`List<Integer>` of vertices from `source` to `target`, or an empty list if
`target` is unreachable. Selected case: the example from the assignment
(`n=5, source=0, edges=[[0,1,3],[0,2,8],[1,2,-7],[2,3,4]]`).

**Case A — reachable target `3`.**
`parent_edge = [-1,0,2,3,-1]`. Route:
`reconstructRoute(3, 0, parent_edge, edges)` → `[0,1,2,3]`.
Total weight `3 + (-7) + 4 = 0`, matching `distance[3] = 0`. Direction is
source→target; every step follows an edge that ends at the current vertex.

**Case B — source itself.**
`reconstructRoute(0, 0, parent_edge, edges)` → `[0]`. The helper returns the
trivial route without consulting `parent_edge`, as required.

**Case C — unreachable target `4`.**
`parent_edge[4] = -1`, `distance[4] = null`.
`reconstructRoute(4, 0, parent_edge, edges)` → `[]` (empty list, documented
no-route result). The helper does not loop forever.

**Case D — tied / parallel edges.**
Graph: `n=4, source=0, edges=[[0,1,1],[0,2,1],[1,3,1],[2,3,1],[0,1,1]]`.
Two shortest paths to `3` exist (`0→1→3` and `0→2→3`), both cost 2; edge `4`
is a parallel duplicate of edge `0`. Suppose the algorithm selects
`parent_edge = [-1,0,1,3,-1]` (parent of `3` is edge `3 = 2→3`).
`reconstructRoute(3, 0, parent_edge, edges)` → `[0,2,3]`, cost `1+1=2`,
matching `distance[3]=2`. If the algorithm had selected
`parent_edge=[-1,0,1,2,-1]` (edge `2 = 1→3`), the helper would return
`[0,1,3]` — also valid. Both are accepted by `public_validate.check` because
the checker verifies distance consistency and chain termination, not a fixed
route.

## 11. Sources and AI assistance

- **Assignment specification:** `README.md` and `docs/Assignment_3_EN.pdf` in
  this repository; public examples in `tools/public.json` and the validator
  `tools/public_validate.py`.
- **AI assistance:** I used Claude (Anthropic) to review algorithm correctness,
  draft the iterative-DFS refactor (explicit stack replacing recursion) and
  help organize this `ANALYSIS.md`. All algorithm code was written and verified
  by me by running `python tools/run.py --check` (36 public cases, pass) and
  `python tools/run.py --input tests/my_cases.jsonl` (custom cases, reviewed
  manually in `tests/README.md`). No external code was copied.
- **External sources:** no textbooks or online code were copied. Standard
  algorithm descriptions (DSU with path compression + union by size, Kruskal,
  Prim, Kosaraju, three-state DFS, DAG relaxation) were used from course
  materials.

## 12. Known limitations
- Custom deep-chain tests at `n = 100000` are verified locally but not
  committed (repository size).