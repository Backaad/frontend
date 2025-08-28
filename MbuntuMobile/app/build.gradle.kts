plugins {
    alias(libs.plugins.android.application)

}

android {
    namespace = "com.Mbuntu.MbuntuMobile"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.Mbuntu.MbuntuMobile"
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

        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.androidx.room.common.jvm)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.androidx.security.crypto)
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    implementation(libs.jedis)

}