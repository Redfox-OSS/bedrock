plugins {
    alias(libs.plugins.bedrock.android.application)
    alias(libs.plugins.bedrock.android.compose)
}

// The Bedrock catalog: every token, component, motion and transition, on a phone.
android {
    namespace = "dev.redfox.bedrock.catalog"

    defaultConfig {
        applicationId = "dev.redfox.bedrock.catalog"
        versionCode = 1
        versionName = project(":bedrock").version.toString()
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            // Signed with the debug key so the release APK installs anywhere for a look.
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

dependencies {
    implementation(project(":bedrock"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
}
