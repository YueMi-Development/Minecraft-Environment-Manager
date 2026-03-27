plugins {
    `java-library`
}

val pluginName: String by project

dependencies {
    implementation("net.bytebuddy:byte-buddy:1.15.1")
    implementation(project(":core-api"))
}

tasks.jar {
    archiveBaseName.set("$pluginName-agent")
    
    manifest {
        attributes(
            "Premain-Class" to "org.yuemi.environmentmanager.agent.AgentInstaller",
            "Can-Redefine-Classes" to "true",
            "Can-Retransform-Classes" to "true"
        )
    }

    // Shadow/Fat JAR logic if needed, but for now we expect dependencies to be provided 
    // or we can use the shadow plugin. Actually, ByteBuddy needs to be available at runtime.
    // For an agent, it's best to shade dependencies to avoid conflicts with the app.
}
