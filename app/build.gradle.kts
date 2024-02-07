plugins {
    id ("com.android.application")
}

android {
    namespace = "net.lonelytransistor.notificationinsystem"
    compileSdk = 34

    defaultConfig {
        applicationId = "net.lonelytransistor.notificationinsystem"
        minSdk = 30
        targetSdk = 34
        versionName = "1.0"
        versionCode = 1
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles("proguard-android-optimize.txt", "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes.addAll(listOf(
                "DebugProbesKt.bin",
                "META-INF/**.version",
                "kotlin-tooling-metadata.json",
                "kotlin/**.kotlin_builtins",
                "org/bouncycastle/pqc/**.properties",
                "org/bouncycastle/x509/**.properties",
            ))
        }
    }

    lint {
        disable.add("MissingTranslation")
    }

    applicationVariants.all {
        if (this.buildType.isDebuggable.not()) {
            outputs.all {
                this as com.android.build.gradle.internal.api.ApkVariantOutputImpl
                outputFileName = "SystemUI Notifier v${versionName}.apk"
            }
        }
    }
}

dependencies {
    implementation ("androidx.appcompat:appcompat:1.6.1")

    implementation(project(mapOf("path" to ":commonlib")))

    compileOnly ("de.robv.android.xposed:api:82")
}
