import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform") version "1.8.21"
    id("com.android.application") version "7.4.2" apply false // Applied in android {} block later or by target
    id("org.jetbrains.compose") version "1.4.0"
    id("io.fabric") version "1.+" apply false // Apply false at root, will be applied to Android
}

// Common versions
val composeVersion = "1.4.0" // org.jetbrains.compose version
val androidCompileSdkVersion = 33
val androidMinSdkVersion = 21
val androidTargetSdkVersion = 33

kotlin {
    android {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.ui)
                // Consider adding api(compose.materialIconsExtended) if needed later
                // For multiplatform settings (check latest version)
                implementation("com.russhwolf:multiplatform-settings-no-arg:1.0.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1") // Explicit coroutines
            }
            resources.srcDirs("src/commonMain/composeResources")
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("com.crashlytics.sdk.android:crashlytics:2.2.2@aar") {
                    isTransitive = true // Replicates transitive = true
                }
                // Android specific dependencies can be added here
                // e.g. implementation("androidx.core:core-ktx:1.9.0")
                // implementation("androidx.appcompat:appcompat:1.6.1")
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
        val iosMainRoot by creating { // Renamed to avoid conflict with potential future iosMain from plugin
            dependsOn(commonMain)
        }
        iosX64Main.dependsOn(iosMainRoot)
        iosArm64Main.dependsOn(iosMainRoot)
        iosSimulatorArm64Main.dependsOn(iosMainRoot)
    }
}

android { // This block in root is for configuring the Android plugin, not applying it
    namespace = "de.musoft.elch.kmp.app" // Will be used by the actual Android app module/target
    compileSdk = androidCompileSdkVersion
    defaultConfig {
        minSdk = androidMinSdkVersion
        targetSdk = androidTargetSdkVersion
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    // sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml") // Move to android target
    // sourceSets["main"].res.srcDirs("src/androidMain/res")
}

compose.desktop {
    application {
        mainClass = "MainKt" // Will be in desktopMain
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ElchAppKMP"
            packageVersion = "1.0.0"
        }
    }
}
