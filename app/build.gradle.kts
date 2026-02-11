plugins {
    application

    // Add `jvmDependencyConflicts` section.
    // See: https://github.com/gradlex-org/jvm-dependency-conflict-resolution
    id("org.gradlex.jvm-dependency-conflict-resolution").version("2.5")

    // Error Prone.
    // See: https://github.com/tbroyer/gradle-errorprone-plugin
    id("net.ltgt.errorprone").version("5.0.0")
    // NullAway support for Error Prone.
    // https://github.com/tbroyer/gradle-nullaway-plugin
    id("net.ltgt.nullaway").version("3.0.0")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.zookeeper)

    errorprone(libs.errorprone.core)
    errorprone(libs.nullaway)
    implementation(libs.jspecify)

    implementation(libs.slf4j.api)
    implementation(libs.logback.classic)

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

// `jvm-dependency-conflict-resolution` plugin.
jvmDependencyConflicts {
    // logging framework settings.
    // See "Select and enforce a logging framework" section:
    // https://gradlex.org/jvm-dependency-conflict-resolution/#logging-dsl-block
    // GitHub: https://github.com/gradlex-org/jvm-dependency-conflict-resolution/blob/v2.5/src/docs/asciidoc/parts/resolution.adoc#select-and-enforce-a-logging-framework
    logging {
        // Use `ch.qos.logback:logback-classic`
        enforceLogback()
    }
}

// `gradle-nullaway-plugin`.
nullaway {
    annotatedPackages.add("com.github.tt4g.zookeeper.playground")
    jspecifyMode = true
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
