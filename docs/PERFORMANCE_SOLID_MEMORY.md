# Performance, SOLID, And Memory Review

This document summarizes the current design choices and the changes made to reduce avoidable allocation, tighten encapsulation, and keep the code easier to maintain.

## Performance Updates

### Cached Network Collections

`TorusNetwork` now builds and stores:

- one row-major `allNodes` list
- one precomputed neighbor list per node

Previously, `getAllNodes()` created a new list on each call, and `getNeighbors()` created a new list for each node lookup. The election loop calls these methods repeatedly, so caching removes unnecessary allocation and repeated topology work.

### Reused Rendering Constants

`TorusGridPanel` now uses static constants for repeated `Color`, `Font`, and `BasicStroke` values.

This avoids creating new graphics helper objects during every repaint, which matters during animation because the panel repaints frequently.

### Single Node List In Election Loop

`TorusElectionAlgorithm.electLeader()` now stores `network.getAllNodes()` in a local variable and reuses it through the run.

This makes the loop intent clearer and avoids repeated method calls while preserving behavior.

## SOLID Updates

### Encapsulation

The broad Lombok `@Getter` on `TorusNetwork` was removed because it generated `getNodes()`, exposing the internal two-dimensional node array. The class now exposes only the operations needed by callers:

- `getRows()`
- `getCols()`
- `getNode(...)`
- `getAllNodes()`
- `getNeighbors(...)`

`TorusElectionAlgorithm` also returns read-only views for animation steps and execution logs. Callers can read run data without mutating algorithm-owned collections.

### Single Responsibility Boundaries

The existing package split remains appropriate for this project size:

- `network` owns topology.
- `algorithm` owns election state transitions.
- `model` owns simple state objects.
- `gui` owns Swing rendering and user workflow.

The main remaining SOLID limitation is that `TorusElectionGUI` still coordinates UI construction, validation, command handling, and animation. For a larger application, the next improvement would be extracting input parsing and animation control into separate classes.

### Safer Algorithm Reuse

`ProcessNode` now has `resetElectionState()`, and `TorusElectionAlgorithm.electLeader()` resets node election state before every run.

This makes repeated calls safer and reduces hidden coupling to the GUI's current habit of creating a fresh `TorusNetwork` for each run.

## Memory Updates

### Reduced Short-Lived Objects

The main allocation reductions are:

- no per-call list allocation in `getAllNodes()`
- no per-call neighbor list allocation in `getNeighbors()`
- no repeated paint-time allocation for common colors, fonts, and strokes

### Controlled Collection Mutation

`TorusNetwork.getAllNodes()` and `TorusNetwork.getNeighbors(...)` return unmodifiable lists. `TorusElectionAlgorithm.getAnimationSteps()` and `getExecutionLog()` return unmodifiable views.

This avoids accidental external mutation while still avoiding defensive copy allocation on every read.

### Current Memory Tradeoff

The network now keeps cached neighbor lists, which adds a small fixed memory cost per node. In exchange, animation and election runs avoid repeated temporary list allocation. With the GUI capped at `8 x 8`, the fixed cost is tiny and predictable.

## Remaining Practical Limits

- The GUI limits the network to `8 x 8`, so algorithmic cost is small in normal use.
- Animation intentionally stores every message as an `AnimationStep`; this is necessary for replay. If the grid limit is raised significantly, consider streaming animation events or adding a "run without recording" mode.
- Swing work should remain on the Event Dispatch Thread. The animation thread already delegates UI updates through `SwingUtilities.invokeLater`.
