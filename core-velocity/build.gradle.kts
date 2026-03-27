plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val pluginName: String by project
val developerName: String by project
val pluginVersion: String = project.version.toString()

dependencies {
    implementation(project(":core-api"))
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.jar {
    archiveBaseName.set("$pluginName-velocity")
    archiveVersion.set(pluginVersion)
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("$pluginName-velocity")
    archiveVersion.set(pluginVersion)
    archiveClassifier.set("")
}

tasks.build {
    dependsOn(tasks.named("shadowJar"))
}
