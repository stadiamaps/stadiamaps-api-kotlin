import org.openapitools.client.apis.GeospatialApi
import org.openapitools.client.apis.RoutingApi
import org.openapitools.client.auth.ApiKeyAuth
import org.openapitools.client.infrastructure.ApiClient
import org.openapitools.client.models.*

const val apiKey = "YOUR-API-KEY"

val seoul = Coordinate(37.56, 126.99)
val tll = RoutingWaypoint(59.416411, 24.798778)
val kultuurikatel = RoutingWaypoint(59.444169, 24.751779)

fun testGeospatialApis(client: ApiClient) {
    val geoService = client.createService(GeospatialApi::class.java)

    val tzRes = geoService.tzLookup(seoul.lat, seoul.lon).execute().body()
    println(tzRes)

    val heightRes = geoService.elevationService(HeightRequest(shape = listOf(seoul))).execute().body()
    println(heightRes)
}

fun testRoutingApis(client: ApiClient) {
    val routingService = client.createService(RoutingApi::class.java)

    val waypoints = listOf(tll, kultuurikatel)
    val route = routingService.route(RouteRequest(waypoints, CostingModel.auto)).execute().body()

    if (route != null) {
        val summary = route.trip.summary
        println("Found route with length of ${summary.length}km and duration of ${summary.time} seconds")
    }
}

fun main(args: Array<String>) {
    val client = ApiClient()
    client.addAuthorization("ApiKeyAuth", ApiKeyAuth("query", "api_key", apiKey))

    testGeospatialApis(client)
    testRoutingApis(client)
}