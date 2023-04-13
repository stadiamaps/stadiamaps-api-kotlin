# OpenAPI Kotlin Demo

This is a simple example project demonstrating how to integrate the
[Stadia Maps APIs](https://docs.stadiamaps.com/api-reference/) into a Kotlin project
with an auto-generated OpenAPI client.

This can serve as a rough template for any JVM-based language integration, as well as a
playground to try out the Stadia Maps API in an IDE such as IntelliJ.

## Running the project

First, you'll need a Stadia Maps API key! You can get one for free (no credit card required).
Get one at https://client.stadiamaps.com/. After signing up, create a property and you'll get
the flow. Then, you'll need to copy your API key and put it in `Main.kt` (see
the placeholder variable at the top of the file).

You can either run in IntelliJ by clicking the play button in the gutter
next to the `main` function, or by running `./gradlew run --quiet` (`gradlew.bat`
on Windows) in your terminal.

If you'd like a detailed explanation of what's going on, check out our full tutorial
[over here](https://docs.stadiamaps.com/tutorials/getting-started-with-geospatial-apis-in-kotlin-openapi/)!
