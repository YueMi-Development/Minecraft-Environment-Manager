plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    java
}

dependencies {
    implementation(project(":core-api"))
    implementation(project(":core-bukkit"))
    implementation(project(":core-bungeecord"))
    implementation(project(":core-velocity"))
}

tasks.shadowJar {
    dependsOn(":core-bukkit:shadowJar", ":core-bungeecord:shadowJar", ":core-velocity:shadowJar")
    archiveBaseName.set(project.property("pluginName") as String)
    archiveClassifier.set("universal")
    archiveVersion.set(project.version.toString())
    
    // Merge service files and other resources
    mergeServiceFiles()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
