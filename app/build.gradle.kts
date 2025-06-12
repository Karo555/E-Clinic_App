plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.e_clinic_app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.e_clinic_app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        jvmTarget = "11" // compose
    }
    buildFeatures {
        compose = true // compose
    }
}

dependencies {
    // firebase storage
    implementation(platform(libs.firebase.bom.v33140))

    // Firebase Storage & Firestore (no versions needed)
    implementation(libs.google.firebase.storage.ktx)
    implementation(libs.com.google.firebase.firebase.firestore.ktx)

    // image handler
    implementation("io.coil-kt:coil-compose:2.4.0")

    // compose
    implementation(platform(libs.androidx.compose.bom.v20230300))
    implementation(libs.ui)
    implementation(libs.androidx.material)
    implementation(libs.ui.tooling.preview)
    implementation(libs.androidx.activity.compose.v170)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.generativeai)
    implementation(libs.firebase.database)
    implementation(libs.firebase.messaging)
    implementation(libs.com.google.firebase.firebase.storage.ktx)
    debugImplementation(libs.ui.tooling)
    implementation(libs.androidx.material.icons.extended)

    // coroutines
    implementation(libs.kotlinx.coroutines.play.services)

    // Firebase Auth
    implementation(libs.google.firebase.auth.ktx)

    // Firestore
    implementation(libs.google.firebase.firestore.ktx)

    // Lifecycle ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    //navigation
    implementation(libs.androidx.navigation.compose)

    // firebase
    implementation(platform(libs.firebase.bom))

    //google play services
    implementation(libs.play.services.location)
    implementation(libs.play.services.auth)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // qr code
    implementation(libs.zxing.android.embedded)
}