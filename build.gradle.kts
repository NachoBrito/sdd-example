import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import io.micronaut.gradle.docker.MicronautDockerfile

plugins {
    java
    checkstyle
    pmd
    alias(libs.plugins.micronaut.application)
    alias(libs.plugins.micronaut.test.resources)
    alias(libs.plugins.spotbugs)
    alias(libs.plugins.pitest)
}

val basePackage = "es.nachobrito.sdd.example"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get())
    }
}

application {
    mainClass = "$basePackage.Application"
}

micronaut {
    version(libs.versions.micronaut.platform.get())
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("$basePackage.*")
    }
    testResources {
        enabled = true
    }
}

// Mockito must be attached as a Java agent on JDK 21+ (dynamic agent loading is deprecated).
val mockitoAgent: Configuration by configurations.creating

dependencies {
    annotationProcessor(libs.micronaut.inject.java)
    annotationProcessor(libs.micronaut.serde.processor)
    annotationProcessor(libs.micronaut.validation.processor)
    annotationProcessor(libs.micronaut.data.processor)
    annotationProcessor(libs.micronaut.openapi)
    annotationProcessor(libs.micronaut.sourcegen.generator.java)

    compileOnly(libs.micronaut.openapi.annotations)
    compileOnly(libs.micronaut.sourcegen.annotations)

    implementation(libs.micronaut.serde.jackson)
    implementation(libs.micronaut.validation)
    implementation(libs.micronaut.http.validation)
    implementation(libs.micronaut.data.jdbc)
    implementation(libs.micronaut.jdbc.hikari)
    implementation(libs.micronaut.liquibase)
    implementation(libs.micronaut.rabbitmq)
    implementation(libs.micronaut.micrometer.core)
    implementation(libs.micronaut.micrometer.observation.http)
    implementation(libs.micronaut.micrometer.registry.otlp)

    runtimeOnly(libs.mariadb.jdbc)
    runtimeOnly(libs.logback.classic)
    runtimeOnly(libs.snakeyaml)

    testImplementation(libs.micronaut.test.junit5)
    testImplementation(libs.micronaut.test.rest.assured)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation(libs.archunit.junit5)
    testRuntimeOnly(libs.junit.jupiter.engine)

    mockitoAgent(platform(libs.micronaut.platform))
    mockitoAgent(libs.mockito.core) { isTransitive = false }
}

val mockitoAgentArg = provider { "-javaagent:${mockitoAgent.singleFile.absolutePath}" }

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = libs.versions.java.get().toInt()
    // Treat compiler warnings as errors. "processing", "classfile", "rawtypes",
    // "unchecked", and "cast" are excluded because Micronaut annotation processors (e.g. Serde)
    // generate code with raw types, unchecked operations, and redundant casts, and third-party JARs reference optional classes.
    options.compilerArgs.addAll(
        listOf(
            "-Xlint:all,-processing,-classfile,-rawtypes,-unchecked,-cast",
            "-Werror",
            "-parameters"
        )
    )
}


tasks.test {
    useJUnitPlatform()
    jvmArgumentProviders.add(CommandLineArgumentProvider { listOf(mockitoAgentArg.get()) })
}

// ---------------------------------------------------------------------------
// Static analysis. Every finding fails the build. See docs/static-analysis.md.
// ---------------------------------------------------------------------------

checkstyle {
    toolVersion = libs.versions.checkstyle.get()
    // Unmodified copy of the Google Java Style configuration bundled with Checkstyle.
    configFile = file("config/checkstyle/google_checks.xml")
    // The Google configuration reports findings as warnings by default. Report them as errors.
    configProperties = mapOf("org.checkstyle.google.severity" to "error")
    isIgnoreFailures = false
    maxWarnings = 0
    maxErrors = 0
}

tasks.withType<Checkstyle>().configureEach {
    // Do not check the sources that annotation processors generate.
    exclude("**/generated/**")
}

pmd {
    toolVersion = libs.versions.pmd.get()
    ruleSetFiles = files("config/pmd/ruleset.xml")
    ruleSets = emptyList()
    isConsoleOutput = true
    isIgnoreFailures = false
    rulesMinimumPriority = 5
}

spotbugs {
    toolVersion = libs.versions.spotbugs.tool.get()
    effort = Effort.MAX
    reportLevel = Confidence.LOW
    ignoreFailures = false
    excludeFilter = file("config/spotbugs/exclude.xml")
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
    reports.create("html") { required = true }
    reports.create("xml") { required = true }
}

pitest {
    pitestVersion = libs.versions.pitest.tool.get()
    junit5PluginVersion = libs.versions.pitest.junit5.plugin.get()
    targetClasses = setOf("$basePackage.*")
    excludedClasses = setOf(
        "$basePackage.Application",
        // Classes that Micronaut annotation processors generate.
        "$basePackage.**.\$*",
        "$basePackage.**.*\$*",
        "$basePackage.**.Serde*",
    )
    excludedTestClasses = setOf("$basePackage.architecture.*", "*IT", "*TestCase")
    mutators = setOf("STRONGER")
    mutationThreshold = 90
    coverageThreshold = 80
    failWhenNoMutations = true
    timestampedReports = false
    outputFormats = setOf("HTML", "XML")
    threads = Runtime.getRuntime().availableProcessors()
    jvmArgs = mockitoAgentArg.map { listOf(it) }
}

tasks.check {
    dependsOn(tasks.named("pitest"))
}

// ---------------------------------------------------------------------------
// Containerization.
// ---------------------------------------------------------------------------

tasks.named<MicronautDockerfile>("dockerfile") {
    baseImage = "eclipse-temurin:${libs.versions.java.get()}-jre"
}
