import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// Release signing material is supplied at build time via environment variables in CI
// (or an untracked local app/signing.properties fallback) and is never committed.
val signingPropertiesFile = project.file("signing.properties")
val signingProperties = Properties().apply {
    if (signingPropertiesFile.exists()) {
        signingPropertiesFile.inputStream().use { load(it) }
    }
}
val releaseStoreFilePath = System.getenv("RELEASE_STORE_FILE")?.takeIf { it.isNotBlank() }
    ?: signingProperties.getProperty("storeFile")?.takeIf { it.isNotBlank() }
val releaseStorePassword = System.getenv("RELEASE_STORE_PASSWORD")?.takeIf { it.isNotEmpty() }
    ?: signingProperties.getProperty("storePassword")?.takeIf { it.isNotEmpty() }
val releaseKeyAlias = System.getenv("RELEASE_KEY_ALIAS")?.takeIf { it.isNotBlank() }
    ?: signingProperties.getProperty("keyAlias")?.takeIf { it.isNotBlank() }
val releaseKeyPassword = System.getenv("RELEASE_KEY_PASSWORD")?.takeIf { it.isNotEmpty() }
    ?: signingProperties.getProperty("keyPassword")?.takeIf { it.isNotEmpty() }
val hasReleaseSigning = releaseStoreFilePath != null &&
    project.file(releaseStoreFilePath).exists() &&
    releaseStorePassword != null &&
    releaseKeyAlias != null &&
    releaseKeyPassword != null

// Version information can be overridden from CI so that the tag drives the release metadata.
val applicationVersionName = (findProperty("releaseVersionName") as String?) ?: "0.0.0-local"
val applicationVersionCode = (findProperty("releaseVersionCode") as String?)?.toIntOrNull() ?: 1

android {
    namespace = "com.panther.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.panther.app"
        minSdk = 26
        targetSdk = 36
        versionCode = applicationVersionCode
        versionName = applicationVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        // Expose Room exported schema JSONs to MigrationTestHelper in androidTest.
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = project.file(checkNotNull(releaseStoreFilePath))
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }

    applicationVariants.all {
        val variant = this
        outputs.all {
            (this as BaseVariantOutputImpl).outputFileName = "Panther V${variant.versionName}.apk"
        }
    }
}

ksp { arg("room.schemaLocation", "$projectDir/schemas") }

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.icons.core)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.junit)
    // org.json is part of the Android platform but absent from the JVM test classpath.
    testImplementation(libs.json)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
