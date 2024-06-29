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
    compileOnly("org.spigotmc:spigot:1.8.8-R0.1-SNAPSHOT")
    compileOnly("org.apache.logging.log4j:log4j-slf4j18-impl:2.18.0")

    /*
    At runtime
     */
    implementation("com.discord4j:discord4j-core:3.2.6" )
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    sourceCompatibility = "1.8"
}

val copyJar by tasks.registering(Copy::class) {
    dependsOn(tasks.withType<ShadowJar>())
    from(tasks.shadowJar.get().archiveFile)
    into("../artifacts")
    rename { "PackedD4J.jar" }
}

tasks.withType<ShadowJar> {
    finalizedBy(copyJar)
}