plugins {
    kotlin("jvm")
    application
}

group = "com.stadiamaps"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val retrofitVersion: String by project

    testImplementation(kotlin("test"))

    implementation(project(":generated-client"))

    val moshiVersion = "1.15.1"
    implementation("com.squareup.moshi:moshi-kotlin:$moshiVersion")
    implementation("com.squareup.moshi:moshi-adapters:$moshiVersion")
    implementation("com.squareup.moshi:moshi-adapters:1.14.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-moshi:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-scalars:$retrofitVersion")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}