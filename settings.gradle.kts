pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven (url = "https://api.xposed.info/")
        google()
        mavenCentral()
    }
}

rootProject.name = "SystemUI Notifier"
include (":app")
include(":commonlib")
