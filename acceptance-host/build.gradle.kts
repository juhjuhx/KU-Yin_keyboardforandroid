plugins { alias(libs.plugins.android.application) }

// Compile the one canonical observer implementation in JVM tests; no copied source in Git.
abstract class StageAcceptanceDriverForJvm : DefaultTask() {
  @get:InputFile abstract val source: RegularFileProperty
  @get:OutputDirectory abstract val output: DirectoryProperty
  @TaskAction fun stage() {
    val file = output.file("AcceptanceDriver.kt").get().asFile
    file.parentFile.mkdirs()
    source.get().asFile.copyTo(file, overwrite = true)
  }
}
val stageAcceptanceDriverForJvm by tasks.registering(StageAcceptanceDriverForJvm::class) {
  source.set(layout.projectDirectory.file("src/androidTest/java/io/github/juhjuhx/kuyin/acceptancehost/AcceptanceDriver.kt"))
  output.set(layout.buildDirectory.dir("generated/observerJvm"))
}

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
  testOptions { unitTests { isIncludeAndroidResources = true } }
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
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.uiautomator)
  testImplementation(libs.robolectric)
  testImplementation(libs.kotlinx.coroutines.core)
  testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.2.10")
  androidTestImplementation(libs.androidx.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  androidTestImplementation(libs.androidx.uiautomator)
}

androidComponents {
  onVariants(selector().all()) { variant ->
    variant.hostTests.values.forEach { hostTest ->
      hostTest.sources.kotlin?.addGeneratedSourceDirectory(stageAcceptanceDriverForJvm) { it.output }
    }
  }
}
