plugins {
    alias(libs.plugins.bedrock.android.library)
    alias(libs.plugins.bedrock.android.compose)
    `maven-publish`
}

// Bedrock: a calm, frosted design system for Android, as a standalone Compose library.
group = "dev.redfox"
version = "0.2.0"

android {
    namespace = "dev.redfox.bedrock"
    // Library resources (font, icons) are prefixed so they never clash with a host app's.
    resourcePrefix = "br_"

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

kotlin {
    // Every public declaration states its visibility and type, so nothing goes public by accident.
    explicitApi()
}

dependencies {
    // Exposed as `api`: host apps write Compose/M3 code against the same versions, and
    // LocalHazeState / rememberHazeState come from Haze.
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.compose.material3)
    api(libs.haze)
}

publishing {
    publications {
        register<MavenPublication>("release") {
            artifactId = "bedrock"
            afterEvaluate { from(components["release"]) }
            pom {
                name.set("Bedrock")
                description.set("A calm, frosted design system for Android, built with Jetpack Compose.")
                url.set("https://github.com/Redfox-OSS/bedrock")
                licenses {
                    license {
                        name.set("GNU General Public License v3.0")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.html")
                    }
                }
                developers {
                    developer {
                        id.set("debz-g")
                        name.set("Debayan G")
                        url.set("https://github.com/debz-g")
                    }
                }
                scm {
                    url.set("https://github.com/Redfox-OSS/bedrock")
                    connection.set("scm:git:https://github.com/Redfox-OSS/bedrock.git")
                }
            }
        }
    }
    repositories {
        // The Maven repository on GitHub Pages: the release workflow checks out the gh-pages
        // branch and passes its maven/ folder as -PreleaseRepo, so each release adds to it.
        val releaseRepo = providers.gradleProperty("releaseRepo")
        if (releaseRepo.isPresent) {
            maven {
                name = "release"
                url = uri(releaseRepo.get())
            }
        }
    }
}
