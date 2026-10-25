# Custom Tests Documentation (`tests/`)

This directory contains custom test cases (`my_cases.jsonl`) designed to verify
boundary conditions, error handling and core functionalities across all six
graph algorithms. Each case is documented below with its input, an independently
justified expected output, the actual output produced by the implementation, and
an explanation of what it verifies.

## How to Run Tests

From the repository root:

```sh
python tools/run.py --input tests/my_cases.jsonl
```

`--input` prints the actual outputs without comparing them to expected answers.
The expected/actual pairs below were captured with the same command and are
included for human review.

---

## Test 1: DSU empty graph

- **Task:** `dsu`
- **Input:**

```json
{"task":"dsu","n":0,"operations":[]}
```

- **Explanation:** Boundary `n=0`. No components, no states. Verifies the
  implementation does not divide by zero or access `parent[0]`.
- **Expected Output:** `{"states":[]}`
- **Actual Output:** `{"states":[]}`

## Test 2: DSU self-union and repeated union

- **Task:** `dsu`
- **Input:**

```json
{"task":"dsu","n":2,"operations":[[0,0],[0,1],[1,0]]}
```

- **Explanation:** `[0,0]` is a self-union (must not change partition);
  `[0,1]` merges the two singletons; `[1,0]` is a repeated union of an already
  connected pair. Verifies idempotence and that `largest` updates correctly.
- **Expected Output:** `{"states":[[2,1],[1,2],[1,2]]}`
- **Actual Output:** `{"states":[[2,1],[1,2],[1,2]]}`

## Test 3: DSU disconnected components

- **Task:** `dsu`
- **Input:**

```json
{"task":"dsu","n":6,"operations":[[0,1],[2,3],[4,5],[1,3]]}
```

- **Explanation:** Three disjoint pairs are formed, then two of them are joined.
  Verifies component count and largest-component tracking across merges.
- **Expected Output:** `{"states":[[5,2],[4,2],[3,2],[2,4]]}`
- **Actual Output:** `{"states":[[5,2],[4,2],[3,2],[2,4]]}`

## Test 4: Kruskal empty graph

- **Task:** `kruskal`
- **Input:**

```json
{"task":"kruskal","n":0,"edges":[]}
```

- **Explanation:** Boundary `n=0`. Must return zero cost and zero components.
- **Expected Output:** `{"cost":0,"edge_ids":[],"components":0}`
- **Actual Output:** `{"cost":0,"edge_ids":[],"components":0}`

## Test 5: Kruskal isolated vertices

- **Task:** `kruskal`
- **Input:**

```json
{"task":"kruskal","n":4,"edges":[]}
```

- **Explanation:** Four isolates, no edges. Every vertex is its own component.
- **Expected Output:** `{"cost":0,"edge_ids":[],"components":4}`
- **Actual Output:** `{"cost":0,"edge_ids":[],"components":4}`

## Test 6: Kruskal negative weights, self-loops, parallel edges

- **Task:** `kruskal`
- **Input:**

```json
{"task":"kruskal","n":5,"edges":[[0,0,-100],[0,1,7],[0,1,-3],[1,2,2],[3,4,0],[4,4,-200]]}
```

- **Explanation:** Self-loops (`[0,0]`, `[4,4]`) must never be selected.
  Parallel edges `[0,1,7]` and `[0,1,-3]` — only the cheaper one is useful.
  Negative weights must be summed correctly. Components: {0,1,2} and {3,4}.
- **Expected Output:** `{"cost":-1,"edge_ids":[2,4,3],"components":2}`
  - Note: `edge_ids` order may vary; `[3,4,2]` is equally valid (same cost).
- **Actual Output:** `{"cost":-1,"edge_ids":[2,4,3],"components":2}`

## Test 7: Kruskal 64-bit overflow protection

- **Task:** `kruskal`
- **Input:**

```json
{"task":"kruskal","n":4,"edges":[[0,1,1000000000],[1,2,1000000000],[2,3,1000000000]]}
```

- **Explanation:** Sum of three `10^9` weights exceeds 32-bit signed range
  (`2 147 483 647`). Verifies `long` accumulation.
- **Expected Output:** `{"cost":3000000000,"edge_ids":[0,1,2],"components":1}`
- **Actual Output:** `{"cost":3000000000,"edge_ids":[0,1,2],"components":1}`

## Test 8: Prim empty graph

- **Task:** `prim`
- **Input:**

```json
{"task":"prim","n":0,"edges":[]}
```

- **Explanation:** Boundary `n=0`, Prim must not enqueue any vertex.
- **Expected Output:** `{"cost":0,"edge_ids":[],"components":0}`
- **Actual Output:** `{"cost":0,"edge_ids":[],"components":0}`

## Test 9: Prim isolated vertices

- **Task:** `prim`
- **Input:**

```json
{"task":"prim","n":4,"edges":[]}
```

- **Explanation:** Prim restarts in every component; with no edges each vertex
  is a separate component.
- **Expected Output:** `{"cost":0,"edge_ids":[],"components":4}`
- **Actual Output:** `{"cost":0,"edge_ids":[],"components":4}`

## Test 10: Prim negative weights, self-loops, parallel edges

- **Task:** `prim`
- **Input:**

```json
{"task":"prim","n":5,"edges":[[0,0,-100],[0,1,7],[0,1,-3],[1,2,2],[3,4,0],[4,4,-200]]}
```

- **Explanation:** Same graph as Test 6, solved by Prim. Must agree with
  Kruskal on cost and components; `edge_ids` may differ.
- **Expected Output:** `{"cost":-1,"edge_ids":[2,4,3],"components":2}`
  - Note: any valid MSF with cost `-1` and 2 components is accepted.
- **Actual Output:** `{"cost":-1,"edge_ids":[2,4,3],"components":2}`

## Test 11: Prim 64-bit overflow protection

- **Task:** `prim`
- **Input:**

```json
{"task":"prim","n":4,"edges":[[0,1,1000000000],[1,2,1000000000],[2,3,1000000000]]}
```

- **Explanation:** Same as Test 7, solved by Prim.
- **Expected Output:** `{"cost":3000000000,"edge_ids":[0,1,2],"components":1}`
- **Actual Output:** `{"cost":3000000000,"edge_ids":[0,1,2],"components":1}`

## Test 12: SCC empty graph

- **Task:** `scc`
- **Input:**

```json
{"task":"scc","n":0,"edges":[]}
```

- **Explanation:** Boundary `n=0`; both arrays empty.
- **Expected Output:** `{"component":[],"condensation":[]}`
- **Actual Output:** `{"component":[],"condensation":[]}`

## Test 13: SCC isolated vertices

- **Task:** `scc`
- **Input:**

```json
{"task":"scc","n":4,"edges":[]}
```

- **Explanation:** Each isolated vertex is its own SCC; no cross-component edges.
- **Expected Output:** `{"component":[3,2,1,0],"condensation":[]}`
  - Note: labels need not be consecutive; `[0,1,2,3]` is also valid.
- **Actual Output:** `{"component":[3,2,1,0],"condensation":[]}`

## Test 14: SCC duplicate edges and self-loops

- **Task:** `scc`
- **Input:**

```json
{"task":"scc","n":5,"edges":[[0,0],[0,1],[1,0],[1,2],[1,2],[2,3],[3,2],[3,4],[4,4]]}
```

- **Explanation:** Self-loops `[0,0]` and `[4,4]` are internal; duplicate
  `[1,2]` appears twice and must not duplicate condensation pairs. SCCs are
  `{0,1}`, `{2,3}`, `{4}`; condensation has exactly two distinct edges.
- **Expected Output:** `{"component":[0,0,1,1,2],"condensation":[[0,1],[1,2]]}`
- **Actual Output:** `{"component":[0,0,1,1,2],"condensation":[[0,1],[1,2]]}`

## Test 15: SCC simple cycle

- **Task:** `scc`
- **Input:**

```json
{"task":"scc","n":3,"edges":[[0,1],[1,2],[2,0]]}
```

- **Explanation:** Directed ring — all three vertices form one SCC; condensation
  is empty.
- **Expected Output:** `{"component":[0,0,0],"condensation":[]}`
- **Actual Output:** `{"component":[0,0,0],"condensation":[]}`

## Test 16: Topo empty graph

- **Task:** `topo`
- **Input:**

```json
{"task":"topo","n":0,"edges":[]}
```

- **Explanation:** Boundary `n=0`; empty graph is trivially acyclic.
- **Expected Output:** `{"acyclic":true,"order":[]}`
- **Actual Output:** `{"acyclic":true,"order":[]}`

## Test 17: Topo parallel edges and isolates

- **Task:** `topo`
- **Input:**

```json
{"task":"topo","n":5,"edges":[[0,2],[0,2],[1,2],[2,3]]}
```

- **Explanation:** Duplicate `[0,2]` must not affect the order. Vertex `4` is
  isolated. Any valid topological order is accepted.
- **Expected Output:** `{"acyclic":true,"order":[4,1,0,2,3]}`
  - Note: `[1,4,0,2,3]`, `[0,1,4,2,3]` etc. are equally valid.
- **Actual Output:** `{"acyclic":true,"order":[4,1,0,2,3]}`

## Test 18: Topo self-cycle

- **Task:** `topo`
- **Input:**

```json
{"task":"topo","n":1,"edges":[[0,0]]}
```

- **Explanation:** A self-loop is a directed cycle; must return `false`.
- **Expected Output:** `{"acyclic":false,"order":[]}`
- **Actual Output:** `{"acyclic":false,"order":[]}`

## Test 19: Topo disconnected cycle

- **Task:** `topo`
- **Input:**

```json
{"task":"topo","n":4,"edges":[[0,1],[2,3],[3,2]]}
```

- **Explanation:** Component `{0,1}` is acyclic, but `{2,3}` forms a cycle.
  The whole graph must be reported as cyclic.
- **Expected Output:** `{"acyclic":false,"order":[]}`
- **Actual Output:** `{"acyclic":false,"order":[]}`

## Test 20: DAG singleton

- **Task:** `dag`
- **Input:**

```json
{"task":"dag","n":1,"source":0,"edges":[]}
```

- **Explanation:** Boundary: single vertex, source is target.
- **Expected Output:** `{"acyclic":true,"distance":[0],"parent_edge":[-1]}`
- **Actual Output:** `{"acyclic":true,"distance":[0],"parent_edge":[-1]}`

## Test 21: DAG unreachable vertices with negative edges

- **Task:** `dag`
- **Input:**

```json
{"task":"dag","n":4,"source":0,"edges":[[0,1,-1],[2,3,-8]]}
```

- **Explanation:** Vertices `2` and `3` are unreachable from `0`. Verifies
  `null` distance and `-1` parent for unreachable vertices; also confirms that
  `-1` is a valid finite cost (not confused with "unreachable").
- **Expected Output:** `{"acyclic":true,"distance":[0,-1,null,null],"parent_edge":[-1,0,-1,-1]}`
- **Actual Output:** `{"acyclic":true,"distance":[0,-1,null,null],"parent_edge":[-1,0,-1,-1]}`

## Test 22: DAG cycle outside source reachability

- **Task:** `dag`
- **Input:**

```json
{"task":"dag","n":4,"source":0,"edges":[[0,1,2],[2,3,1],[3,2,1]]}
```

- **Explanation:** Source `0` reaches only vertex `1`; the cycle `2↔3` is
  unreachable. The whole graph must still be checked, so the result is
  `acyclic=false`.
- **Expected Output:** `{"acyclic":false,"distance":[],"parent_edge":[]}`
- **Actual Output:** `{"acyclic":false,"distance":[],"parent_edge":[]}`

## Test 23: DAG self-cycle

- **Task:** `dag`
- **Input:**

```json
{"task":"dag","n":1,"source":0,"edges":[[0,0,0]]}
```

- **Explanation:** Self-loop makes the graph cyclic.
- **Expected Output:** `{"acyclic":false,"distance":[],"parent_edge":[]}`
- **Actual Output:** `{"acyclic":false,"distance":[],"parent_edge":[]}`

## Test 24: DAG tied paths and parallel edges

- **Task:** `dag`
- **Input:**

```json
{"task":"dag","n":4,"source":0,"edges":[[0,1,1],[0,2,1],[1,3,1],[2,3,1],[0,1,1]]}
```

- **Explanation:** Two shortest paths `0→1→3` and `0→2→3` both cost 2; parallel
  `[0,1,1]` duplicates. Any valid parent choice is accepted. The chosen parent
  for `3` is edge `3` (`2→3`); `[0,1,1,2]` is also valid.
- **Expected Output:** `{"acyclic":true,"distance":[0,1,1,2],"parent_edge":[-1,0,1,3]}`
- **Actual Output:** `{"acyclic":true,"distance":[0,1,1,2],"parent_edge":[-1,0,1,3]}`

## Test 25: DAG 64-bit overflow protection

- **Task:** `dag`
- **Input:**

```json
{"task":"dag","n":4,"source":0,"edges":[[0,1,1000000000],[1,2,1000000000],[2,3,1000000000]]}
```

- **Explanation:** Path cost `3·10^9` exceeds 32-bit signed range; verifies
  `long` distances.
- **Expected Output:** `{"acyclic":true,"distance":[0,1000000000,2000000000,3000000000],"parent_edge":[-1,0,1,2]}`
- **Actual Output:** `{"acyclic":true,"distance":[0,1000000000,2000000000,3000000000],"parent_edge":[-1,0,1,2]}`

## Test 26: DAG unreachable (no cycle)

- **Task:** `dag`
- **Input:**

```json
{"task":"dag","n":4,"source":0,"edges":[[0,1,5],[2,3,10]]}
```

- **Explanation:** Disconnected DAG: `2` and `3` are in a separate component;
  they must be reported with `null` distance and `-1` parent.
- **Expected Output:** `{"acyclic":true,"distance":[0,5,null,null],"parent_edge":[-1,0,-1,-1]}`
- **Actual Output:** `{"acyclic":true,"distance":[0,5,null,null],"parent_edge":[-1,0,-1,-1]}`

## Test 27: Kruskal / Prim disconnected components

- **Task:** `kruskal` and `prim`
- **Input:**

```json
{"task":"kruskal","n":5,"edges":[[0,1,10],[1,2,5]]}
```

- **Explanation:** Vertices `0,1,2` form one component with MSF cost 15; vertices
  `3,4` are isolates. Total components = 3. Both algorithms must produce the
  same cost and component count; `edge_ids` order may differ.
- **Expected Output:** `{"components":3,"cost":15,"edge_ids":[0,1]}`
  or `{"components":3,"cost":15,"edge_ids":[1,0]}`
- **Actual Output (kruskal):** `{"components":3,"cost":15,"edge_ids":[0,1]}`
- **Actual Output (prim):** `{"components":3,"cost":15,"edge_ids":[0,1]}`

---
## Test 28–30: Deep chains (n = 10 000)

These three cases exercise the explicit-stack DFS on a chain of 10 000
vertices. With a recursive DFS, 10 000 nested frames exceed the default JVM
call stack (~3000–10000 frames), so this size is sufficient to catch a
`StackOverflowError`. The published limit (n = 100 000) is verified locally
but not committed to keep the repository small; see `ANALYSIS.md` §9.

- **Test 28 — scc deep chain:** chain `0→1→2→…→9999`.
  Expected: `component = [0,1,2,…,9999]`,
  `condensation = [[0,1],[1,2],…,[9998,9999]]`.
- **Test 29 — topo deep chain:** chain `0→1→2→…→9999`.
  Expected: `acyclic = true`, `order = [0,1,2,…,9999]`.
- **Test 30 — dag deep chain:** chain `0→1→2→…→9999`, weight 1 per edge,
  source 0. Expected: `acyclic = true`,
  `distance = [0,1,2,…,9999]`, `parent_edge = [-1,0,1,2,…,9998]`.
  
## Alternative valid answer and invalid witness

Because forests, SCC labels, topological orders and shortest-path parents are
not unique, the checker must accept any *semantically valid* answer and reject
an invalid witness. Examples verified against `tools/public_validate.py`:

### Kruskal alternative optimum (valid)

Graph: `n=4`, edges `[[0,1,-2],[0,1,-2],[1,2,3],[0,2,3],[3,3,-99]]`.
Expected: `{"cost":1,"components":2,"edge_ids":[0,2]}`.
Alternative valid answer: `{"cost":1,"components":2,"edge_ids":[1,3]}` — same
cost, different tied edges. Both are accepted by the validator.

### Kruskal invalid witness (rejected)

Same graph. Invalid answers such as `edge_ids=[0,0]` (duplicate), `[0,1]`
(cycle), `[0,4]` (self-loop), `[0]` (too few edges), `[0,2,3]` (too many edges)
are all rejected.

### SCC alternative labels (valid)

Graph: `n=3`, edges `[[0,1],[1,0],[1,2]]`. Expected:
`{"component":[0,0,1],"condensation":[[0,1]]}`. Alternative valid:
`{"component":[7,7,20],"condensation":[[7,20]]}` — labels are arbitrary.

### SCC invalid witness (rejected)

Same graph. Invalid: duplicate condensation pair `[[5,9],[5,9]]`; missing
cross edge `[]`; merged components `{"component":[5,5,5],"condensation":[]}`.

### Topo alternative order (valid)

Graph: `n=3`, edges `[[0,2],[1,2]]`. Expected order `[0,1,2]`; alternative
valid `[1,0,2]`; both respect `0<2` and `1<2`.

### DAG alternative parent (valid)

Graph: `n=5`, source `0`, edges
`[[0,1,2],[0,1,2],[0,2,1],[1,3,-3],[2,3,-2]]`.
Expected parents `[-1,0,2,3,-1]`; alternative valid `[-1,1,2,4,-1]` uses the
parallel edge `1` for vertex `1` and edge `4` for vertex `3`, same distances.

### DAG invalid witness (rejected)

Same graph. Invalid: parents `[-1,0,2,0,-1]` (edge `0` ends at `1`, not `3`),
`[-1,0,2,3,4]` (unreachable vertex `4` has non-`-1` parent), `[0,0,2,3,-1]`
(source has non-`-1` parent).

---

## Deep chain coverage

The published public tests `scc deep chain`, `topo deep chain` and
`dag deep chain` in `tools/public.json` already exercise 2000-vertex chains.
The full published limit (`n = 100000`) is verified locally with a one-off
generator (not committed to keep the repository small); see `ANALYSIS.md`
section "Deep DFS" for the verification note. The implementation uses an
explicit-stack DFS, so call-stack depth is independent of graph depth.

## State reset

`run.py --check` repeats the entire public batch twice in the same process
(`state reset: two complete batches in one process`). All 36 public cases must
pass both times, which verifies that no mutable static state leaks between
requests. Custom cases follow the same contract: every `handle()` call
allocates fresh arrays and lists.