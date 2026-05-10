# Performance, SOLID, And Memory Review

Last reviewed: May 11, 2026.

This project is acceptable for the current desktop GUI scope. The GUI limits the network to `8 x 8`, so CPU and memory use are predictable. The core topology, algorithm, logging, and rendering code now avoid the main unnecessary allocations that existed earlier.

## Current Status

| Area | Status | Notes |
| --- | --- | --- |
| Performance | Meets current scope | Cached topology, reused render constants, and bounded GUI input keep runtime cost small. |
| SOLID | Mostly meets current scope | Domain packages are separated well. The main remaining compromise is that `TorusElectionGUI` still owns UI construction, input parsing, command handling, and animation coordination. |
| Memory | Meets current scope | Cached immutable lists reduce temporary allocation. Animation history is intentionally retained for replay and is bounded by the `8 x 8` GUI limit. |

## Changes Applied

### Network Topology

`TorusNetwork` builds and stores:

- one row-major `allNodes` list
- one precomputed neighbor list per node

`getAllNodes()` and `getNeighbors(...)` return unmodifiable cached lists. This removes repeated list allocation during election and animation while protecting internal collections from caller mutation.

### Election Algorithm

`TorusElectionAlgorithm.electLeader()` now reuses the cached node list throughout the run, including the final maximum-ID lookup. It also clears previous run data and resets each node before a new election, making repeated runs safer.

The algorithm exposes read-only views from:

- `getAnimationSteps()`
- `getExecutionLog()`

This keeps callers from mutating algorithm-owned collections.

### Rendering

`TorusGridPanel` uses static constants for repeated `Color`, `Font`, and `BasicStroke` objects. This avoids recreating graphics helper objects during frequent repaint calls.

`TorusElectionGUI` now also reuses UI colors, status colors, legend colors, the rounded-panel border stroke, and the time formatter instead of constructing those repeatedly.

### Logging

`AppLog.append(...)` still writes synchronously, which is acceptable because log volume is small. It now creates the log directory once per successful run instead of calling `Files.createDirectories(...)` on every log append.

If a write fails, the logger resets the directory-ready flag and reports the error to `System.err`, so a later append can retry directory creation.

## SOLID Review

### Single Responsibility

The package boundaries are reasonable:

- `network` owns torus topology and neighbor lookup.
- `algorithm` owns leader-election state transitions.
- `model` owns small data/state objects.
- `logging` owns persistent application log paths and writes.
- `gui` owns Swing rendering and user workflow.

The main remaining improvement would be splitting `TorusElectionGUI` into smaller collaborators:

- an input parser/validator for rows, columns, and process IDs
- an animation controller for thread lifecycle
- a view builder for Swing layout

That refactor is not required for the current project size, but it would improve testability if the GUI grows.

### Open/Closed

The current algorithm is concrete and direct. If multiple election algorithms are added later, introduce a small interface such as `LeaderElectionAlgorithm` with methods for execution, logs, animation steps, rounds, and message count.

### Liskov Substitution

The project does not currently rely on inheritance for domain behavior. This keeps Liskov risk low.

### Interface Segregation

There are no oversized public interfaces. Public APIs are small and task-specific.

### Dependency Inversion

The GUI directly creates `TorusNetwork` and `TorusElectionAlgorithm`. This is fine for a small Swing application. If the app gains tests or multiple algorithm implementations, inject an algorithm factory into the GUI instead of constructing the algorithm directly.

## Performance Review

For a grid of `n = rows * cols`:

- network construction is `O(n)`
- each round processes four directed neighbor exchanges per node, so one round is `O(4n)`, effectively `O(n)`
- memory for topology is `O(n)`
- memory for animation is `O(steps)`, where `steps` is the number of recorded message exchanges

The animation step list is the largest intentional memory cost. It is needed so the GUI can replay every phase. With the current `8 x 8` cap, this is acceptable.

## Memory Review

The project now reduces short-lived allocation by:

- caching node and neighbor lists
- returning unmodifiable views instead of defensive copies
- reusing render constants
- reusing the GUI time formatter
- avoiding repeated log-directory creation after the first successful write

The main retained objects during a run are:

- `ProcessNode` objects
- immutable `Position` objects
- cached neighbor lists
- execution log strings
- recorded `AnimationStep` objects

This is appropriate for the current GUI cap.

## Remaining Tradeoffs

- `TorusElectionGUI` is still the largest class and mixes several responsibilities.
- Animation stores all steps before playback. This supports replay but would need redesign if much larger grids are allowed.
- Logging is synchronous. This keeps the code simple, but a future high-volume logger should use buffering or a background writer.
- There are no automated tests in the repository yet. Adding unit tests for `TorusNetwork`, `TorusElectionAlgorithm`, and `AppLog` would make future refactors safer.

## Verification

The project was compiled after the latest updates:

```bash
mvn compile
```

Result: build successful.
