import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.hidetake.gradle.swagger.generator.GenerateSwaggerCode

plugins {
    kotlin("jvm")
    id("org.hidetake.swagger.generator") version "2.19.2"
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    val retrofitVersion: String by project

    swaggerCodegen("org.openapitools:openapi-generator-cli:7.5.0")

    // Dependencies of the generated code. Check out `build.gradle` in your build folder later if you're curious.
    val moshiVersion = "1.15.1"
    implementation("com.squareup.moshi:moshi-kotlin:$moshiVersion")
    implementation("com.squareup.moshi:moshi-adapters:$moshiVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-moshi:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-scalars:$retrofitVersion")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}

// Bit of a hack because the generator can't download files at the time of this writing:
// https://github.com/int128/gradle-swagger-generator-plugin/issues/167
open class DownloadResource : DefaultTask() {
    @get:Input
    lateinit var sourceUrl: String

    @get:OutputFile
    lateinit var target: File

    @TaskAction
    fun download() {
        ant.withGroovyBuilder {
            "get"("src" to sourceUrl, "dest" to target)
        }
    }
}

tasks.register("downloadOpenAPISpec", DownloadResource::class.java) {
    sourceUrl = "https://api.stadiamaps.com/openapi.yaml"
    target = File("openapi.yaml")
}

tasks.named("generateSwaggerCode").configure {
    dependsOn("downloadOpenAPISpec")
}

swaggerSources {
  register("stadiamaps") {
      val validationTask = validation
      validationTask.dependsOn("downloadOpenAPISpec")

      setInputFile(file("openapi.yaml"))
      code(delegateClosureOf<GenerateSwaggerCode> {
          language = "kotlin"
          library = "jvm-retrofit2"
          additionalProperties = mapOf("groupId" to "com.stadiamaps", "packageName" to "com.stadiamaps.api")
          dependsOn(validationTask)
      })
  }
}

// Comment this out if you do NOT want the code gen to run every time you build.
// There is an HTTP cache by default, so it won't necessarily make a request every single build.
tasks.compileKotlin.configure {
    dependsOn(tasks.generateSwaggerCode)
}

sourceSets {
    val main by getting
    val stadiamaps by swaggerSources.getting
    main.kotlin.srcDir("${stadiamaps.code.outputDir}/src/main/kotlin")
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/stadiamaps/stadiamaps-api-kotlin")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }

    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])

            groupId = "com.stadiamaps"
            artifactId = "api"
            version = "2.1.0"
        }
    }
}
