plugins {
    alias(libs.plugins.android.application)
}
apply(plugin = "com.google.gms.google-services")

android {
    namespace = "com.example.tailwagging"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tailwagging"
        minSdk = 23
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation (platform("com.google.firebase:firebase-bom:33.13.0"))

// Firebase libraries
    implementation ("com.google.firebase:firebase-auth-ktx")
    implementation ("com.google.firebase:firebase-storage-ktx")
    implementation ("com.google.firebase:firebase-database-ktx")
    implementation ("com.google.firebase:firebase-firestore-ktx")
    implementation ("com.google.code.gson:gson:2.10.1") //calendar store event

// Glide for image loading
    implementation ("com.github.bumptech.glide:glide:4.16.0")

// Play Services for authentication
    implementation ("com.google.android.gms:play-services-auth:21.3.0")

// Facebook Android SDK
    implementation ("com.facebook.android:facebook-android-sdk:18.0.3")

// Annotation processor for Glide
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")

    implementation ("de.hdodenhof:circleimageview:3.1.0")

}