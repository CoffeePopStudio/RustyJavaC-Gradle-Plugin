plugins {
    id("java")
    id("com.rustyjavac") version "0.1.0"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
}

rustyJavaC {
    mainClass.set("com.example.HelloWorld")

    // 使用 Panama FFM 模式（需提供 rustyjavac_native.dll/.so/.dylib 路径）：
    // nativeLibPath.set("/path/to/rustyjavac_native")

    // 不设 nativeLibPath 则走 CLI 模式（需 PATH 上有 rustyjavac 二进制）：
    // command.set(listOf("rustyjavac"))

    javaVersion.set(25)
}

tasks.register("runDemo", JavaExec::class) {
    dependsOn("rustyJavaCJar")
    classpath = files(tasks.named("rustyJavaCJar"))
    mainClass.set(rustyJavaC.mainClass)
    group = "rustyjavac"
    description = "Runs the demo using RustyJavaC-compiled JAR"
}
