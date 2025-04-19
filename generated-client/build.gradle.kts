buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.yaml:snakeyaml:2.0")
    }
}

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor

plugins {
    kotlin("jvm")
    id("org.openapi.generator") version "7.12.0"
    `maven-publish`
    signing
    id("org.jetbrains.dokka") version "1.9.20"
    id("tech.yanand.maven-central-publish") version "1.3.0"
}

val stagingDir = layout.buildDirectory.dir("staging-deploy").get()
val generatedClientDir = layout.buildDirectory.dir("generated").get()

repositories {
    mavenCentral()
}

dependencies {
    val retrofitVersion: String by project

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

tasks.register("patchOpenAPISpec") {
    doLast {
        val openApiFile = file("openapi.yaml")
        val originalText = openApiFile.readText()

        // We have to do some post-processing of the YAML spec here,
        // because of https://github.com/OpenAPITools/openapi-generator/issues/18167.
        // The way the Kotlin OpenAPI generator works for `oneOf` models,
        // the result is a superset of all the properties of the

        // Parse the document with SnakeYAML
        val yaml = Yaml()
        val root = yaml.load<Map<String, Any>>(originalText)?.toMutableMap() ?: mutableMapOf()

        // Remove the required Valhalla route response properties (trip in this case)
        val components = (root["components"] as? Map<*, *>)!!.toMutableMap()
        val schemas = (components["schemas"] as? Map<*, *>)!!.toMutableMap()

        val routeResponse = (schemas["routeResponse"] as? Map<*, *>)!!.toMutableMap()
        routeResponse.remove("required")
        schemas["routeResponse"] = routeResponse

        val osrmBaseApiResponse = (schemas["osrmBaseApiResponse"] as? Map<*, *>)!!.toMutableMap()
        osrmBaseApiResponse.remove("required")
        schemas["osrmBaseApiResponse"] = osrmBaseApiResponse

        components["schemas"] = schemas
        root["components"] = components

        openApiFile.writeText(yaml.dump(root))

    }
}

tasks.named("patchOpenAPISpec").configure {
    dependsOn("downloadOpenAPISpec")
}

tasks.named("openApiGenerate").configure {
    // Temporarily commented out because the generator is so buggy.
//    dependsOn("patchOpenAPISpec")
}

tasks.named<Jar>("sourcesJar").configure {
    dependsOn("openApiGenerate")
}

tasks.dokkaHtml {
    dependsOn("openApiGenerate")
    outputDirectory.set(layout.buildDirectory.dir("dokkaHtml"))
    dokkaSourceSets {
        configureEach {
            reportUndocumented.set(true)
            jdkVersion.set(11)
        }
    }
}

tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn("dokkaHtml")
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml)
}

openApiGenerate {
    generatorName.set("kotlin")
    library.set("jvm-retrofit2")
    groupId.set("com.stadiamaps")
    inputSpec.set(file("openapi.yaml").path)
    apiPackage.set("com.stadiamaps.api")
    packageName.set("com.stadiamaps.api")
    modelPackage.set("com.stadiamaps.api.models")
    outputDir.set(generatedClientDir.toString())
}

// Comment this out if you do NOT want the code gen to run every time you build.
// There is an HTTP cache by default, so it won't necessarily make a request every single build.
tasks.compileKotlin.configure {
    dependsOn(tasks.openApiGenerate)
}

sourceSets {
    val main by getting
    main.kotlin.srcDir("${generatedClientDir}/src/main/kotlin")
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
            name = "local"
            url = stagingDir.asFile.toURI()
        }
    }

    publications {
        create<MavenPublication>("publication") {
            groupId = "com.stadiamaps"
            artifactId = "api"
            version = "5.1.1"

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
