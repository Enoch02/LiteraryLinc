pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
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
include(":features:barcodescanner")
include(":features:more")
include(":core:settings")
include(":features:search")
include(":features:stats")
include(":features:reader")
include(":mupdf-lib")
