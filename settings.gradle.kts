pluginManagement {
    fun RepositoryHandler.strictMaven(url: String, vararg groups: String) {
        exclusiveContent {
            forRepository { maven(url) }
            filter {
                groups.forEach { includeGroupAndSubgroups(it) }
            }
        }
    }

    repositories {
        mavenLocal()
        gradlePluginPortal()
        strictMaven("https://maven.neoforged.net/releases", "net.neoforged")
    }

    includeBuild("gradle/build-logic")
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "Minecraft Comes Epicly Alive"
