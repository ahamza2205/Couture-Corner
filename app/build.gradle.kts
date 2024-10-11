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
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.junit.ktx)
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
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.paypal.android:paypal-web-payments:1.5.0")

    implementation ("com.airbnb.android:lottie:6.0.1")

    implementation ("com.google.android.gms:play-services-auth:20.5.0")


    implementation ("com.google.android.material:material:1.8.0")

// test

// JUnit testing framework
    testImplementation("junit:junit:4.13.2")

// Coroutines test for handling suspend functions
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")


// Mockito for mocking objects
    testImplementation("org.mockito:mockito-core:3.12.4")

// MockK for mocking Kotlin classes
    testImplementation("io.mockk:mockk:1.12.0")

// Truth for better assertions
    testImplementation("com.google.truth:truth:1.1.3")

// Robolectric for testing
    testImplementation("org.robolectric:robolectric:4.9.2")

   // Timber for logging
    implementation("com.jakewharton.timber:timber:5.0.1")

   // Mockito-inline for mockito extensions
    testImplementation("org.mockito:mockito-inline:4.6.1")

   // Core testing library for LiveData and ViewModel tests
    testImplementation("androidx.arch.core:core-testing:2.1.0")

   // Mockito-Kotlin for better Kotlin integration with Mockito
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")

    // Turbine for testing
    testImplementation ("app.cash.turbine:turbine:0.6.1")
    // For mocking dependencies
    testImplementation ("org.mockito:mockito-core:4.5.1")
    testImplementation ("org.mockito.kotlin:mockito-kotlin:4.0.0")

        // Hilt testing dependencies
        androidTestImplementation ("com.google.dagger:hilt-android-testing:2.48")
        kaptAndroidTest ("com.google.dagger:hilt-android-compiler:2.48")

        // Hilt testing for unit tests
        testImplementation ("com.google.dagger:hilt-android-testing:2.48")
        kaptTest ("com.google.dagger:hilt-android-compiler:2.48")

    implementation ("com.google.firebase:firebase-bom:32.0.0")
    implementation ("com.google.firebase:firebase-auth")
    testImplementation ("org.robolectric:robolectric:4.9")
    // Apollo GraphQL Testing Helpers
    testImplementation("com.apollographql.apollo3:apollo-mockserver:3.7.3")
    testImplementation ("org.mockito:mockito-core:4.2.0")
    testImplementation ("org.mockito:mockito-inline:4.2.0")


}