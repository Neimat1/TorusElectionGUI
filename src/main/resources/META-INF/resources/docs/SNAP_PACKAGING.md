# Quarkus Packaging Notes

The previous Snap packaging notes belonged to the pre-migration UI. The main packaging target is now the Quarkus web application.

## Build Locally

```bash
mvn package
```

## Run The Packaged App

```bash
java -jar target/quarkus-app/quarkus-run.jar
```

## Output

The packaged application is written to:

```text
target/quarkus-app
```

That folder contains the runnable Quarkus application, dependencies, and static web assets under `META-INF/resources`.

## Future Packaging

If a distributable installer is needed later, prefer one of these Quarkus-compatible paths:

- container image
- native executable
- platform service wrapper

Snap packaging can still be reintroduced, but it should wrap the Quarkus runtime instead of the old desktop launcher.
