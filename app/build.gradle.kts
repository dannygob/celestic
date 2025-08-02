plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-parcelize")
    id("com.google.dagger.hilt.android")
    kotlin("kapt")

}

android {
    namespace = "com.example.celestica"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.celestica"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndkVersion = "26.1.10909125" // Specify NDK version for CMake integration

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    configurations.all {
        resolutionStrategy {
            force("org.jetbrains:annotations:23.0.0") // Force the newer version
            // Or if you explicitly wanted the older one (less common for this scenario)
            // force("com.intellij:annotations:12.0")
            exclude(group = "com.intellij", module = "annotations")
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.12" // Updated for Kotlin 2.0+ compatibility
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    externalNativeBuild {
        cmake {
            path = "../../opencv/CMakeLists.txt" // Path to OpenCV's CMakeLists.txt
            version = "3.22.1" // Match the CMake version used in build_opencv_android.bat
            defaultConfig {
                abiFilters += "arm64-v8a" // Specify the ABI to build for
            }
            arguments += "-DOPENCV_EXTRA_MODULES_PATH=../../opencv_contrib/modules" // Path to opencv_contrib modules
            arguments += "-DBUILD_SHARED_LIBS=ON" // Build shared libraries
            arguments += "-DBUILD_ANDROID_PROJECTS=OFF" // Prevent building OpenCV's own Android projects
            arguments += "-DANDROID_PLATFORM=android-24" // Match the Android platform from the batch script
            arguments += "-DCMAKE_BUILD_TYPE=Release" // Build type
        }
    }
}

dependencies {
    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    // Android core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.androidx.activity.compose)

    implementation(libs.material)

    implementation(libs.androidx.room.runtime)


    // OpenCV (Commented out as it will be built directly via CMake)
    // implementation(libs.opencv)

    // CameraX

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.androidx.room.compiler)
    implementation(libs.androidx.room.common.jvm)
    implementation(libs.androidx.room.runtime.android)
    implementation(libs.litert)
    implementation(libs.androidx.navigation.compose.jvmstubs)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.androidx.databinding.adapters)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    kapt(libs.hilt.compiler)

    // AprilTag (Commented out due to resolution issues and elective use)
    // implementation(libs.apriltaglib)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.benchmark.macro)





}
