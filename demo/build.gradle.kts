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
    javaVersion.set(25)
}

tasks.register("runDemo", JavaExec::class) {
    dependsOn("rustyJavaCJar")
    classpath = files(tasks.named("rustyJavaCJar"))
    mainClass.set(rustyJavaC.mainClass)
    group = "rustyjavac"
    description = "Runs the demo using RustyJavaC-compiled JAR"
}
