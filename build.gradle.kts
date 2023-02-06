import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.util.Properties

val versionPropertiesFile = "${projectDir}/project.properties"

fun String.runCommand(currentWorkingDir: File = file("./")): String {
    val byteOut = ByteArrayOutputStream()
    project.exec {
        workingDir = currentWorkingDir
        commandLine = this@runCommand.split("\\s".toRegex())
        standardOutput = byteOut
    }
    return String(byteOut.toByteArray()).trim()
}

fun getRevision(): String {
    return "git rev-parse --short=7 HEAD".runCommand()
}

fun getProperties(file: String, key: String): String {
    val fileInputStream = FileInputStream(file)
    val props = Properties()
    props.load(fileInputStream)
    return props.getProperty(key)
}

fun getVersion(): String {
    return getProperties(versionPropertiesFile, "version")
}

fun getStage(): String {
    return getProperties(versionPropertiesFile, "stage")
}

plugins {
    kotlin("jvm") version "1.7.20"
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "org.qianq"
version = getVersion() + "-" + getStage() + "+" + getRevision()

tasks {
    val projectProps by registering(WriteProperties::class) {
        outputFile = file("${projectDir}/src/main/resources/version.properties")
        encoding = "UTF-8"
        property("version", getVersion())
        property("stage", getStage())
        property("revision", getRevision())
    }

    var shadowJarVersion = getVersion()
    shadowJar {
        if (getStage() == "dev" || getStage() == "alpha" || getStage() == "beta" || getStage() == "rc") {
            shadowJarVersion = shadowJarVersion + "-" + getStage()
        }
        shadowJarVersion = shadowJarVersion + "+" + getRevision()
        destinationDirectory.set(file("${projectDir}/build/distributions"))
        archiveVersion.set(shadowJarVersion)
        archiveClassifier.set("")
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(projectProps)
    }
}

application {
    mainClass.set("org.qianq.qlox.QLox")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
