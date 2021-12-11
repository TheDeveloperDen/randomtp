plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "net.developerden"
version = "1.0.2-SNAPSHOT"

java {
    targetCompatibility = JavaVersion.VERSION_17
    sourceCompatibility = targetCompatibility
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.bristermitten.me/repository/maven-releases/")
    maven("https://repo.bristermitten.me/repository/maven-snapshots/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.18-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("com.github.Redempt:RedClaims:298da532bd")

    implementation("me.bristermitten:mittenlib-core:1.0-SNAPSHOT")
    implementation("me.bristermitten:mittenlib-minimessage:1.0-SNAPSHOT")
    annotationProcessor("me.bristermitten:mittenlib-annotation-processor:1.0-SNAPSHOT")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.shadowJar {
    minimize()
    relocate("net.kyori", "me.bristermitten.randomtp.libs.kyori")
    relocate("me.bristermitten.mittenlib", "me.bristermitten.randomtp.libs.mittenlib")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
