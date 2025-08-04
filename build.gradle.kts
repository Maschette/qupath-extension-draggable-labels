import java.util.concurrent.TimeUnit

plugins {
    id("java-library")
    id("maven-publish")
    id("org.openjfx.javafxplugin")
    id("com.gradleup.shadow") version "8.3.5"
    id("qupath-conventions")
}

qupathExtension {
    name = "qupath-extension-draggable-labels"
    group = "io.github.qupath"
    version = "1.0.0"
    description = "QuPath extension for draggable annotation labels"
    automaticModule = "qupath.extension.draggablelabels"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
    }
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
    
    maven {
        name = "SciJava"
        url = uri("https://maven.scijava.org/content/repositories/releases")
    }
    
    maven {
        name = "SciJava snapshots"
        url = uri("https://maven.scijava.org/content/repositories/snapshots")
    }
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
}

val qupathVersion: String by project

dependencies {
    // Use compileOnly to avoid bundling QuPath dependencies
    compileOnly("io.github.qupath:qupath-gui-fx:$qupathVersion")
    shadow(libs.bundles.qupath)
    shadow(libs.bundles.logging)
    shadow(libs.qupath.fxtras)
    // For testing
    testImplementation("io.github.qupath:qupath-gui-fx:$qupathVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

javafx {
    version = "24.0.2"
    modules("javafx.controls", "javafx.fxml")
}

tasks.test {
    useJUnitPlatform()
}

