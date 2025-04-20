plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.android.application") version "8.1.4" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true // Soporte para drawables vectoriales
        }
    }
    buildFeatures {
        compose = true // Activa Jetpack Compose
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.7.8" // Usa una versión compatible con tu configuración
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true // Habilitar soporte para Jetpack Compose
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}" // Exclusión de recursos innecesarios
        }
    }
}

dependencies {

    implementation(libs.androidx.activity)
    implementation(platform(libs.androidx.compose.bom.v20230100))


    // Core Android
    implementation(libs.androidx.core.ktx) // Extensiones de Core KTX
    implementation(libs.androidx.lifecycle.runtime.ktx) // Extensiones para ciclo de vida
    implementation(libs.androidx.activity.compose) // Actividad Compose

    // Compose BOM (Bill of Materials)
    implementation(platform(libs.androidx.compose.bom)) // BOM para versión unificada de Compose
    implementation(libs.androidx.ui) // Extensiones de UI
    implementation(libs.androidx.ui.graphics) // Utilidades gráficas
    implementation(libs.androidx.ui.tooling.preview) // Herramientas para previsualización
    implementation(libs.androidx.material3) // Componentes Material Design 3

    // Navigation Compose
    implementation(libs.navigation.compose) // Dependencia de Navigation Compose
    implementation(libs.navigation.ui.ktx) // Navegación UI

    // Material y compatibility UI
    implementation(libs.androidx.appcompat) // Compatibilidad con versiones antiguas de Android
    implementation(libs.material) // Material Design

    // Constraint Layout (Si lo usas en XML)
    implementation(libs.androidx.constraintlayout)

    // Testing
    testImplementation(libs.junit) // Framework de pruebas unitarias
    androidTestImplementation(libs.androidx.junit) // Pruebas instrumentadas
    androidTestImplementation(libs.androidx.espresso.core) // Framework Espresso para UI
    androidTestImplementation(platform(libs.androidx.compose.bom)) // Pruebas en Compose
    androidTestImplementation(libs.androidx.ui.test.junit4) // Pruebas con JUnit4

    // Debugging
    debugImplementation(libs.androidx.ui.tooling) // Herramientas de Compose para debugging
    debugImplementation(libs.androidx.ui.test.manifest) // Manifesto para pruebas UI

    // OpenCV
    implementation(libs.opencv) // Biblioteca de OpenCV para procesamiento de imágenes

}