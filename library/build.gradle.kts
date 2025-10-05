import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.vanniktechMavenPublish)
}

group = "io.github.neilyich"
version = "0.0.1-alpha02"

kotlin {
    jvm()
    androidLibrary {
        namespace = "io.github.neilyich.glassmorphism"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withJava() // enable java compilation support
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilations.configureEach {
            compilerOptions.configure {
                jvmTarget.set(JvmTarget.JVM_11)
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.foundation)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "glassmorphism-compose", version.toString())

    pom {
        name = "Glassmorphism Compose"
        description = "Library providing glassmorphism for Compose"
        inceptionYear = "2025"
        url = "https://github.com/neilyich/glassmorphism-compose"
        licenses {
            license {
                name = "MIT License"
                url = "https://mit-license.org"
                distribution = "https://mit-license.org"
            }
        }
        developers {
            developer {
                id = "neilyich"
                name = "Ilya Nekleenov"
                url = "https://github.com/neilyich"
            }
        }
        scm {
            url = "https://github.com/neilyich/glassmorphism-compose"
            connection = "scm:git:git://github.com/neilyich/glassmorphism-compose.git"
            developerConnection = "scm:git:ssh://git@github.com/neilyich/glassmorphism-compose.git"
        }
    }
}
