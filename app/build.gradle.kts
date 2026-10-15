plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.themanoftalent.aleclip"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.themanoftalent.aleclip"
        minSdk = 26
        targetSdk = 36
        versionCode = 2026
        versionName = "2.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val keystorePath = (project.findProperty("ALECLIP_KEYSTORE_PATH") as? String)
                ?: System.getenv("KEYSTORE_PATH")
                ?: "${rootDir}/aleclip-release.keystore"
            val keystoreFile = file(keystorePath)

            val sPassword = (project.findProperty("ALECLIP_KEYSTORE_PASSWORD") as? String)
                ?: System.getenv("KEYSTORE_PASSWORD")
            val kAlias = (project.findProperty("ALECLIP_KEY_ALIAS") as? String)
                ?: System.getenv("KEY_ALIAS")
                ?: "aleclip_upload_key"
            val kPassword = (project.findProperty("ALECLIP_KEY_PASSWORD") as? String)
                ?: System.getenv("KEY_PASSWORD")

            if (keystoreFile.exists() && !sPassword.isNullOrBlank() && !kPassword.isNullOrBlank()) {
                storeFile = keystoreFile
                storePassword = sPassword
                keyAlias = kAlias
                keyPassword = kPassword
                storeType = "PKCS12"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseConfig = signingConfigs.findByName("release")
            if (releaseConfig?.storeFile != null && releaseConfig.storeFile!!.exists() && !releaseConfig.storePassword.isNullOrBlank()) {
                signingConfig = releaseConfig
            } else {
                throw GradleException(
                    "Release build aborted: aleclip-release.keystore or credentials are missing! " +
                    "Release builds will NOT be signed with debug keys. " +
                    "Please set KEYSTORE_PASSWORD / KEY_PASSWORD in environment variables or ~/.gradle/gradle.properties."
                )
            }
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.0")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
