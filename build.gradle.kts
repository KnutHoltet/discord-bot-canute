plugins {
    kotlin("jvm") version "1.9.23"
    application
}

application {
    mainClass = "io.github.knutholtet.MainKt"
}


group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    // Kord Snapshots Repository (Optional):
    maven("https://oss.sonatype.org/content/repositories/snapshots")



}

dependencies {
    testImplementation(kotlin("test"))

    /* Kord */
    implementation("dev.kord:kord-core:0.15.0-SNAPSHOT")

    /* Ktor */
    val ktorVersion = "2.3.11"
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
