# Stadia Maps Kotlin SDK

This SDK helps you access the full range of geospatial APIs from Stadia Maps using Kotlin and other JVM languages.
We've derived everything from our official API spec, so you'll get all the goodies like autocomplete, model definitions,
and other documentation in your favorite editor.

This package specifically targets Kotlin and retrofit2. If you'd prefer to customize the generated code to
your stack (ex: using okhttp), we've written a tutorial on generating code within JVM projects
[over here](https://docs.stadiamaps.com/tutorials/getting-started-with-geospatial-apis-in-kotlin-openapi/).

## Getting started with Gradle

### Authenticate to GitHub Packages

You'll need an access token to install from GitHub packages. See the [GitHub Packages docs](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#authenticating-to-github-packages)
for details.

### Add the repository to build script

Add the repository to your `repositories` block like so.

Kotlin build script (`build.gradle.kts`):

```kotlin
repositories {
    mavenCentral()
    
    maven {
        url = uri("https://maven.pkg.github.com/stadiamaps/stadiamaps-api-kotlin")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("TOKEN")
        }
    }
}
```

Groovy build script (`build.gradle`):

```groovy
repositories {
    mavenCentral()
    
    maven {
        url = uri("https://maven.pkg.github.com/stadiamaps/stadiamaps-api-kotlin")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.token") ?: System.getenv("TOKEN")
        }
   }
}
```

### Add dependencies

Now you're ready to add the package and its dependencies.

Kotlin:

```kotlin
dependencies {
    val retrofitVersion = "2.9.0"
    
    // API package
    implementation("com.stadiamaps:api:1.0.0")

    // Dependencies
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    implementation("com.squareup.moshi:moshi-adapters:1.15.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-moshi:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-scalars:$retrofitVersion")
}
```

Groovy:

```groovy
dependencies {
    def retrofitVersion = "2.9.0"
    
    // API package
    implementation 'com.stadiamaps:api:1.0.0'

    // Dependencies
    implementation 'com.squareup.moshi:moshi-kotlin:1.15.1'
    implementation 'com.squareup.moshi:moshi-adapters:1.15.1'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.10.0'
    implementation "com.squareup.retrofit2:retrofit:${retrofitVersion}"
    implementation "com.squareup.retrofit2:converter-moshi:${retrofitVersion}"
    implementation "com.squareup.retrofit2:converter-scalars:${retrofitVersion}"
}
```

## Usage

Next, you'll need a Stadia Maps API key.
You can create an API key for free [here](https://client.stadiamaps.com/signup/?utm_source=github&utm_campaign=sdk_readme&utm_content=kotlin_readme)
(no credit card required).

See the [example app](example/src/main/kotlin/Main.kt) for usage examples.

## Documentation

Official documentation lives at [docs.stadiamaps.com](https://docs.stadiamaps.com/?utm_source=github&utm_campaign=sdk_readme&utm_content=kotlin_readme),
where we have both long-form prose explanations of each endpoint and an interactive [API reference](https://docs.stadiamaps.com/api-reference/?utm_source=github&utm_campaign=sdk_readme&utm_content=kotlin_readme).
If you're using an IDE like IntelliJ though,
the auto-complete and built-in documentation functionality is even easier to use than the online generated reference.