# Election Status Logging

The Quarkus migration keeps election status visible in two places:

- the `Election Status` panel in the browser UI
- the execution log returned by the `/api/election` resource

## Status Metrics

Each completed election response includes:

- status
- leader ID
- leader position
- rounds checked
- messages exchanged
- start time
- end time
- execution log lines
- animation steps for the visualization

## Runtime Flow

1. The browser sends the grid size and process IDs to `/api/election`.
2. `ElectionResource` receives the request and delegates to `ElectionService`.
3. The service validates the request, builds the torus network, runs the leader election algorithm, and maps the result.
4. The browser updates the status card, execution log, and torus visualization from the JSON response.

## Example Log Output

```text
[09:28:10 AM] Initialization complete. Total processes: 16
[09:28:10 AM] Starting leader election.
[09:28:10 AM] Round 1 completed. Status: 4 nodes updated, 64 messages exchanged.
[09:28:10 AM] Round 2 completed. Status: 11 nodes updated, 64 messages exchanged.
[09:28:10 AM] Round 3 completed (convergence check). Status: No updates detected - election converged.
[09:28:10 AM] Election completed. Leader is Process ID: 100 at position (0, 0). Total rounds: 3, Total messages: 192
```

## Verify In The Web App

Run the Quarkus app:

```bash
mvn quarkus:dev
```

Open:

```text
http://localhost:8080/
```

Run or animate an election and check the `Election Status` and `Execution Log` cards.
