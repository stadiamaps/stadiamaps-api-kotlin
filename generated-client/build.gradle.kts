import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.hidetake.gradle.swagger.generator.GenerateSwaggerCode

plugins {
    kotlin("jvm")
    id("org.hidetake.swagger.generator") version "2.19.2"
}

repositories {
    mavenCentral()
}

dependencies {
    val retrofitVersion = "2.9.0"

    swaggerCodegen("org.openapitools:openapi-generator-cli:6.5.0")

    // Dependencies of the generated code. Check out `build.gradle` in your build folder later if you're curious.
    api("com.squareup.moshi:moshi-kotlin:1.13.0")
    api("com.squareup.moshi:moshi-adapters:1.13.0")
    api("com.squareup.okhttp3:logging-interceptor:4.10.0")
    api("com.squareup.retrofit2:retrofit:$retrofitVersion")
    api("com.squareup.retrofit2:converter-moshi:$retrofitVersion")
    api("com.squareup.retrofit2:converter-scalars:$retrofitVersion")
}

kotlin {
    jvmToolchain(17)
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

      setInputFile(file("openapi.yaml"))
      code(delegateClosureOf<GenerateSwaggerCode> {
          language = "kotlin"
          library = "jvm-retrofit2"
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