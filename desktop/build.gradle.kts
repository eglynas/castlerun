plugins {
    id("org.jetbrains.kotlin.jvm") // No version here!
}

group = "org.example"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":core")) // Connects to your shared game logic
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:1.12.1") // Desktop backend
    implementation("com.badlogicgames.gdx:gdx-platform:1.12.1:natives-desktop") // Desktop native libraries
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(23)
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}
