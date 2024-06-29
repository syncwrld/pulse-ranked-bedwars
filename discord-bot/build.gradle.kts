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
    maven("https://jitpack.io")
}

dependencies {
    /*
    Just at compile-time
     */
    compileOnly("org.projectlombok:lombok:1.18.24")
    compileOnly("org.spigotmc:spigot:1.8.8-R0.1-SNAPSHOT")
    compileOnly(files("../artifacts/PackedJC.jar"))

    /*
    At runtime
     */
    implementation("com.github.syncwrld:syncBooter:v0.1.4.5")

    /*
    Annotation processors
     */
    annotationProcessor("org.projectlombok:lombok:1.18.24")
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
    minimize()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

val copyJar by tasks.registering(Copy::class) {
    dependsOn(tasks.withType<ShadowJar>())
    from(tasks.shadowJar.get().archiveFile)
    into("../artifacts")
    rename { "PRanked - 4s Bot.jar" }
}

tasks.withType<ShadowJar> {
    finalizedBy(copyJar)
}