plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.zookeeper)

    // JUnit 6 dependencies.
    // See: https://github.com/junit-team/junit-framework/blob/r6.0.2/documentation/modules/ROOT/pages/running-tests/ide-support.adoc
    testImplementation(platform(libs.junit.jupiter.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    // Mockito does not support JUnit 6 yet.
    // See: https://github.com/mockito/mockito/issues/3779
//    testImplementation(platform(libs.mockito.bom))
//    testImplementation(libs.mockito.core)
//    testImplementation(libs.mockito.junit.jupiter)

    testImplementation(libs.assertj.core)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

application {
    mainClass = "com.github.tt4g.zookeeper.playground.App"
}

tasks.withType(JavaCompile::class).configureEach {
    options.compilerArgs.addLast("-Xlint:all")
}

tasks.named<Test>("test") {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
        debug {
            events("started", "passed", "skipped", "failed")
            exceptionFormat =
                org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }
}
