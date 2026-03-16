plugins {
    id("com.android.application") version "9.0.0" apply false
    id("com.android.library") version "9.0.0" apply false
    kotlin("plugin.serialization") version "2.2.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        // 阿里云镜像仓库
        maven(url = "https://maven.aliyun.com/repository/public")
        maven(url = "https://maven.aliyun.com/repository/jcenter")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
