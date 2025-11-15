plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  id("com.google.devtools.ksp")
}

android {
  namespace = "com.example.zoomatch"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.example.zoomatch"
    minSdk = 26
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions {
    jvmTarget = "11"
  }
  buildFeatures {
    viewBinding = true
  }
}


dependencies {

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.material)
  implementation(libs.androidx.activity)
  implementation(libs.androidx.constraintlayout)
  implementation(libs.androidx.annotation)
  implementation(libs.androidx.lifecycle.livedata.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.ktx)
  implementation(libs.androidx.navigation.fragment.ktx)
  implementation(libs.androidx.navigation.ui.ktx)
  implementation(libs.androidx.cardview)
  implementation(libs.androidx.viewpager2)
  implementation(libs.androidx.recyclerview)
  implementation(libs.mpandroidchart)
  implementation(libs.glide)
  implementation(libs.androidx.room.ktx)
  implementation(libs.retrofit)
  implementation(libs.converter.gson)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.cronet.embedded)
  val room_version = "2.7.2"
  implementation("androidx.room:room-runtime:${room_version}")
  ksp("androidx.room:room-compiler:$room_version")
  implementation("androidx.room:room-ktx:${room_version}")
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
}