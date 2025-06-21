import com.android.build.api.variant.FilterConfiguration

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    alias(libs.plugins.aboutLibraries)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.enoch02.literarylinc"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.enoch02.literarylinc"
        minSdk = 24
        targetSdk = 35
        versionCode = 14
        versionName = "0.0.14"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        setProperty("archivesBaseName", "Literarylinc")
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "LiteraryLinc Debug")
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true
        }
    }

    val abiCodes = mapOf("armeabi-v7a" to 1, "arm64-v8a" to 2, "x86" to 3, "x86_64" to 4)
    androidComponents {
        onVariants { variant ->
            variant.outputs.forEach { output ->
                val name =
                    output.filters.find { it.filterType == FilterConfiguration.FilterType.ABI }?.identifier
                val baseAbiCode = abiCodes[name]

                if (baseAbiCode != null) {
                    output.versionCode.set(baseAbiCode * 1000 + output.versionCode.get())
                }
            }
        }
    }
}

dependencies {
    api(project(":document-viewer"))
    val daggerVersion: String by rootProject.extra

    implementation(project(":features:booklist"))
    implementation(project(":features:modifybook"))
    implementation(project(":features:bookdetail"))
    implementation(project(":features:more"))
    implementation(project(":features:stats"))
    implementation(project(":features:reader"))
    implementation(project(":core:database"))
    implementation(project(":core:settings"))
    implementation(project(":core:resources"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.ui.text.google.fonts)
    ksp(libs.hilt.android.compiler)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.core.splashscreen)

    implementation(libs.aboutlibraries.core)
    implementation(libs.aboutlibraries.compose.core)
    implementation(libs.aboutlibraries.compose.m3)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
/*
aboutLibraries {
//    android.registerAndroidTasks = true
    library.duplicationMode = com.mikepenz.aboutlibraries.plugin.DuplicateMode.MERGE
    library.duplicationRule = com.mikepenz.aboutlibraries.plugin.DuplicateRule.SIMPLE

    collect.filterVariants.addAll(listOf("debug", "release"))
}*/
