pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
        }
    }
}

rootProject.name = "MnemoAI"
include(":app")
include(":core:database")
include(":core:model")
include(":core:data")
include(":core:common")
include(":core:datastore")
include(":core:network")
include(":core:ui")
include(":feature:reciteword")
include(":feature:mine")
include(":feature:search")
include(":feature:statistic")
include(":feature:worddetail")
include(":feature:changevocabularybookword")
include(":feature:vocabularybook")
include(":feature:vocabularybookgroup")
include(":shared-ui")
include(":core:datastore-proto")
include(":core:domain")
