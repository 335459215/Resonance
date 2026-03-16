plugins {
    id("com.android.library")
}

android {
    namespace = "com.resonance.core"
    compileSdk = 35

    defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        targetSdk = 35
    }

    lint {
        targetSdk = 35
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
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.24")
    testImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // 播放内核
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.5.1")
    implementation("androidx.media3:media3-ui:1.5.1")
    implementation("tv.danmaku.ijk.media:ijkplayer-java:0.8.8")
    implementation("tv.danmaku.ijk.media:ijkplayer-armv7a:0.8.8")
    implementation("tv.danmaku.ijk.media:ijkplayer-arm64:0.8.8")
    implementation("org.videolan.android:libvlc-all:3.3.14")

    // 协程
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")

    // 网络
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // 存储
    implementation("com.tencent:mmkv:2.0.2")
}