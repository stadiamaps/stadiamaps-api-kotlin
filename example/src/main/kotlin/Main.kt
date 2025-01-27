import com.stadiamaps.api.GeocodingApi
import com.stadiamaps.api.GeospatialApi
import com.stadiamaps.api.RoutingApi
import com.stadiamaps.api.auth.ApiKeyAuth
import com.stadiamaps.api.infrastructure.ApiClient
import com.stadiamaps.api.models.*

private const val apiKey = "YOUR-API-KEY"

val seoul = Coordinate(37.56, 126.99)
val tll = RoutingWaypoint(59.416411, 24.798778)
val kultuurikatel = RoutingWaypoint(59.44436, 24.75071)

// Note: All code in this example is blocking for demonstration purposes.
// If you're using Kotlin with coroutines,
// you can also use these asynchronously within suspend functions.
// Synchronous code can enqueue a callback to avoid blocking
// (you'll definitely want to do one of these instead when on the main thread of an app).
// See the docs for details: https://square.github.io/retrofit/2.x/retrofit/retrofit2/Call.html

fun testGeospatialApis(client: ApiClient) {
    val geoService = client.createService(GeospatialApi::class.java)

    val tzRes = geoService.tzLookup(seoul.lat, seoul.lon).execute()
    if (tzRes.isSuccessful) {
        println(tzRes.body())
    } else {
        println("Request failed with error code ${tzRes.code()}")
    }

    val heightRes = geoService.elevation(HeightRequest(shape = listOf(seoul))).execute()
    if (heightRes.isSuccessful) {
        println(heightRes.body())
    } else {
        println("Request failed with error code ${heightRes.code()}")
    }
}

fun testRoutingApis(client: ApiClient) {
    val routingService = client.createService(RoutingApi::class.java)

    val waypoints = listOf(tll, kultuurikatel)
    val route = routingService.route(RouteRequest(waypoints, CostingModel.auto)).execute()

    if (route.isSuccessful) {
        val summary = route.body()?.trip?.summary
        println("Found route with length of ${summary?.length}km and duration of ${summary?.time} seconds")
    } else {
        println("Request failed with error code ${route.code()}")
    }

    val matrix = routingService.timeDistanceMatrix(MatrixRequest(listOf(MatrixWaypoint(tll.lat, tll.lon)), listOf(MatrixWaypoint(kultuurikatel.lat, kultuurikatel.lon)), MatrixCostingModel.pedestrian)).execute()
    if (matrix.isSuccessful) {
        println("Found matrix response! ${matrix.body()?.sourcesToTargets}")
    } else {
        println("Matrix failed with error code: ${matrix.code()}")
    }
}

fun testGeocodingApis(client: ApiClient) {
    val geocodingService = client.createService(GeocodingApi::class.java)

    val structuredSearch = geocodingService.searchStructured(address = "11 Wall St", region = "New York", locality = "New York").execute()

    if (structuredSearch.isSuccessful) {
        println("Found structured search result: ${structuredSearch.body()?.features?.first()}")
    } else {
        println("Request failed with error code ${structuredSearch.code()}")
    }

    val reverseGeocode = geocodingService.reverse(kultuurikatel.lat, kultuurikatel.lon).execute()

    if (reverseGeocode.isSuccessful) {
        println("Found reverse geocode result: ${reverseGeocode.body()?.features?.first()}")
    } else {
        println("Request failed with error code ${reverseGeocode.code()}")
    }
}

fun main(args: Array<String>) {
    val client = ApiClient()
    client.addAuthorization("ApiKeyAuth", ApiKeyAuth("query", "api_key", apiKey))

    testGeospatialApis(client)
    testRoutingApis(client)
    testGeocodingApis(client)
}
