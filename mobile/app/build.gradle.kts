plugins {
    alias(libs.plugins.android.application)
}
android {
    namespace = "net.rolibrt.itp_notifier"
    compileSdk = 35

    defaultConfig {
        applicationId = "net.rolibrt.itp_notifier"
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
}

dependencies {
    implementation(libs.okhttp)
    implementation(libs.recyclerview)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.org.projectlombok.lombok)
    implementation(libs.swiperefreshlayout)
    annotationProcessor(libs.org.projectlombok.lombok)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}