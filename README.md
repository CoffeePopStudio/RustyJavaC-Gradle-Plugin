# RustyJavaC Gradle Plugin

> [!WARNING]
> This project is still under heavy development. RustyJavaC itself is not yet a complete Java compiler.

Gradle plugin that integrates [Rusty-JavaC](https://github.com/Eatgrapes/Rusty-JavaC) (a Rust-based Java compiler) into your Gradle build. It compiles Java sources using the RustyJavaC CLI and assembles the resulting `.class` files into JARs.

## Features

- Compiles Java source files with RustyJavaC
- Produces standard JAR files with automatic `Main-Class` manifest
- Supports multi-module source sets
- Configurable via a `rustyJavaC {}` DSL block

## Quick Start

### 1. Apply the plugin

```kotlin
// build.gradle.kts
plugins {
    id("java")
    id("com.rustyjavac") version "0.1.0"
}
```

### 2. Configure

```kotlin
rustyJavaC {
    command.set(listOf("rustyjavac"))  // path to the RustyJavaC binary
    mainClass.set("com.example.Main")  // optional, for executable JAR
    javaVersion.set(21)
}
```

### 3. Build

```bash
./gradlew rustyJavaCJar
```

This creates `build/libs/<project-name>.jar` using only RustyJavaC for compilation.

## Tasks

| Task | Description |
|------|-------------|
| `compileRustyJavaC` | Compiles main Java sources with RustyJavaC |
| `rustyJavaCJar` | Assembles a JAR from the compiled classes |

## Extension Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `command` | `ListProperty<String>` | `["rustyjavac"]` | Command to invoke the RustyJavaC compiler |
| `javaVersion` | `Property<Int>` | `21` | Target Java version |
| `mainClass` | `Property<String>` | `""` | Main class for executable JAR manifest |

## Requirements

- Java 17+
- Kotlin 1.9+
- Gradle 8+
- [Rusty-JavaC](https://github.com/Eatgrapes/Rusty-JavaC) compiler binary accessible on `PATH`

## Project Structure

```
├── build.gradle.kts          # Plugin build configuration
├── settings.gradle.kts
├── src/main/kotlin/...        # Plugin source code
│   ├── RustyJavaCPlugin.kt    # Plugin entry point
│   ├── RustyJavaCExtension.kt # DSL extension
│   └── CompileRustyJavaCTask.kt # Compilation task
├── demo/                      # Demo project using the plugin
│   ├── build.gradle.kts
│   └── src/main/java/...      # Sample Java sources
├── gradlew / gradlew.bat
└── .gitignore
```

## License

[MIT](LICENSE)
