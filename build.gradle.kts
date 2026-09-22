plugins {
    id("java")
    id("com.gradleup.shadow") version "9.6.1"
}

group = "mu.nada"
version = findProperty("releaseVersion")?.toString() ?: "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:4.0.0")
    annotationProcessor("com.velocitypowered:velocity-api:4.0.0")
    
    implementation("com.h2database:h2:2.5.250")
    implementation("com.zaxxer:HikariCP:7.1.0")
    implementation("at.favre.lib:bcrypt:0.10.2")

    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.velocitypowered:velocity-api:4.0.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    test {
        useJUnitPlatform()
    }

    shadowJar {
        archiveClassifier.set("")
        relocate("com.zaxxer.hikari", "mu.nada.nadamuauth.libs.hikari")
        relocate("org.h2", "mu.nada.nadamuauth.libs.h2")
        relocate("at.favre.lib", "mu.nada.nadamuauth.libs.bcrypt")
    }

    build {
        dependsOn(shadowJar)
    }
}