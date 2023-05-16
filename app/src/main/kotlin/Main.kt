import org.openapitools.client.apis.GeocodingApi
import org.openapitools.client.apis.GeospatialApi
import org.openapitools.client.apis.RoutingApi
import org.openapitools.client.auth.ApiKeyAuth
import org.openapitools.client.infrastructure.ApiClient
import org.openapitools.client.models.*

const val apiKey = "YOUR-API-KEY"

val seoul = Coordinate(37.56, 126.99)
val tll = RoutingWaypoint(59.416411, 24.798778)
val kultuurikatel = RoutingWaypoint(59.44436, 24.75071)

fun testGeospatialApis(client: ApiClient) {
    val geoService = client.createService(GeospatialApi::class.java)

    val tzRes = geoService.tzLookup(seoul.lat, seoul.lon).execute()
    if (tzRes.isSuccessful) {
        println(tzRes.body())
    } else {
        println("Request failed with error code ${tzRes.code()}")
    }

    val heightRes = geoService.elevationService(HeightRequest(shape = listOf(seoul))).execute()
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