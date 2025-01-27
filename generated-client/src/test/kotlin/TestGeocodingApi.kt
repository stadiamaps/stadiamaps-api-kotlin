import com.stadiamaps.api.GeocodingApi
import com.stadiamaps.api.auth.ApiKeyAuth
import com.stadiamaps.api.infrastructure.ApiClient
import com.stadiamaps.api.infrastructure.CollectionFormats
import com.stadiamaps.api.models.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.fail

internal class TestGeocodingApi {
    private val apiKey = System.getenv("STADIA_API_KEY") ?: throw RuntimeException("API Key not set")
    private lateinit var service: GeocodingApi
    private val address = "Põhja pst 27"

    @BeforeEach
    fun setUp() {
        val client = ApiClient()
        client.addAuthorization("ApiKeyAuth", ApiKeyAuth("query", "api_key", apiKey))
        service = client.createService(GeocodingApi::class.java)
    }

    @Test
    fun testAutocomplete() {
        val res = service.autocomplete(address).execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals("Estonia", body.features.first().properties!!.country)
        assertEquals(GeocodingLayer.address, body.features.first().properties!!.layer)
    }

    @Test
    fun testSearch() {
        val res = service.search(address).execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals("Estonia", body.features.first().properties!!.country)
        assertEquals(GeocodingLayer.address, body.features.first().properties!!.layer)
    }

    @Test
    fun testSearchStructured() {
        val res = service.searchStructured(address = address, country = "Estonia").execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals("Estonia", body.features.first().properties!!.country)
        assertEquals(GeocodingLayer.address, body.features.first().properties!!.layer)
    }

    @Test
    fun testSearchBulk() {
        val reqs = listOf(
            BulkRequestFactory.searchRequest(SearchQuery(text = address)),
            BulkRequestFactory.searchStructuredRequest(SearchStructuredQuery(address = address, country = "Estonia"))
        )
        val res = service.searchBulk(reqs).execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        for (rec in body) {
            assertEquals(200, rec.status)
            assertEquals("Estonia", rec.response!!.features.first().properties!!.country)
            assertEquals(GeocodingLayer.address, rec.response!!.features.first().properties!!.layer)
        }
    }

    @Test
    fun testReverse() {
        val res = service.reverse(59.444351, 24.750645).execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals("Estonia", body.features.first().properties!!.country)
        assertEquals(GeocodingLayer.address, body.features.first().properties!!.layer)
    }

    @Test
    fun testReverseUncommonLayer() {
        val res = service.reverse(24.750645, 59.444351).execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals(GeocodingLayer.marinearea, body.features.first().properties!!.layer)
    }

    @Test
    fun testPlace() {
        val res = service.place(CollectionFormats.CSVParams("openstreetmap:address:way/109867749")).execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals("Estonia", body.features.first().properties!!.country)
        assertEquals(GeocodingLayer.address, body.features.first().properties!!.layer)
    }
}