# Method Specifications

This document describes the public, protected, package-private, private, and Lombok-generated methods in the Java source code.

## `Main`

Source: `src/main/java/Main.java`

### `main(String[] args)`

- Visibility: `public static`
- Purpose: Application entry point.
- Parameters: `args` is accepted by the JVM but not used.
- Returns: Nothing.
- Side effects: Schedules `TorusElectionGUI` creation on the Swing Event Dispatch Thread with `SwingUtilities.invokeLater`.
- Errors: Does not perform explicit error handling.

## `algorithm.TorusElectionAlgorithm`

Source: `src/main/java/algorithm/TorusElectionAlgorithm.java`

### `TorusElectionAlgorithm(TorusNetwork network)`

- Visibility: `public`
- Purpose: Creates an election runner for a torus network.
- Parameters: `network` is the topology and process state used by the election.
- Returns: A new `TorusElectionAlgorithm` instance.
- Side effects: Stores the network reference. It does not clone the network.
- Constraints: Callers should pass a fresh `TorusNetwork` for each independent run because the election mutates `ProcessNode.maxKnownId` and leader state.

### `electLeader()`

- Visibility: `public`
- Purpose: Runs the leader election until all nodes converge on the maximum process ID.
- Parameters: None.
- Returns: Nothing.
- Side effects: Clears previous animation/log data, resets round and message counters, resets node election state, updates each node's `maxKnownId`, records `AnimationStep` entries, records execution log lines, and marks the elected leader node.
- Algorithm: Repeatedly lets every process receive each neighbor's `maxKnownId`; when no process updates during a round, the election stops.
- Errors: Relies on the network being valid. It does not validate duplicate IDs; the GUI validates that before creating the network.

### `findMaximumId()`

- Visibility: `private`
- Purpose: Finds the maximum original process ID in the network.
- Parameters: None.
- Returns: The largest `ProcessNode.id` value.
- Side effects: None.
- Constraints: Assumes the network has at least one node.

### Accessors

- `getAnimationSteps()`: returns an unmodifiable view of the recorded animation steps.
- `getExecutionLog()`: returns an unmodifiable view of the execution log.
- `getRounds()`: returns the number of completed rounds.
- `getMessages()`: returns the number of message exchanges.

## `network.TorusNetwork`

Source: `src/main/java/network/TorusNetwork.java`

### `TorusNetwork(int rows, int cols, int[] ids)`

- Visibility: `public`
- Purpose: Builds a two-dimensional torus network from row/column dimensions and process IDs.
- Parameters: `rows` is the row count, `cols` is the column count, and `ids` is the row-major process ID list.
- Returns: A new `TorusNetwork` instance.
- Side effects: Allocates a `ProcessNode[][]` grid and creates one `ProcessNode` per ID.
- Errors: Throws `IllegalArgumentException` when `rows < 2`, `cols < 2`, or `ids.length != rows * cols`.

### `getNode(int row, int col)`

- Visibility: `public`
- Purpose: Returns the node at a grid coordinate.
- Parameters: `row` and `col` are zero-based indexes.
- Returns: The `ProcessNode` stored at `nodes[row][col]`.
- Side effects: None.
- Errors: May throw `ArrayIndexOutOfBoundsException` if indexes are outside the grid.

### `getNode(Position position)`

- Visibility: `public`
- Purpose: Returns the node at a `Position`.
- Parameters: `position` supplies zero-based row and column indexes.
- Returns: The matching `ProcessNode`.
- Side effects: None.
- Errors: May throw `NullPointerException` for a null position or `ArrayIndexOutOfBoundsException` for an out-of-range position.

### `getAllNodes()`

- Visibility: `public`
- Purpose: Returns all nodes in row-major order.
- Parameters: None.
- Returns: An unmodifiable row-major `List<ProcessNode>` containing references to every node.
- Side effects: None. The list is cached by the network.

### `getNeighbors(ProcessNode node)`

- Visibility: `public`
- Purpose: Returns the four torus neighbors of a process.
- Parameters: `node` is the process whose neighbors should be resolved.
- Returns: An unmodifiable cached list ordered as right, left, down, up.
- Side effects: None.
- Errors: Throws `IllegalArgumentException` when the node does not belong to this network.
- Torus rule: Uses modular arithmetic so the first and last rows/columns wrap around.

### Accessors

- `getRows()`: returns the row count.
- `getCols()`: returns the column count.

## `model.ProcessNode`

Source: `src/main/java/model/ProcessNode.java`

### `ProcessNode(int id, Position position)`

- Visibility: `public`
- Purpose: Creates a process with an immutable ID and position.
- Parameters: `id` is the unique process ID, and `position` is its grid position.
- Returns: A new `ProcessNode`.
- Side effects: Initializes `maxKnownId` to `id` and `leader` to `false`.

### `updateMaxKnownId(int receivedId)`

- Visibility: `public`
- Purpose: Updates the process's known maximum ID when a larger value is received.
- Parameters: `receivedId` is the value received from a neighbor.
- Returns: Nothing.
- Side effects: Mutates `maxKnownId` only if `receivedId > maxKnownId`.

### `resetElectionState()`

- Visibility: `public`
- Purpose: Restores the node to its pre-election state.
- Parameters: None.
- Returns: Nothing.
- Side effects: Sets `maxKnownId` back to `id` and clears the leader flag.

### Lombok-Generated Accessors

`@Getter` generates these methods:

- `getId()`: returns the original process ID.
- `getPosition()`: returns the process position.
- `getMaxKnownId()`: returns the current maximum known ID.
- `isLeader()`: returns whether the process is marked as leader.

`@Setter` on `leader` generates:

- `setLeader(boolean leader)`: sets the elected-leader flag.

## `model.Position`

Source: `src/main/java/model/Position.java`

### `Position(int row, int col)`

- Visibility: `public`
- Purpose: Creates an immutable grid position.
- Parameters: `row` and `col` are zero-based coordinates.
- Returns: A new `Position`.
- Side effects: None after construction.

### `toString()`

- Visibility: `public`
- Purpose: Formats the position for logs and status labels.
- Parameters: None.
- Returns: A string in the form `(row, col)`.
- Side effects: None.

### Lombok-Generated Methods

`@Getter` generates:

- `getRow()`: returns the row index.
- `getCol()`: returns the column index.

`@EqualsAndHashCode` generates:

- `equals(Object other)`: returns `true` when `other` is a `Position` with the same row and column.
- `hashCode()`: returns a hash code based on row and column.

## `model.AnimationStep`

Source: `src/main/java/model/AnimationStep.java`

### Lombok-Generated Constructor

`@AllArgsConstructor` generates:

```java
AnimationStep(
    Position senderPosition,
    Position receiverPosition,
    int senderId,
    int receiverId,
    int transmittedValue,
    int round,
    boolean updated
)
```

- Visibility: `public`
- Purpose: Creates an immutable animation record for one message transmission.
- Parameters: Sender/receiver positions and IDs, the transmitted max-known value, the election round, and whether the receiver updated.
- Returns: A new `AnimationStep`.
- Side effects: None after construction.

### Lombok-Generated Accessors

`@Getter` generates:

- `getSenderPosition()`: returns the sender position.
- `getReceiverPosition()`: returns the receiver position.
- `getSenderId()`: returns the sender process ID.
- `getReceiverId()`: returns the receiver process ID.
- `getTransmittedValue()`: returns the transmitted max-known value.
- `getRound()`: returns the election round.
- `isUpdated()`: returns whether the receiver updated.

## `logging.AppLog`

Source: `src/main/java/logging/AppLog.java`

### `append(String text)`

- Visibility: `public static synchronized`
- Purpose: Appends text to the persistent application log file.
- Parameters: `text` is the exact text to append.
- Returns: Nothing.
- Side effects: Creates the log directory if needed and writes to `torus-election-gui.log`.
- Errors: Catches `IOException` and writes the failure message to standard error.

### `getLogDirectory()`

- Visibility: `public static`
- Purpose: Exposes the resolved persistent log directory.
- Parameters: None.
- Returns: The `Path` to the log directory.
- Side effects: None.

### `getLogFile()`

- Visibility: `public static`
- Purpose: Exposes the resolved persistent log file path.
- Parameters: None.
- Returns: The `Path` to `torus-election-gui.log`.
- Side effects: None.

### `resolveLogDirectory()`

- Visibility: `private static`
- Purpose: Resolves the per-user data directory used for persistent logs.
- Parameters: None.
- Returns: `%LOCALAPPDATA%\torus-election-gui\logs` on Windows, `$XDG_DATA_HOME/torus-election-gui/logs` when `XDG_DATA_HOME` is set on Unix-like systems, otherwise `~/.local/share/torus-election-gui/logs`.
- Side effects: None.

### `isWindows()`

- Visibility: `private static`
- Purpose: Detects whether the current runtime is Windows.
- Parameters: None.
- Returns: `true` when `os.name` contains `win`, otherwise `false`.
- Side effects: None.

## `gui.TorusElectionGUI`

Source: `src/main/java/gui/TorusElectionGUI.java`

### `TorusElectionGUI()`

- Visibility: `public`
- Purpose: Creates and displays the main Swing window.
- Parameters: None.
- Returns: A new `TorusElectionGUI` frame.
- Side effects: Builds the UI, registers spinner listeners, sets frame properties, and makes the frame visible.
- Threading: Should be constructed on the Swing Event Dispatch Thread.

### `createSideBar()`

- Visibility: `private`
- Purpose: Builds the left navigation/sidebar panel.
- Parameters: None.
- Returns: A configured `JPanel`.
- Side effects: Creates Swing components.

### `menuItem(String text)`

- Visibility: `private`
- Purpose: Creates a styled sidebar label.
- Parameters: `text` is the visible item label.
- Returns: A configured `JLabel`.
- Side effects: None outside the returned component.

### `createMainContent()`

- Visibility: `private`
- Purpose: Builds the central application layout.
- Parameters: None.
- Returns: A configured `JPanel`.
- Side effects: Creates and nests configuration, visualization, status, legend, controls, and log panels.

### `createConfigPanel()`

- Visibility: `private`
- Purpose: Builds row/column/total-process controls.
- Parameters: None.
- Returns: A configured `JPanel`.
- Side effects: Makes the total-process field read-only.

### `createIdsPanel()`

- Visibility: `private`
- Purpose: Builds the multiline process ID input panel.
- Parameters: None.
- Returns: A configured `JPanel`.
- Side effects: Sets the ID input font.

### `createVisualizationPanel()`

- Visibility: `private`
- Purpose: Wraps the torus drawing panel.
- Parameters: None.
- Returns: A configured `JPanel`.
- Side effects: Adds `gridPanel` to the returned panel.

### `createStatusPanel()`

- Visibility: `private`
- Purpose: Builds labels that show election status and metrics.
- Parameters: None.
- Returns: A configured `JPanel`.
- Side effects: None outside the returned component.

### `createLegendPanel()`

- Visibility: `private`
- Purpose: Builds the color legend.
- Parameters: None.
- Returns: A configured `JPanel`.
- Side effects: None outside the returned component.

### `createControlsPanel()`

- Visibility: `private`
- Purpose: Builds run, animate, and reset buttons.
- Parameters: None.
- Returns: A configured `JPanel`.
- Side effects: Registers button action listeners.

### `styleButton(JButton button)`

- Visibility: `private`
- Purpose: Applies shared size and font styling to a button.
- Parameters: `button` is the button to mutate.
- Returns: Nothing.
- Side effects: Mutates alignment, maximum size, preferred size, and font on `button`.

### `createLogPanel()`

- Visibility: `private`
- Purpose: Builds the execution log panel.
- Parameters: None.
- Returns: A configured `JPanel`.
- Side effects: Makes `logArea` read-only and sets its font.

### `card(String title)`

- Visibility: `private`
- Purpose: Creates a reusable white panel with border and title.
- Parameters: `title` is the titled-border text.
- Returns: A configured `JPanel`.
- Side effects: None outside the returned component.

### `colorBox(Color color)`

- Visibility: `private`
- Purpose: Creates a swatch label for the legend.
- Parameters: `color` is the swatch fill color.
- Returns: A configured `JLabel`.
- Side effects: None outside the returned component.

### `updateTotalProcesses()`

- Visibility: `private`
- Purpose: Recalculates `rows * cols` when spinner values change.
- Parameters: None.
- Returns: Nothing.
- Side effects: Updates `totalProcessesField`.

### `runElectionOnly()`

- Visibility: `private`
- Purpose: Executes the election immediately without replaying animation.
- Parameters: None.
- Returns: Nothing.
- Side effects: Parses user input, creates a new network and algorithm, runs the election, updates the grid, status labels, and log.
- Errors: Shows an input error dialog for parsing or validation failures.
- Guard: Returns immediately if an animation is already running.

### `runElectionWithAnimation()`

- Visibility: `private`
- Purpose: Executes the election and starts replaying recorded animation steps.
- Parameters: None.
- Returns: Nothing.
- Side effects: Parses user input, creates a new network and algorithm, runs the election, updates animation status, and starts the animation thread.
- Errors: Shows an input error dialog for parsing or validation failures.
- Guard: Returns immediately if an animation is already running.

### `parseIds(int rows, int cols)`

- Visibility: `private`
- Purpose: Parses and validates process IDs from `idsArea`.
- Parameters: `rows` and `cols` determine the required number of IDs.
- Returns: An `int[]` in row-major order.
- Side effects: None.
- Errors: Throws `IllegalArgumentException` for empty input, wrong ID count, or duplicate IDs. Throws `NumberFormatException` when a token is not a valid integer.

### `animateElection(TorusElectionAlgorithm election)`

- Visibility: `private`
- Purpose: Replays the election's recorded animation steps on a background thread.
- Parameters: `election` supplies animation steps and final summary data.
- Returns: Nothing.
- Side effects: Sets `animationRunning`, appends log lines, updates `gridPanel`, updates final status, and manages `animationThread`.
- Threading: Uses a daemon thread for delays and `SwingUtilities.invokeLater` for UI updates.
- Errors: Handles `InterruptedException` by clearing animation state and marking the thread interrupted.

### `updateStatus(TorusNetwork network, TorusElectionAlgorithm election)`

- Visibility: `private`
- Purpose: Displays final election status and metrics.
- Parameters: `network` supplies the leader node, and `election` supplies round/message counts.
- Returns: Nothing.
- Side effects: Updates status labels and final end time.

### `writeLog(TorusElectionAlgorithm election)`

- Visibility: `private`
- Purpose: Appends timestamped election log lines.
- Parameters: `election` supplies execution log entries.
- Returns: Nothing.
- Side effects: Mutates `logArea`.

### `writeFinalLog(TorusElectionAlgorithm election)`

- Visibility: `private`
- Purpose: Appends final animation summary lines.
- Parameters: `election` supplies execution log entries.
- Returns: Nothing.
- Side effects: Mutates `logArea`.

### `resetStatusOnly()`

- Visibility: `private`
- Purpose: Restores status labels and log content to their initial state.
- Parameters: None.
- Returns: Nothing.
- Side effects: Mutates labels and clears `logArea`.

### `reset()`

- Visibility: `private`
- Purpose: Stops animation and clears the current visualization state.
- Parameters: None.
- Returns: Nothing.
- Side effects: Interrupts a running animation thread, clears `currentNetwork`, clears `gridPanel`, and resets status/log fields.

### `appendLog(String text)`

- Visibility: `private`
- Purpose: Appends log text to both the visible GUI log area and the persistent application log file.
- Parameters: `text` is the exact text to append.
- Returns: Nothing.
- Side effects: Mutates `logArea` and calls `AppLog.append(text)`.

### `currentTime()`

- Visibility: `private`
- Purpose: Formats the current local time for display in logs and status labels.
- Parameters: None.
- Returns: A string formatted as `hh:mm:ss a`.
- Side effects: Reads system local time.

## `gui.TorusGridPanel`

Source: `src/main/java/gui/TorusGridPanel.java`

### `TorusGridPanel()`

- Visibility: `public`
- Purpose: Creates the custom visualization panel.
- Parameters: None.
- Returns: A new `TorusGridPanel`.
- Side effects: Sets the background color to white.

### `setNetwork(TorusNetwork network)`

- Visibility: `public`
- Purpose: Sets the network to render.
- Parameters: `network` is the torus network, or `null` to show the empty state.
- Returns: Nothing.
- Side effects: Updates internal panel state and calls `repaint()`.

### `setActiveStep(AnimationStep step)`

- Visibility: `public`
- Purpose: Sets the currently animated message.
- Parameters: `step` is the active animation step.
- Returns: Nothing.
- Side effects: Updates internal animation state and calls `repaint()`.

### `clearAnimation()`

- Visibility: `public`
- Purpose: Clears sender/receiver/message highlighting.
- Parameters: None.
- Returns: Nothing.
- Side effects: Sets `activeStep` to `null` and calls `repaint()`.

### `getPreferredSize()`

- Visibility: `public`
- Purpose: Supplies a preferred size for layout managers.
- Parameters: None.
- Returns: `Dimension(620, 500)`.
- Side effects: None.

### `paintComponent(Graphics g)`

- Visibility: `protected`
- Purpose: Draws the visualization panel.
- Parameters: `g` is the Swing graphics context.
- Returns: Nothing.
- Side effects: Paints onto the component.
- Behavior: Draws an empty prompt when no network is set; otherwise draws links, wrap links, nodes, animated message, and footer.

### `drawEmpty(Graphics2D g2)`

- Visibility: `private`
- Purpose: Draws the empty-state prompt.
- Parameters: `g2` is the 2D graphics context.
- Returns: Nothing.
- Side effects: Paints text.

### `createLayout()`

- Visibility: `private`
- Purpose: Calculates grid drawing positions based on panel size and network dimensions.
- Parameters: None.
- Returns: A `Layout` value with rows, columns, start coordinates, and gaps.
- Side effects: None.
- Constraints: Requires `network` to be non-null.

### `drawLinks(Graphics2D g2, Layout layout)`

- Visibility: `private`
- Purpose: Draws direct horizontal and vertical arrows between adjacent nodes.
- Parameters: `g2` is the graphics context, and `layout` supplies node positions.
- Returns: Nothing.
- Side effects: Paints link lines and arrowheads.

### `drawWrapLinks(Graphics2D g2, Layout layout)`

- Visibility: `private`
- Purpose: Draws dashed wrap-around arcs that represent torus connections.
- Parameters: `g2` is the graphics context, and `layout` supplies node positions.
- Returns: Nothing.
- Side effects: Paints wrap-around arcs and restores the stroke to a solid line.

### `drawNodes(Graphics2D g2, Layout layout)`

- Visibility: `private`
- Purpose: Draws every process node.
- Parameters: `g2` is the graphics context, and `layout` supplies node positions.
- Returns: Nothing.
- Side effects: Paints node boxes, IDs, max-known values, and state highlights.

### `drawAnimatedMessage(Graphics2D g2, Layout layout)`

- Visibility: `private`
- Purpose: Draws the active transmitted value between sender and receiver.
- Parameters: `g2` is the graphics context, and `layout` supplies node positions.
- Returns: Nothing.
- Side effects: Paints a red message marker when `activeStep` is non-null.

### `drawFooter(Graphics2D g2)`

- Visibility: `private`
- Purpose: Draws footer notes explaining wrap-around behavior.
- Parameters: `g2` is the graphics context.
- Returns: Nothing.
- Side effects: Paints footer background, separator lines, and text.

### `drawArrowLine(Graphics2D g2, int x1, int y1, int x2, int y2)`

- Visibility: `private`
- Purpose: Draws a line with an arrowhead.
- Parameters: `g2` is the graphics context, and `x1`, `y1`, `x2`, `y2` are start/end coordinates.
- Returns: Nothing.
- Side effects: Paints a line and two arrowhead strokes.

## `gui.TorusGridPanel.Layout`

Source: `src/main/java/gui/TorusGridPanel.java`

### `Layout(int rows, int cols, int startX, int startY, int gapX, int gapY)`

- Visibility: package-private constructor in a private static nested class.
- Purpose: Stores calculated grid layout values.
- Parameters: Row/column counts, start coordinates, and horizontal/vertical gaps.
- Returns: A new `Layout`.
- Side effects: None after construction.

### `x(int col)`

- Visibility: package-private method in a private static nested class.
- Purpose: Converts a column index to an x coordinate.
- Parameters: `col` is the zero-based column index.
- Returns: `startX + col * gapX`.
- Side effects: None.

### `y(int row)`

- Visibility: package-private method in a private static nested class.
- Purpose: Converts a row index to a y coordinate.
- Parameters: `row` is the zero-based row index.
- Returns: `startY + row * gapY`.
- Side effects: None.
