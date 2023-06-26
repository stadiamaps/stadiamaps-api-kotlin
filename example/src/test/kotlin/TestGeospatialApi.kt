import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openapitools.client.apis.GeospatialApi
import org.openapitools.client.auth.ApiKeyAuth
import org.openapitools.client.infrastructure.ApiClient
import org.openapitools.client.models.HeightRequest
import kotlin.test.*

internal class TestGeospatialApi {
    private val apiKey = System.getenv("STADIA_API_KEY") ?: throw RuntimeException("API Key not set")
    private lateinit var service: GeospatialApi

    @BeforeEach
    fun setUp() {
        val client = ApiClient()
        client.addAuthorization("ApiKeyAuth", ApiKeyAuth("query", "api_key", apiKey))
        service = client.createService(GeospatialApi::class.java)
    }

    @Test
    fun testTzLookup() {
        val res = service.tzLookup(seoul.lat, seoul.lon).execute()
        assertTrue(res.isSuccessful)
        assertEquals("Asia/Seoul", res.body()?.tzId)
    }

    @Test
    fun testElevation() {
        val req = HeightRequest(id = "Seoul", shape = listOf(seoul))
        val res = service.elevation(req).execute()
        assertTrue(res.isSuccessful)

        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals(req.id, body.id)
        assertTrue((body.height?.count() ?: 0) > 0, "Expected at least one height")
        assertTrue((body.height?.first() ?: 0) > 0, "Expected the height to be greater than zero")
    }
}