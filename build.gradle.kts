import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "net.jcflorezr"
version = "0.1-SNAPSHOT"

buildscript {
    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
    var kotlinVersion: String by extra
    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
    var springVersion: String by extra
    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
    var springDataVersion: String by extra
    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
    var jacksonVersion: String by extra
    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
    var testContainersVersion: String by extra

    @Suppress("UNUSED_VALUE")
    kotlinVersion = "1.3.10"
    @Suppress("UNUSED_VALUE")
    springVersion = "5.1.0.RELEASE"
    @Suppress("UNUSED_VALUE")
    springDataVersion = "2.1.3.RELEASE"
    @Suppress("UNUSED_VALUE")
    jacksonVersion = "2.9.8"
    @Suppress("UNUSED_VALUE")
    testContainersVersion = "1.10.6"
}

val kotlinVersion: String by extra
val springVersion: String by extra
val springDataVersion: String by extra
val jacksonVersion: String by extra
val testContainersVersion: String by extra

plugins {
    kotlin("jvm") version "1.3.10"
    kotlin("plugin.spring") version "1.3.10"
    id("org.jlleitschuh.gradle.ktlint") version "7.2.1"
    id("com.palantir.docker") version "0.21.0"
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {

    // Kotlin
    implementation(kotlin(module = "stdlib-jdk8", version = kotlinVersion))
    implementation(kotlin(module = "reflect", version = kotlinVersion))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.0")

    // Spring
    implementation("org.springframework:spring-core:$springVersion")

    // Kafka
    implementation("org.apache.kafka:kafka_2.12:2.2.0")

    // Util
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.uuid:java-uuid-generator:3.1.4")

    // Testing
    testImplementation("org.springframework:spring-test:$springVersion")
    testImplementation("junit:junit:4.12")
    testImplementation("org.mockito:mockito-core:2.24.5")
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
}

val compileKotlin by tasks.getting(KotlinCompile::class) {
    // Overwriting the "implementationKotlin" task.
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict")
    doLast { println("Finished compiling Kotlin source code") }
}
val compileTestKotlin by tasks.getting(KotlinCompile::class) {
    // Overwriting the "implementationTestKotlin" task.
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict")
    doLast { println("Finished compiling Kotlin source code for testing") }
}