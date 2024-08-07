import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-android")
    id("kotlin-kapt")
}

android {
    namespace = "com.mch.myketrubikacleaner"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mch.myketrubikacleaner"
        minSdk = 24
        targetSdk = 34
        versionCode = getVersionCode()
        versionName = getVersionName()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    packagingOptions {
        exclude("META-INF/atomicfu.kotlin_module")
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        dataBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.mikhaellopez:circularprogressbar:3.1.0")
    implementation("com.intuit.sdp:sdp-android:1.1.1")
    implementation("com.intuit.ssp:ssp-android:1.1.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.adivery:sdk:4.7.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.3")


    val room_version = "2.6.1"

    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    // AppMetrica SDK.
    implementation("io.appmetrica.analytics:analytics:7.0.0")
}

//// اسکریپت برای افزایش خودکار versionCode
//fun getVersionCode(): Int {
//    val versionCodeFile = file("version.properties")
//    if (!versionCodeFile.exists()) {
//        versionCodeFile.createNewFile()
//        versionCodeFile.writeText("VERSION_CODE=268\n")
//    }
//
//    val properties = Properties()
//    versionCodeFile.inputStream().use { properties.load(it) }
//
//    val versionCode = properties.getProperty("VERSION_CODE").toInt()
//    val newVersionCode = versionCode + 1
//
//    versionCodeFile.outputStream().use {
//        properties.setProperty("VERSION_CODE", newVersionCode.toString())
//        properties.store(it, null)
//    }
//
//    return newVersionCode
//}
//
//fun getVersionName(): String {
//    return "1.0.269${getVersionCode()}"
//}
//
//// Task برای تغییر نام فایل خروجی APK
//tasks.register("renameApk") {
//    doLast {
//        val appName = "RubikaCleaner" // نام برنامه که می‌خواهید استفاده کنید
//        val versionName = android.defaultConfig.versionName
//        val buildType = "release" // می‌توانید این را به "debug" تغییر دهید یا از متغیر استفاده کنید
//
//        val outputDir = file("${buildDir}/outputs/apk/${buildType}")
//        outputDir.listFiles()?.forEach { file ->
//            if (file.name.endsWith(".apk")) {
//                val newName = "${appName}_${versionName}_${buildType}.apk"
//                file.renameTo(File(outputDir, newName))
//            }
//        }
//    }
//}
//
//// اطمینان از اجرای Task renameApk پس از ساختن APK
//tasks.whenTaskAdded {
//    if (name == "assembleRelease") {
//        finalizedBy("renameApk")
//    }
//}


fun isReleaseBuild(): Boolean {
    return gradle.startParameter.taskRequests.toString().contains("Release")
}

fun getVersionCode(): Int {
    val versionCodeFile = file("version.properties")
    if (!versionCodeFile.exists()) {
        versionCodeFile.createNewFile()
        versionCodeFile.writeText("VERSION_CODE=268\n")
    }
    val properties = Properties().apply {
        load(versionCodeFile.inputStream())
    }
    val versionCode = properties.getProperty("VERSION_CODE").toInt()
    val newVersionCode = if (isReleaseBuild()) versionCode + 1 else versionCode
    properties.setProperty("VERSION_CODE", newVersionCode.toString())
    versionCodeFile.outputStream().use {
        properties.store(it, null)
    }
    return newVersionCode
}

fun getVersionName(): String {
    return "1.0.${getVersionCode()}"
}

tasks.register("renameApk") {
    doLast {
        val appName = "RubikaCleaner"
        val versionName = android.defaultConfig.versionName
        val buildType = "release"
        val outputDir = file("${buildDir}/outputs/apk/$buildType")
        outputDir.listFiles()?.forEach { file ->
            if (file.name.endsWith(".apk")) {
                val newName = "${appName}_${versionName}_$buildType.apk"
                file.renameTo(File(outputDir, newName))
            }
        }
    }
}

tasks.whenTaskAdded {
    if (name == "assembleRelease") {
        finalizedBy("renameApk")
    }
}