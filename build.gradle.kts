import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import io.gitlab.arturbosch.detekt.extensions.DetektExtension.Companion.DEFAULT_SRC_DIR_JAVA
import io.gitlab.arturbosch.detekt.extensions.DetektExtension.Companion.DEFAULT_SRC_DIR_KOTLIN
import io.gitlab.arturbosch.detekt.extensions.DetektExtension.Companion.DEFAULT_TEST_SRC_DIR_JAVA
import io.gitlab.arturbosch.detekt.extensions.DetektExtension.Companion.DEFAULT_TEST_SRC_DIR_KOTLIN
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Locale

plugins {
    id(Plugins.kotlin_jvm) version Global.kotlin_version

    id(Plugins.update_dependencies) version PluginVers.update_dependencies
    id(Plugins.detekt) version PluginVers.detekt
    id(Plugins.jacoco)

    `maven-publish`
}

group = "com.github.mynameisscr"
version = "v0.0.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation(Libs.kotlin_reflect)

    testImplementation(Libs.kotlin_test)
    testImplementation(Libs.kotest_assertions_core_jvm)
    testImplementation(Libs.mockk)
}

java {
    withSourcesJar()
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
        }
    }
}

detekt {
    config = files("$rootDir/detekt/detekt-config.yml")
    buildUponDefaultConfig = true
    autoCorrect = true

    @Suppress("DEPRECATION")
    reports {
        html.required.set(true)
    }

    source = files(
        DEFAULT_SRC_DIR_JAVA,
        DEFAULT_TEST_SRC_DIR_JAVA,
        DEFAULT_SRC_DIR_KOTLIN,
        DEFAULT_TEST_SRC_DIR_KOTLIN,
    )

    dependencies {
        detektPlugins("${Plugins.detekt_formatting}:${PluginVers.detekt}")
    }
}

tasks {
    val check = named<DefaultTask>("check")
    val dependencyUpdate = named<DependencyUpdatesTask>("dependencyUpdates")

    check {
        finalizedBy(dependencyUpdate)
    }

    dependencyUpdate {
        revision = "release"
        outputFormatter = "txt"
        checkForGradleUpdate = true
        outputDir = "$buildDir/reports/dependencies"
        reportfileName = "updates"
    }

    dependencyUpdate.configure {
        fun isNonStable(version: String): Boolean {
            val stableKeyword = listOf("RELEASE", "FINAL", "GA")
                .any { version.uppercase(Locale.getDefault()).contains(it) }
            val regex = "^[0-9,.v-]+(-r)?$".toRegex()
            val isStable = stableKeyword || regex.matches(version)
            return isStable.not()
        }

        rejectVersionIf {
            isNonStable(candidate.version) && !isNonStable(currentVersion)
        }
    }

    val failOnWarning =
        project.properties["allWarningsAsErrors"] != null && project.properties["allWarningsAsErrors"] == "true"

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_11.toString()
            allWarningsAsErrors = failOnWarning
            freeCompilerArgs = listOf("-Xjvm-default=enable")
        }
    }

    withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_11.toString()
        targetCompatibility = JavaVersion.VERSION_11.toString()

        options.compilerArgs.add("-Xlint:all")
    }

    withType<Test> {
        useJUnitPlatform()

        environment("liquibase.duplicateFileMode", "WARN")

        testLogging {
            events(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
            showStandardStreams = true
            exceptionFormat = TestExceptionFormat.FULL
        }

        finalizedBy(this@tasks.jacocoTestReport)
    }

    jacocoTestReport {
        dependsOn(withType<Test>())

        classDirectories.setFrom(
            files(classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        "**/uuid/**",
                    )
                }
            })
        )
    }
}
