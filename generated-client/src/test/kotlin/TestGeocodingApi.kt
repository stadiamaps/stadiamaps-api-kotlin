import com.stadiamaps.api.GeocodingApi
import com.stadiamaps.api.auth.ApiKeyAuth
import com.stadiamaps.api.infrastructure.ApiClient
import com.stadiamaps.api.infrastructure.CollectionFormats
import com.stadiamaps.api.models.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
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
        val res = service.autocomplete(address, lang = "en").execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals("Estonia", body.features.first().properties!!.country)
        assertEquals("address", body.features.first().properties!!.layer)
    }

    @Test
    fun testAutocompleteV2() {
        val res = service.autocompleteV2(address, lang = "en").execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertNull(body.features.first().properties.context)
        assertEquals("address", body.features.first().properties.layer)
    }

    @Test
    fun testSearch() {
        val res = service.search(address).execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals("Estonia", body.features.first().properties!!.country)
        assertEquals("address", body.features.first().properties!!.layer)
    }

    @Test
    fun testSearchV2() {
        val res = service.searchV2(address).execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals("EST", body.features.first().properties.context?.iso3166A3)
        assertEquals("address", body.features.first().properties.layer)
    }

    @Test
    fun testSearchStructured() {
        val res = service.searchStructured(address = address, country = "Estonia").execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals("Estonia", body.features.first().properties!!.country)
        assertEquals("address", body.features.first().properties!!.layer)
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
            assertEquals("address", rec.response!!.features.first().properties!!.layer)
        }
    }

    @Test
    fun testReverse() {
        val res = service.reverse(59.444351, 24.750645).execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals("Estonia", body.features.first().properties!!.country)
    }

    @Test
    fun testReverseUncommonLayer() {
        val res = service.reverse(24.750645, 59.444351).execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals("marinearea", body.features.first().properties!!.layer)
    }

    @Test
    fun testReverseV2() {
        val res = service.reverseV2(59.444351, 24.750645, lang = "en").execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals("EST", body.features.first().properties.context?.iso3166A3)
    }

    @Test
    fun testReverseUncommonLayerV2() {
        val res = service.reverseV2(24.750645, 59.444351, lang = "en").execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals("marinearea", body.features.first().properties.layer)
    }

    @Test
    fun testPlaceDetailsV1() {
        val res = service.placeDetails(CollectionFormats.CSVParams("openstreetmap:address:way/109867749")).execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals("Estonia", body.features.first().properties!!.country)
        assertEquals("address", body.features.first().properties!!.layer)
    }

    @Test
    fun testPlaceDetailsV2() {
        val res = service.placeDetailsV2(CollectionFormats.CSVParams("openstreetmap:address:way/109867749"), lang = "en").execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals("Estonia", body.features.first().properties.context?.whosonfirst?.country?.name)
        assertEquals("EST", body.features.first().properties.context?.iso3166A3)
        assertEquals("address", body.features.first().properties.layer)
    }
}