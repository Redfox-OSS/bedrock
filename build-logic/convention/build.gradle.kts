plugins {
    `kotlin-dsl`
}

group = "dev.redfox.bedrock.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "bedrock.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidApplication") {
            id = "bedrock.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidCompose") {
            id = "bedrock.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
    }
}
