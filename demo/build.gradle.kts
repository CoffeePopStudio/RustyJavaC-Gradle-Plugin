plugins {
    id("java")
    id("com.rustyjavac") version "0.1.0"
}

repositories {
    mavenCentral()
}

rustyJavaC {
    mainClass.set("com.example.HelloWorld")
    javaVersion.set(21)
}

tasks.register("runDemo", JavaExec::class) {
    dependsOn("rustyJavaCJar")
    classpath = files(tasks.named("rustyJavaCJar"))
    mainClass.set(rustyJavaC.mainClass)
    group = "rustyjavac"
    description = "Runs the demo using RustyJavaC-compiled JAR"
}
