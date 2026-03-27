plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val pluginName: String by project
val developerName: String by project
val pluginVersion: String = project.version.toString()

dependencies {
    implementation(project(":core-api"))
    compileOnly("net.md-5:bungeecord-api:1.20-R0.1-SNAPSHOT")
    implementation("net.kyori:adventure-platform-bungeecord:4.3.4")
    implementation("net.kyori:adventure-text-serializer-bungeecord:4.3.4")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.jar {
    archiveBaseName.set("$pluginName-bungee")
    archiveVersion.set(pluginVersion)
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("$pluginName-bungee")
    archiveVersion.set(pluginVersion)
    archiveClassifier.set("")
}

tasks.build {
    dependsOn(tasks.named("shadowJar"))
}
