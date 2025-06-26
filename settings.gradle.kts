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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven("${rootDir}/repo".also { print(it) })
        google()
        mavenCentral()
//        mavenLocal()
    }
}

rootProject.name = "KvStorage"
include(":app")
include(":lib:asynckv")
include(":lib:asynckv-mmkv")
include(":lib:asynckv-datastore")
include(":lib:asynckv-preference")
