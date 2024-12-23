group = "no.nav.syfo"
version = "0.0.1"

val flywayVersion = "11.1.0"
val hikariVersion = "6.2.1"
val postgresVersion = "42.7.4"
val postgresEmbeddedVersion = "2.1.0"
val kafkaVersion = "3.9.0"
val logbackVersion = "1.5.15"
val logstashEncoderVersion = "8.0"
val micrometerRegistryVersion = "1.14.2"
val jacksonDatatypeVersion = "2.18.2"
val ktorVersion = "3.0.3"
val mockkVersion = "1.13.14"
val nimbusJoseJwtVersion = "9.48"
val kotlinVersion = "2.1.0"

plugins {
    kotlin("jvm") version "2.1.0"
    id("com.gradleup.shadow") version "8.3.5"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
}

repositories {
    mavenCentral()
    maven(url = "https://packages.confluent.io/maven/")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-call-id:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")

    // Metrics and Prometheus
    implementation("io.ktor:ktor-server-metrics-micrometer:$ktorVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:$micrometerRegistryVersion")

    // Database
    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")
    testImplementation("io.zonky.test:embedded-postgres:$postgresEmbeddedVersion")

    // Kafka
    implementation("org.apache.kafka:kafka_2.13:$kafkaVersion")

    // (De-)serialization
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonDatatypeVersion")

    // Tests
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("com.nimbusds:nimbus-jose-jwt:$nimbusJoseJwtVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
}

kotlin {
    jvmToolchain(21)
}

tasks {
    jar {
        manifest.attributes["Main-Class"] = "no.nav.syfo.AppKt"
    }

    create("printVersion") {
        doLast {
            println(project.version)
        }
    }

    shadowJar {
        mergeServiceFiles()
        archiveBaseName.set("app")
        archiveClassifier.set("")
        archiveVersion.set("")
    }

    test {
        useJUnitPlatform()
        testLogging.showStandardStreams = true
    }
}
