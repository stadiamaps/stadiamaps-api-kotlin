import com.stadiamaps.api.GeocodingApi
import com.stadiamaps.api.auth.ApiKeyAuth
import com.stadiamaps.api.infrastructure.ApiClient
import com.stadiamaps.api.models.GeocodingLayer
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.fail

internal class TestEuEndpoint {
    @Test
    fun testAutocompleteEuEndpoint() {
        val apiKey = System.getenv("STADIA_API_KEY") ?: throw RuntimeException("API Key not set")

        val client = ApiClient(baseUrl = "https://api-eu.stadiamaps.com")
        client.addAuthorization("ApiKeyAuth", ApiKeyAuth("query", "api_key", apiKey))

        val service = client.createService(GeocodingApi::class.java)

        val res = service.autocomplete("Põhja pst 27").execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals("Estonia", body.features.first().properties!!.country)
        assertEquals(GeocodingLayer.address, body.features.first().properties!!.layer)
    }
}