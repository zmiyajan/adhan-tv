plugins {
    alias(libs.plugins.kotlin.jvm)
}

// adhan2 0.0.7 ships Java 21 bytecode
kotlin { jvmToolchain(21) }

dependencies {
    implementation(libs.adhan)
    testImplementation(libs.junit)
}
