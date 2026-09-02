plugins {
    kotlin("jvm") version "2.1.10"
    id("io.gitlab.arturbosch.detekt") version "1.23.5"
}

group = "org.bytebloom"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}