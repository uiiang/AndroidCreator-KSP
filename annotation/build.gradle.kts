plugins {
    id("java")
    id("com.google.devtools.ksp") version "1.9.24-1.0.20"
    id("maven-publish")
    kotlin("jvm")
}
apply(from = "publish_jar.gradle.kts")

group = "uii.ang.creator.plugin"
version = "1.0-SNAPSHOT"

repositories {
    gradlePluginPortal()
    mavenCentral()
}

ksp {
    arg("autoserviceKsp.verify", "true")
    arg("autoserviceKsp.verbose", "true")
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:1.9.23-1.0.20")
    implementation("com.google.auto.service:auto-service-annotations:1.0.1")
    implementation("com.squareup:kotlinpoet:1.17.0")
    implementation("com.squareup:kotlinpoet-ksp:1.17.0")
    ksp("dev.zacsweers.autoservice:auto-service-ksp:1.2.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
