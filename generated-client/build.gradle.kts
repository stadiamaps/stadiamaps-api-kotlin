import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.hidetake.gradle.swagger.generator.GenerateSwaggerCode

plugins {
    kotlin("jvm")
    id("org.hidetake.swagger.generator") version "2.19.2"
    `maven-publish`
    signing
    id("org.jetbrains.dokka") version "1.9.20"
    id("tech.yanand.maven-central-publish") version "1.1.1"
}

val stagingDir = layout.buildDirectory.dir("staging-deploy").get()

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

java {
    withSourcesJar()
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

tasks.named<Jar>("sourcesJar").configure {
    dependsOn("generateSwaggerCode")
}

tasks.dokkaHtml {
    dependsOn("generateSwaggerCode")
    outputDirectory.set(buildDir.resolve("dokkaHtml"))
    dokkaSourceSets {
        configureEach {
            includeNonPublic.set(false)
            reportUndocumented.set(true)
            skipEmptyPackages.set(true)
            skipDeprecated.set(false)
            jdkVersion.set(11)
        }
    }
}

tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn("dokkaHtml")
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml)
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

        maven {
            url = stagingDir.asFile.toURI()
        }
    }

    publications {
        create<MavenPublication>("publication") {
            groupId = "com.stadiamaps"
            artifactId = "api"
            version = "3.2.1"

            from(components["java"])

            // Add the Dokka Javadoc jar as an artifact
            artifact(tasks["dokkaJavadocJar"]) {
                classifier = "javadoc"
            }


            pom {
                name = "Stadia Maps Kotlin API"
                description = "An API client library for accessing the Stadia Maps Geospatial APIs"
                url = "https://github.com/stadiamaps/stadiamaps-api-kotlin"
                inceptionYear = "2023"
                licenses {
                    license {
                        name = "BSD-3-Clause"
                        url = "https://spdx.org/licenses/BSD-3-Clause.html"
                    }
                }
                developers {
                    developer {
                        name = "Ian Wagner"
                        organization = "Stadia Maps"
                        organizationUrl = "https://stadiamaps.com/"
                    }
                }
                scm {
                    connection = "scm:git:https://github.com/stadiamaps/stadiamaps-api-kotlin.git"
                    developerConnection = "scm:git:ssh://github.com/stadiamaps/stadiamaps-api-kotlin.git"
                    url = "http://github.com/stadiamaps/stadiamaps-api-kotlin"
                }
            }

        }
    }
}

signing {
    val signingKey = System.getenv("GPG_PRIVATE_KEY")
    val signingKeyPassphrase = System.getenv("MAVEN_GPG_PASSPHRASE")
    useInMemoryPgpKeys(signingKey, signingKeyPassphrase)

    sign(publishing.publications["publication"])
}

mavenCentral {
    repoDir = stagingDir
    authToken = System.getenv("MAVEN_CENTRAL_TOKEN")
    publishingType = "AUTOMATIC"
}
