plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.kvstorage"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.kvstorage"
        minSdk = 24
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
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    api("androidx.lifecycle:lifecycle-runtime-ktx:2.9.1")


    implementation(project(":lib:asynckv-mmkv"))
    implementation(project(":lib:asynckv-datastore"))
    implementation(project(":lib:asynckv-preference"))

//    implementation("com.zhn.asynckv:asynckv-mmkv:1.0,0")
//    implementation("com.zhn.asynckv:asynckv-datastore:1.0,0")
//    implementation("com.zhn.asynckv:asynckv-preference:1.0,0")


}