# Running the Repository

This project is a Java 17 Swing application built with Maven.

## Prerequisites

- JDK 17 or later
- Maven 3.8 or later
- Internet access for the first build so Maven can download Lombok

Check your local tools:

```bash
java -version
mvn -version
```

## Compile

From the repository root:

```bash
mvn compile
```

Maven downloads Lombok on the first run and uses it as an annotation processor during compilation.

## Run From Compiled Classes

```bash
mvn compile
java -cp target/classes Main
```

The Swing window should open with a default `4 x 4` torus input.

## Build And Run The Jar

Build the runnable jar:

```bash
mvn package
```

Run it:

```bash
java -jar target/torus-election-gui-1.0.3.jar
```

## Native Installers

To generate Ubuntu `.deb` or Windows `.exe` installers, see [PACKAGING.md](PACKAGING.md).

The Ubuntu installer can be generated directly through Maven:

```bash
mvn clean package -Plinux-installer
```

The Windows installer can be generated directly through Maven from a Windows machine:

```powershell
mvn clean package -Pwindows-installer
```

Snap packaging is documented in [SNAP_PACKAGING.md](SNAP_PACKAGING.md).

## IntelliJ IDEA

1. Open the repository as a Maven project.
2. Use JDK 17 or later for the project SDK.
3. Let IntelliJ import Maven dependencies.
4. Ensure annotation processing is enabled if IntelliJ does not detect Lombok automatically:

```text
Settings -> Build, Execution, Deployment -> Compiler -> Annotation Processors
```

5. Run the `Main` class.

## Troubleshooting

- If Maven cannot resolve `org.projectlombok:lombok`, check network access and rerun `mvn compile`.
- If the IDE shows missing Lombok-generated methods but Maven compiles successfully, enable annotation processing in the IDE.
- If the GUI does not open in a remote or headless environment, run it on a machine with a desktop display server.
