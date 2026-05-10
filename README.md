# Torus Network Leader Election GUI

A Java Swing desktop application for visualizing leader election in a two-dimensional torus network. The application lets you configure a grid of processes, enter unique process IDs, run the election immediately, or animate message propagation step by step.

## Requirements

- JDK 17 or later
- Maven 3.8 or later
- Internet access on the first Maven build so Maven can download Lombok

## Compile

```bash
mvn compile
```

## Run

```bash
mvn compile
java -cp target/classes Main
```

## Package

Build the runnable jar:

```bash
mvn package
```

Run the generated jar:

```bash
java -jar target/torus-election-gui-1.0.0.jar
```

## Run Documentation

For a fuller first-time setup guide, IDE notes, and troubleshooting, see [docs/RUNNING.md](docs/RUNNING.md).

## Installer Packaging

For Ubuntu `.deb` and Windows `.exe` installer commands, see [docs/PACKAGING.md](docs/PACKAGING.md).

On Ubuntu, the `.deb` installer can be generated directly with Maven:

```bash
mvn clean package -Plinux-installer
```

The installed launcher icon is configured from `assets/icons/torus-election-gui.png`.

On Windows, the `.exe` installer can be generated directly with Maven from a Windows machine:

```powershell
mvn clean package -Pwindows-installer
```

The Windows launcher icon is configured from `assets/icons/torus-election-gui.ico`.

For Snapcraft packaging and Snap Store publishing, see [docs/SNAP_PACKAGING.md](docs/SNAP_PACKAGING.md).

## GitHub Pages

This repository includes a static project page at [docs/index.html](docs/index.html), plus HTML versions of the docs for browser-friendly GitHub Pages navigation.

After uploading the repo to GitHub:

1. Open repository `Settings`.
2. Go to `Pages`.
3. Set `Source` to `Deploy from a branch`.
4. Select the branch, usually `main`.
5. Select the `/docs` folder.
6. Save.

GitHub Pages will serve the site from the `docs` folder.

## Usage

1. Choose the number of rows and columns. Both values must be at least `2`.
2. Enter one unique process ID for every grid cell.
3. Click `Run Election` to execute the algorithm and display the final leader.
4. Click `Auto Animate` to replay the election messages with sender and receiver highlighting.
5. Click `Reset` to clear the visualization, stop any running animation, and return the UI to its initial state.

## Logs

The visible execution log is also appended to a persistent per-user log file:

```text
~/.local/share/torus-election-gui/logs/torus-election-gui.log
```

If `XDG_DATA_HOME` is set, the log file is stored under:

```text
$XDG_DATA_HOME/torus-election-gui/logs/torus-election-gui.log
```

Reset clears only the visible log panel. It does not delete the persistent log file.

On Windows, logs are stored under:

```text
%LOCALAPPDATA%\torus-election-gui\logs\torus-election-gui.log
```

On Ubuntu, uninstall removes the application:

```bash
sudo apt remove torus-election-gui
```

Purge removes the application plus the default Linux log directories:

```bash
sudo apt purge torus-election-gui
```

Example input for a `4 x 4` torus:

```text
12 5 33 8
17 40 2 29
11 6 55 21
9 14 31 25
```

## Architecture

The project is organized as a small layered Swing application:

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

The architecture separates the UI, network model, algorithm, and animation state:

- `Main` starts the Swing application on the Event Dispatch Thread.
- `gui` owns all desktop UI behavior, input validation, drawing, animation, and status output.
- `logging` appends GUI log text to a persistent per-user log file.
- `network` builds the torus topology and resolves each process node's four wrap-around neighbors.
- `algorithm` executes the leader election and records animation/log data.
- `model` contains the data objects shared between the algorithm, network, and UI.
- Lombok generates simple constructors, getters, setters, and equality methods at compile time.

### Runtime Flow

```text
User input
   |
   v
TorusElectionGUI parses rows, columns, and IDs
   |
   v
TorusNetwork creates ProcessNode grid with wrap-around topology
   |
   v
TorusElectionAlgorithm propagates max-known IDs until stable
   |
   v
AnimationStep records are generated for visualization
   |
   v
TorusGridPanel renders nodes, links, messages, leader, and footer
```

## Algorithm Summary

Each process starts with its own ID as `maxKnownId`. During each round, every process receives the `maxKnownId` values known by its four torus neighbors: right, left, down, and up. If a received value is larger than the process's current `maxKnownId`, the process updates its value.

Rounds continue until a complete pass finishes with no changes. At that point the maximum process ID has propagated through the network, and the process with that ID is marked as leader.

The algorithm records:

- total rounds
- total messages exchanged
- a textual execution log
- animation steps containing sender, receiver, transmitted value, round number, and whether the receiver updated

## UI Components

- Network configuration panel: row, column, and total process count.
- Process ID panel: row-wise ID input.
- Visualization panel: torus graph, process state, animated messages, wrap-around footer.
- Election status panel: leader ID, leader position, rounds, messages, start time, and end time.
- Legend panel: color meaning for process, sender, receiver, and leader.
- Controls panel: run, animate, and reset actions.
- Execution log panel: detailed round and summary output.

## Documentation

See [docs/PROJECT_DOCUMENTATION.md](docs/PROJECT_DOCUMENTATION.md) for fuller implementation documentation, including class responsibilities, data flow, rendering behavior, validation rules, and maintenance notes.

See [docs/METHOD_SPECIFICATIONS.md](docs/METHOD_SPECIFICATIONS.md) for method-level contracts across the codebase.

See [docs/PERFORMANCE_SOLID_MEMORY.md](docs/PERFORMANCE_SOLID_MEMORY.md) for the performance, SOLID, and memory review.
