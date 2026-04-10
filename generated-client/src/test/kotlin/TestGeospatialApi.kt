import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import com.stadiamaps.api.GeospatialApi
import com.stadiamaps.api.auth.ApiKeyAuth
import com.stadiamaps.api.infrastructure.ApiClient
import com.stadiamaps.api.models.Coordinate
import com.stadiamaps.api.models.HeightRequest
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

internal class TestGeospatialApi {
    private val seoul = Coordinate(37.56, 126.99)
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
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals("Asia/Seoul", body.tzId)
    }

    @Test
    fun testTzLookupV2() {
        val res = service.tzLookupV2(seoul.lat, seoul.lon).execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals("Asia/Seoul", body.tzId)
        assertTrue(body.utcOffset != 0, "Expected a non-zero UTC offset")
        assertTrue(body.localRfc3339Timestamp.isNotEmpty(), "Expected a non-empty RFC 3339 timestamp")
    }

    @Test
    fun testElevation() {
        val req = HeightRequest(id = "Seoul", shape = listOf(seoul))
        val res = service.elevation(req).execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals(req.id, body.id)
        assertTrue(body.height!!.isNotEmpty(), "Expected at least one height")
        assertTrue(body.height.first() > 0, "Expected the height to be greater than zero")
    }

    @Test
    fun testElevationRange() {
        val req = HeightRequest(id = "Seoul", shape = listOf(seoul), range = true)
        val res = service.elevation(req).execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")
        val rangeHeight = body.rangeHeight ?: fail("Expected rangeHeight to be present")

        assertEquals(req.id, body.id)
        assertTrue(rangeHeight.isNotEmpty(), "Expected at least one height")
        assertEquals(0f, rangeHeight.first()[0], "Expected the range to be zero for the first element")
        assertTrue(rangeHeight.first()[1].let { it != null && it > 0 }, "Expected the height to be greater than zero")
    }
}
