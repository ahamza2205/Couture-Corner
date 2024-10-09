plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    id ("androidx.navigation.safeargs.kotlin")
    id("dagger.hilt.android.plugin")
    id("com.apollographql.apollo3").version("3.7.3")
    alias(libs.plugins.google.gms.google.services)
}

apollo {
    packageName.set("com.graphql")
    generateKotlinModels.set(true)

}


android {
    namespace = "com.example.couturecorner"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.couturecorner"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    kapt {
        correctErrorTypes = true
    }

    packaging {
        resources {
            excludes += "**/META-INF/NOTICE.md"
            excludes += "**/META-INF/LICENSE.md"
        }
    }


}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.cardview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.firebase.auth)
    // Material Design Components
    implementation (libs.material)
    implementation ("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation ("androidx.navigation:navigation-ui-ktx:2.5.3")
    implementation ("com.google.android.material:material:1.9.0")


    implementation ("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.1")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

    implementation ("androidx.activity:activity-ktx:1.5.0")
    implementation ("androidx.fragment:fragment-ktx:1.6.2")

    implementation("com.apollographql.apollo3:apollo-runtime:3.7.3") // Check for the latest version
    // implementation("com.apollographql.apollo3:apollo-coroutines-support:3.7.3")

    ///  implementation("com.apollographql.apollo3:apollo-runtime:3.7.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")

    implementation("com.google.dagger:hilt-android:2.42")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    implementation ("androidx.navigation:navigation-fragment-ktx:2.5.3")

    // Dagger Hilt
    implementation ("com.google.dagger:hilt-android:2.48")
    kapt ("com.google.dagger:hilt-compiler:2.48")
    //gild
    implementation( "com.github.bumptech.glide:glide:4.16.0")

    implementation ("de.hdodenhof:circleimageview:3.1.0")


    implementation ("com.sun.mail:android-mail:1.6.7")
    implementation ("com.sun.mail:android-activation:1.6.7")
    implementation ("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-auth")
//map
    implementation("org.osmdroid:osmdroid-android:6.1.12")


    implementation ("com.airbnb.android:lottie:6.0.1")

    implementation ("com.google.android.gms:play-services-auth:20.5.0")


    implementation ("com.google.android.material:material:1.8.0")

    implementation ("com.google.code.gson:gson:2.8.9")

}