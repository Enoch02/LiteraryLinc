pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://maven.ghostscript.com") }
        maven { url = uri("https://github.com/psiegman/mvn-repo/raw/master/releases") }
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
include(":features:modifybook")
include(":core:coverfile")
include(":features:bookdetail")
include(":features:more")
include(":core:settings")
include(":features:stats")
include(":features:reader")
include(":document-viewer")
include(":core:resources")
