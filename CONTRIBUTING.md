# Contributing to RustyJavaC Gradle Plugin

Thanks for being interested in contributing! This plugin is part of the Rusty-JavaC ecosystem — it bridges the Rust-based compiler with the Gradle build system.

## Getting Started

### Prerequisites

- **Java 17+**
- **Kotlin 1.9+**
- **Gradle 8+** (use the included wrapper: `./gradlew`)
- **Git**

### Building

```bash
git clone https://github.com/CoffeePopStudio/RustyJavaC-Gradle-Plugin.git
cd RustyJavaC-Gradle-Plugin
./gradlew build
```

### Publishing Locally

```bash
./gradlew publishToMavenLocal
```

This publishes the plugin to your local Maven repository so you can test it in other projects.

### Running the Demo

The `demo/` directory contains a sample project that uses the plugin:

```bash
./gradlew -p demo rustyJavaCJar
```

This compiles the demo Java sources with RustyJavaC and produces a JAR. To run it:

```bash
java -jar demo/build/libs/rustyjavac-demo.jar
```

## Project Structure

```
├── build.gradle.kts          # Plugin build configuration
├── settings.gradle.kts
├── gradle/wrapper/           # Gradle wrapper files
├── src/main/kotlin/
│   └── com/rustyjavac/gradle/
│       ├── RustyJavaCPlugin.kt      # Plugin: registers tasks when 'java' plugin is applied
│       ├── RustyJavaCExtension.kt   # DSL extension: command, javaVersion, mainClass
│       └── CompileRustyJavaCTask.kt # Task: invokes RustyJavaC CLI, writes .class files
├── demo/                     # Demo project consuming the plugin
│   ├── build.gradle.kts
│   └── src/main/java/...     # Sample Java source files
└── gradlew / gradlew.bat
```

## Development Workflow

1. **Fork and clone** the repo.
2. **Create a branch** from `main` for your changes. Use a descriptive name like `feat/source-set-support` or `fix/manifest-encoding`.
3. **Make your changes.** Keep commits focused — one logical change per commit.
4. **Run `./gradlew build`** to make sure everything compiles.
5. **Test locally** by publishing to `mavenLocal` and running the demo project.
6. **Open a pull request** against `main`.

## What to Work On

- **Incremental compilation** — Currently all sources are recompiled every time.
- **Test source set support** — The plugin skips `test` sources; adding support would be valuable.
- **Configuration caching** — The task is marked `@DisableCachingByDefault`. Supporting caching for idempotent builds would improve performance.
- **Upstream compiler integration** — Improving the `command` configuration to work more seamlessly with `cargo run` workflows.
- **Tests** — Unit tests for the plugin logic.
- **Documentation** — Doc comments on public API, usage examples, etc.

## Code Style

- Use Kotlin Gradle DSL throughout.
- Follow standard Kotlin conventions (default IntelliJ formatter settings).
- Keep the task implementation self-contained and straightforward.
- Prefer Gradle's lazy configuration APIs (`Property`, `ListProperty`, `DirectoryProperty`) over eager evaluation.

## Commit Messages

We follow [Conventional Commits](https://www.conventionalcommits.org/):

```
type(scope): short description
```

| Type | When to use |
|------|-------------|
| `feat` | A new feature or capability |
| `fix` | A bug fix |
| `refactor` | Code restructuring without behavior change |
| `docs` | Documentation only |
| `chore` | Maintenance, build config, etc. |

Examples:

```
feat(plugin): add mainClass configuration for executable JAR
fix(task): handle empty source directories
refactor(extension): use ListProperty for command
chore: remove demo wrapper, use root wrapper instead
```

## License

By contributing, you agree that your contributions will be licensed under the [MIT License](LICENSE), same as the rest of the project.

---

Thanks for contributing! Even small PRs make a difference at this stage of the project.
