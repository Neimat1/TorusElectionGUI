# Project Documentation

## Overview

Torus Network Leader Election GUI is a Java Swing application that demonstrates leader election on a two-dimensional torus topology. It models each process as a grid node, connects every node to four neighbors with wrap-around edges, and elects the process with the largest ID as leader by propagating maximum known IDs through the network.

The application is intended for interactive demonstration: users can configure the network, run the election immediately, or animate each message exchange.

## Prototype

The project includes a desktop Swing prototype and a static HTML documentation preview:

- The executable prototype is `gui.TorusElectionGUI`, launched from `Main`.
- The GitHub Pages preview is in `docs/index.html` under the "Current GUI Prototype" section.
- The HTML preview is not a browser implementation of the algorithm; it is a static visual guide to the Swing application's layout and workflow.
- The full interactive behavior runs through the Java desktop app, packaged as a jar, `.deb`, `.exe`, or snap.

## Package Structure

```text
src/
  main/
    java/
      Main.java
      algorithm/
        TorusElectionAlgorithm.java
      gui/
        TorusElectionGUI.java
        TorusGridPanel.java
      logging/
        AppLog.java
      model/
        AnimationStep.java
        Position.java
        ProcessNode.java
      network/
        TorusNetwork.java
```

## Component Responsibilities

### `Main`

Entry point for the application. It starts `TorusElectionGUI` using `SwingUtilities.invokeLater`, which ensures Swing components are created on the Event Dispatch Thread.

### `gui.TorusElectionGUI`

Main application window and workflow coordinator.

Responsibilities:

- Builds the Swing layout and all panels.
- Reads rows, columns, and process IDs from user input.
- Validates that the number of IDs equals `rows * columns`.
- Validates that process IDs are unique.
- Creates `TorusNetwork` and `TorusElectionAlgorithm` instances.
- Runs the election immediately or launches an animation thread.
- Updates status labels and execution log output.
- Stops a running animation when `Reset` is clicked.

Important fields:

- `currentNetwork`: active torus network shown in the visualization.
- `gridPanel`: custom drawing panel for the network.
- `animationRunning`: shared flag used to stop animation safely.
- `animationThread`: background thread that replays recorded animation steps.

### `gui.TorusGridPanel`

Custom `JPanel` responsible for rendering the torus visualization.

Responsibilities:

- Draws direct horizontal and vertical neighbor links.
- Draws dashed wrap-around links to show torus behavior.
- Draws process nodes with ID and `maxKnownId`.
- Highlights sender, receiver, and leader states.
- Draws animated transmitted values between sender and receiver.
- Reserves a footer band for wrap-around notes so text remains readable.

Visual state colors:

- Process: light blue
- Sender: yellow
- Receiver: red or light red
- Leader: green

### `logging.AppLog`

Small file logger for persistent user logs.

Responsibilities:

- Resolves the log directory from `XDG_DATA_HOME` when set.
- Uses `%LOCALAPPDATA%\torus-election-gui\logs` on Windows.
- Falls back to `~/.local/share/torus-election-gui/logs`.
- Appends visible GUI log text to `torus-election-gui.log`.
- Creates the log directory on first write.

### `network.TorusNetwork`

Owns the topology and node grid.

Responsibilities:

- Validates minimum network size of `2 x 2`.
- Validates that ID count matches the grid size.
- Creates a two-dimensional `ProcessNode` array.
- Returns all nodes in row-major order.
- Returns four neighbors for any node using modular arithmetic:
  - right: `(c + 1) % cols`
  - left: `(c - 1 + cols) % cols`
  - down: `(r + 1) % rows`
  - up: `(r - 1 + rows) % rows`

### `algorithm.TorusElectionAlgorithm`

Runs the leader election over a `TorusNetwork`.

Responsibilities:

- Clears previous run state before each election.
- Repeatedly propagates max-known IDs between neighbors.
- Counts rounds and messages.
- Records each message as an `AnimationStep`.
- Records human-readable execution log lines.
- Marks the node with the maximum original process ID as leader.

The algorithm stops when a full round completes with no `maxKnownId` changes.

### `model.ProcessNode`

Represents one process in the torus.

Fields:

- `id`: immutable process ID.
- `position`: immutable row/column position.
- `maxKnownId`: largest ID known by this process so far.
- `leader`: whether this process is the elected leader.

### `model.Position`

Immutable row/column value object. It implements `equals`, `hashCode`, and `toString`, allowing positions to be compared during animation highlighting.

### `model.AnimationStep`

Immutable record-like object describing one transmitted message.

Fields:

- sender position
- receiver position
- sender ID
- receiver ID
- transmitted max-known value
- round number
- whether the receiver updated its own max-known ID

## Election Flow

1. The user enters rows, columns, and process IDs.
2. `TorusElectionGUI` validates and parses input.
3. `TorusNetwork` creates the process grid.
4. `TorusElectionAlgorithm.electLeader()` starts with every process knowing only its own ID.
5. For each round, every process checks each of its four neighbors.
6. The neighbor's `maxKnownId` is transmitted to the current process.
7. If the transmitted value is larger, the current process updates its own `maxKnownId`.
8. Each transmission is saved as an `AnimationStep`.
9. Rounds continue until no process updates.
10. The process with the maximum original ID is marked as leader.
11. The GUI displays final status, logs, and optionally replays the recorded steps.

## Animation Flow

The election is computed before animation starts. The animation thread then iterates over the recorded `AnimationStep` list:

1. Set the active step on `TorusGridPanel`.
2. Append a log line describing the message.
3. Sleep for 1000 milliseconds.
4. Continue until all steps are shown.
5. Clear the active highlight and display the final election summary.

Animation phase meaning:

- One animation phase represents one recorded `AnimationStep`.
- The highlighted sender is the neighbor currently transmitting a value.
- The highlighted receiver is the process currently receiving that value.
- The red message marker/arrow carries the sender's current `maxKnownId`.
- If the received value is larger than the receiver's current `maxKnownId`, the receiver updates and is shown with the stronger receiver color.

Reset behavior:

- Sets `animationRunning` to `false`.
- Interrupts `animationThread` if it is alive.
- Clears the network, active highlight, status labels, and log output.

Persistent log behavior:

- GUI log text is appended to `~/.local/share/torus-election-gui/logs/torus-election-gui.log`.
- If `XDG_DATA_HOME` is set, logs are appended to `$XDG_DATA_HOME/torus-election-gui/logs/torus-election-gui.log`.
- On Windows, logs are appended to `%LOCALAPPDATA%\torus-election-gui\logs\torus-election-gui.log`.
- Reset clears only the visible log panel, not the persistent log file.

## Validation Rules

- Rows must be between `2` and `8` through the spinner model.
- Columns must be between `2` and `8` through the spinner model.
- The ID input cannot be empty.
- The number of IDs must equal `rows * columns`.
- All process IDs must be unique.
- IDs are parsed as Java `int` values.

## Build Commands

This project uses Maven and Lombok. Lombok is configured as a `provided` dependency and annotation processor in `pom.xml`, so it is needed only while compiling and is not packaged as a runtime dependency.

Compile:

```bash
mvn compile
```

Run:

```bash
mvn compile
java -cp target/classes Main
```

Package:

```bash
mvn package
```

Run generated jar:

```bash
java -jar target/torus-election-gui-1.0.0.jar
```

For first-time setup, IDE notes, and troubleshooting, see [RUNNING.md](RUNNING.md).

For Ubuntu `.deb` and Windows `.exe` installer generation, see [PACKAGING.md](PACKAGING.md).

For method-level contracts, parameters, return values, side effects, and errors, see [METHOD_SPECIFICATIONS.md](METHOD_SPECIFICATIONS.md).

For performance, SOLID, and memory notes, see [PERFORMANCE_SOLID_MEMORY.md](PERFORMANCE_SOLID_MEMORY.md).

For Snapcraft packaging and Snap Store publishing, see [SNAP_PACKAGING.md](SNAP_PACKAGING.md).

## Maintenance Notes

- Maven owns compilation and jar packaging through `pom.xml`.
- Lombok generates simple constructors, selected getters/setters, and equality methods at compile time.
- Network topology lists are cached and exposed as unmodifiable views to avoid repeated allocation and accidental external mutation.
- Swing UI updates should happen on the Event Dispatch Thread.
- Long-running animation work should stay outside the Event Dispatch Thread.
- `TorusElectionAlgorithm` currently mutates `ProcessNode.maxKnownId`; create a fresh `TorusNetwork` for each run.
- The visualization layout reserves footer space to prevent torus wrap lines from overlapping footer text.
- The leader is the node with the maximum original process ID, so duplicate IDs are rejected by the GUI.
