import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "app.adhantv"
    compileSdk = 37

    defaultConfig {
        applicationId = "app.adhantv"
        minSdk = 26
        targetSdk = 35
        // One source of truth: version.txt, bumped by release-please from the commit messages.
        val version = rootProject.file("version.txt").readText().trim()
        val (major, minor, patch) = version.split(".").map(String::toInt)
        versionCode = major * 10_000 + minor * 100 + patch
        versionName = version
    }

    // Release signing comes from keystore.properties (never committed); without it, release builds use the debug
    // key so they can be tried on a TV.
    val keystore = rootProject.file("keystore.properties").takeIf { it.exists() }?.let { f ->
        Properties().apply { f.inputStream().use { load(it) } }
    }
    signingConfigs {
        if (keystore != null) {
            create("release") {
                storeFile = rootProject.file(keystore.getProperty("storeFile"))
                storePassword = keystore.getProperty("storePassword")
                keyAlias = keystore.getProperty("keyAlias")
                keyPassword = keystore.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
    androidResources { localeFilters += listOf("ar", "en") }
}

/** LICENSE, CHANGELOG.md and licenses/ copied into the APK's assets (legal/), so «عن البرنامج» can show them. */
abstract class LegalAssets : DefaultTask() {
    @get:InputFiles abstract val files: ConfigurableFileCollection

    @get:OutputDirectory abstract val outputDir: DirectoryProperty

    @TaskAction
    fun copy() {
        val dir = outputDir.get().asFile.resolve("legal").apply { deleteRecursively(); mkdirs() }
        files.forEach { it.copyTo(dir.resolve(it.name), overwrite = true) }
    }
}

val legalAssets = tasks.register<LegalAssets>("legalAssets") {
    files.from(rootProject.file("LICENSE"), rootProject.file("CHANGELOG.md"), rootProject.fileTree("licenses"))
}

androidComponents {
    onVariants { it.sources.assets?.addGeneratedSourceDirectory(legalAssets, LegalAssets::outputDir) }
}

dependencies {
    implementation(project(":core:prayer"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.foundation)
    implementation(libs.tv.material)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.datastore.preferences)
    implementation(libs.media3.exoplayer)
    implementation(libs.zxing.core)
}
