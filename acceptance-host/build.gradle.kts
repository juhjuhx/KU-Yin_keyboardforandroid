plugins { alias(libs.plugins.android.application) }

android {
  namespace = "io.github.juhjuhx.kuyin.acceptancehost"
  compileSdk { version = release(36) { minorApiLevel = 1 } }
  defaultConfig {
    applicationId = "io.github.juhjuhx.kuyin.acceptancehost"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0-p1e"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  sourceSets {
    getByName("test").kotlin.srcDir("src/sharedTest/java")
    getByName("androidTest").kotlin.srcDir("src/sharedTest/java")
  }
}

dependencies {
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  androidTestImplementation(libs.androidx.uiautomator)
}
