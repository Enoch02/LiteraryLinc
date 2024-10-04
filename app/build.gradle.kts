plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.enoch02.literarylinc"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.enoch02.literarylinc"
        minSdk = 24
        targetSdk = 33
        versionCode = 7
        versionName = "0.0.7"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val daggerVersion: String by rootProject.extra

    api(project(":mupdf-lib"))
    implementation(project(":features:booklist"))
    implementation(project(":features:modifybook"))
    implementation(project(":features:bookdetail"))
    implementation(project(":features:barcodescanner"))
    implementation(project(":features:search"))
    implementation(project(":features:more"))
    implementation(project(":features:stats"))
    implementation(project(":features:reader"))
    implementation(project(":core:database"))
    implementation(project(":core:settings"))

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
}
