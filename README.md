# RustyJavaC Gradle Plugin
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
    mainClass.set("com.example.Main")  // optional, for executable JAR
    javaVersion.set(25)
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
| `javaVersion` | `Property<Int>` | `25` | Target Java version |
| `mainClass` | `Property<String>` | `""` | Main class for executable JAR manifest |

## Requirements
- Java 25+
- Kotlin 1.9+
- Gradle 9+

## License

[MIT](LICENSE)
