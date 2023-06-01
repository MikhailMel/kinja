object LibVers {
    const val kotlin_version = "1.8.21"

    const val kotest = "5.6.2"
}

object Libs {
    // Kotlin
    const val kotlin_reflect = "org.jetbrains.kotlin:kotlin-reflect:${LibVers.kotlin_version}"

    // Tests
    const val kotlin_test = "org.jetbrains.kotlin:kotlin-test:${LibVers.kotlin_version}"
    const val kotest_assertions_core_jvm = "io.kotest:kotest-assertions-core-jvm:${LibVers.kotest}"
}
