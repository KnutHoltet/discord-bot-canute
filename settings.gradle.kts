plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "discordbot"
include("src:main:Gateway")
findProject(":src:main:Gateway")?.name = "Gateway"
