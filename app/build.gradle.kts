plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.tailwagging"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tailwagging"
        minSdk = 21
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    //tensorflow dependencies
    implementation(libs.tensorflow.lite) //tensorflow light
    implementation(libs.tensorflow.lite.support) //tensorflow lib
    implementation(libs.tensorflow.tensorflow.lite.select.tf.ops)

    //onnx dependencies
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.14.0")

    //PyTouch dependencies
    implementation ("org.pytorch:pytorch_android:1.10.0")
    implementation ("org.pytorch:pytorch_android_torchvision:1.10.0")
}