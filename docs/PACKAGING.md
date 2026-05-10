# Packaging Native Installers

This project can be packaged as native desktop installers with `jpackage`, which is included with JDK 17 and later.

`jpackage` builds installers for the operating system it is running on. Build Ubuntu `.deb` packages on Ubuntu or another Debian-based Linux system. Build Windows `.exe` installers on Windows.

For Snapcraft packaging and Snap Store publishing, see [SNAP_PACKAGING.md](SNAP_PACKAGING.md).

## Prerequisites

- JDK 17 or later
- Maven 3.8 or later
- A successful Maven package build

Build the runnable jar first:

```bash
mvn package
```

The jar should be created at:

```text
target/torus-election-gui-1.0.0.jar
```

## Ubuntu `.deb` Installer With Maven

The POM includes `org.panteleyev:jpackage-maven-plugin` in the `linux-installer` profile. Build the `.deb` directly with Maven:

```bash
mvn clean package -Plinux-installer
```

The generated package is written to:

```text
target/installer/
```

Install the generated package:

```bash
sudo apt install ./target/installer/torus-election-gui_1.0.0-1_amd64.deb
```

The profile runs the same `jpackage` settings that are shown below, but keeps them in `pom.xml`.

The installed launcher uses this icon:

```text
assets/icons/torus-election-gui.png
```

## Ubuntu `.deb` Installer With `jpackage`

Install the Linux packaging tools if needed:

```bash
sudo apt update
sudo apt install fakeroot dpkg-dev
```

Create the installer:

```bash
mkdir -p target/installer

jpackage \
  --type deb \
  --name torus-election-gui \
  --app-version 1.0.0 \
  --input target \
  --main-jar torus-election-gui-1.0.0.jar \
  --main-class Main \
  --icon assets/icons/torus-election-gui.png \
  --dest target/installer \
  --linux-menu-group "Education" \
  --linux-shortcut \
  --linux-deb-maintainer "you@example.com" \
  --add-modules java.desktop
```

Install the generated package:

```bash
sudo apt install ./target/installer/torus-election-gui_1.0.0-1_amd64.deb
```

Run the installed app from the desktop menu or from the terminal:

```bash
torus-election-gui
```

Uninstall it:

```bash
sudo apt remove torus-election-gui
```

Remove package configuration files too:

```bash
sudo apt purge torus-election-gui
```

The `.deb` includes a `postrm` purge hook. `apt purge` removes the default log directories for `/root` and `/home/*`:

```bash
~/.local/share/torus-election-gui
```

If a user ran the app with a custom `XDG_DATA_HOME`, delete that custom location manually if needed:

```bash
rm -rf "$XDG_DATA_HOME/torus-election-gui"
```

## Windows `.exe` Installer

The POM includes a `windows-installer` profile. Run this on Windows with JDK 17 or later installed:

```powershell
mvn clean package -Pwindows-installer
```

The generated installer is written to:

```text
target\installer\
```

The installed launcher uses this icon:

```text
assets\icons\torus-election-gui.ico
```

Install it by running the generated `.exe`.

Uninstall it from Windows:

```text
Settings -> Apps -> Installed apps -> TorusElectionGUI -> Uninstall
```

Or from Control Panel:

```text
Control Panel -> Programs -> Programs and Features -> TorusElectionGUI -> Uninstall
```

Application logs are user data and are not removed automatically. Delete them manually if needed:

```powershell
Remove-Item -Recurse -Force "$env:LOCALAPPDATA\torus-election-gui"
```

Windows installer notes:

- Build the `.exe` on Windows. `jpackage` cannot create a Windows installer from Linux.
- Install WiX Toolset if your JDK/jpackage setup requires it for `.exe` generation.
- The Maven profile requests Start Menu and desktop shortcuts.
- The Maven profile requests per-user installation and an installation-directory chooser.

## Windows `.exe` Installer With `jpackage`

Run these manual commands on Windows with JDK 17 or later installed:

```powershell
mvn package
mkdir target\installer

jpackage `
  --type exe `
  --name TorusElectionGUI `
  --app-version 1.0.0 `
  --input target `
  --main-jar torus-election-gui-1.0.0.jar `
  --main-class Main `
  --icon assets\icons\torus-election-gui.ico `
  --dest target\installer `
  --win-menu `
  --win-menu-group "Torus Election GUI" `
  --win-shortcut `
  --win-dir-chooser `
  --win-per-user-install `
  --add-modules java.desktop
```

The installer should be created under:

```text
target\installer\
```

Install it by running the generated `.exe`.

## Notes

- The app uses Swing, so `java.desktop` is required.
- Lombok is compile-time only and is not needed by the installed application.
- Linux runtime logs are written to `~/.local/share/torus-election-gui/logs/torus-election-gui.log`, or to `$XDG_DATA_HOME/torus-election-gui/logs/torus-election-gui.log` when `XDG_DATA_HOME` is set.
- The Linux `.deb` purge script removes only the default `~/.local/share/torus-election-gui` paths. Custom `XDG_DATA_HOME` paths must be removed manually.
- Windows runtime logs are written to `%LOCALAPPDATA%\torus-election-gui\logs\torus-election-gui.log`.
- If `jpackage` is not found, check that your `JAVA_HOME` points to a JDK, not a JRE, and that the JDK `bin` directory is on `PATH`.
- If `.deb` generation fails because of missing tools, install `fakeroot` and `dpkg-dev`.
