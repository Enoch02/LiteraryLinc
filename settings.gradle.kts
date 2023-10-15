pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "LiteraryLinc"
include(":app")
include(":core")
include(":core:database")
include(":features")
include(":features:booklist")
include(":features:addbook")
include(":core:coverfile")
