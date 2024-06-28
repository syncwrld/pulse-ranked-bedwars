import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "com.github.syncwrld.prankedbw"
version = "1.0"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    /*
    Just at compile-time
     */
    compileOnly("org.projectlombok:lombok:1.18.24")
    compileOnly("org.spigotmc:spigot:1.8.8-R0.1-SNAPSHOT")

    /*
    At runtime
     */
    implementation("net.dv8tion:JDA:5.0.0-beta.24")

    /*
    Annotation processors
     */
    annotationProcessor("org.projectlombok:lombok:1.18.24")
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

val copyJar by tasks.registering(Copy::class) {
    dependsOn(tasks.withType<ShadowJar>())
    from(tasks.shadowJar.get().archiveFile)
    into("../result")
    rename { "PackedJDA.jar" }
}

tasks.withType<ShadowJar> {
    finalizedBy(copyJar)
}