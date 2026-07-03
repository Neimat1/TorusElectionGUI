# Packaging And Deployment

The migrated project is packaged as a Quarkus application.

```bash
mvn package
java -jar target/quarkus-app/quarkus-run.jar
```

The primary deployment artifact is target/quarkus-app. Native image or container packaging can be added later if required.
