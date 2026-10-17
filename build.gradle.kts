plugins {
    kotlin("jvm") version "2.3.10"
    application
}

application {
    mainClass = "io.github.knutholtet.MainKt"
}


group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven("https://maven.lavalink.dev/releases")
}

dependencies {
    testImplementation(kotlin("test-junit5"))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
    implementation("dev.schlaubi.lavakord:kord:9.2.0")

    /* Kord */
    implementation("dev.kord:kord-core:0.17.0")

    /* Ktor */
    val ktorVersion = "3.4.0"
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-android:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-xml:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-protobuf:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-serialization-gson:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
