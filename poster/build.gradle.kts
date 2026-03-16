plugins {
    id("com.android.library")
    kotlin("plugin.serialization")
}

android {
    namespace = "com.resonance.poster"
    compileSdk = 35

    defaultConfig {
        minSdk = 23

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // 网络
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // 序列化
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

    // 协程
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")

    // 图片加载
    implementation("io.coil-kt:coil:2.7.0")
    implementation("io.coil-kt:coil-compose:2.7.0")

    // 存储
    implementation("com.tencent:mmkv:2.0.2")

    // 核心模块
    implementation(project(":core"))
}