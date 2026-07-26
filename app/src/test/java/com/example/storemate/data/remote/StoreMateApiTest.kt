package com.example.storemate.data.remote

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

/**
 * Contract tests against response bodies captured verbatim from the Spring Boot
 * backend, so a change in the wire format fails here rather than on a shop's
 * tablet.
 */
class StoreMateApiTest {

    private lateinit var server: MockWebServer
    private lateinit var config: NetworkConfig
    private lateinit var api: StoreMateApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        config = NetworkConfig().apply { updateServer(server.url("/")) }
        api = ApiFactory.createApi(ApiFactory.createOkHttpClient(config, SessionExpiryNotifier()))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `requests are redirected from the placeholder base url to the configured server`() = runTest {
        server.enqueue(jsonResponse(LOGIN_BODY))

        api.login(com.example.storemate.data.remote.dto.AuthRequestDto("demo", "demo"))

        val recorded = server.takeRequest()
        assertEquals(server.hostName, recorded.requestUrl?.host)
        assertEquals(server.port, recorded.requestUrl?.port)
        assertEquals("/api/auth/login", recorded.path)
    }

    @Test
    fun `login response is parsed`() = runTest {
        server.enqueue(jsonResponse(LOGIN_BODY))

        val response = api.login(com.example.storemate.data.remote.dto.AuthRequestDto("demo", "demo"))

        assertEquals("demo", response.username)
        assertEquals("Demo Store", response.storeName)
        assertEquals(43200L, response.expiresInSeconds)
        assertTrue(response.token.isNotBlank())
    }

    @Test
    fun `login and register are sent without an Authorization header`() = runTest {
        config.updateToken("stale-token")
        server.enqueue(jsonResponse(LOGIN_BODY))

        api.login(com.example.storemate.data.remote.dto.AuthRequestDto("demo", "demo"))

        assertNull(server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `authenticated requests carry the bearer token`() = runTest {
        config.updateToken("abc123")
        server.enqueue(jsonResponse(PRODUCTS_PAGE_BODY))

        api.getProducts(page = 0, size = 200)

        assertEquals("Bearer abc123", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `paged product response is parsed including a detached supplier`() = runTest {
        server.enqueue(jsonResponse(PRODUCTS_PAGE_BODY))

        val page = api.getProducts(page = 0, size = 200)

        assertEquals(2, page.content.size)
        assertTrue(page.last)
        assertEquals(2L, page.totalElements)

        val detached = page.content.first { it.id == 1L }
        assertNull(detached.supplierId)
        assertEquals(6, detached.currentStockLevel)

        val attached = page.content.first { it.id == 2L }
        assertEquals(2L, attached.supplierId)
        assertEquals("Supplier Maria", attached.supplierName)
    }

    @Test
    fun `a 204 delete does not fail parsing`() = runTest {
        server.enqueue(MockResponse().setResponseCode(204))

        api.deleteProduct(1L)

        assertEquals("DELETE", server.takeRequest().method)
    }

    @Test
    fun `an error body maps to a message the user can act on`() = runTest {
        server.enqueue(jsonResponse(OVERSELL_ERROR_BODY, code = 400))

        val thrown = runCatching {
            api.createTransaction(
                com.example.storemate.data.remote.dto.CreateTransactionDto(
                    date = "2026-07-26T11:00:00",
                    type = "SALE",
                    productId = 1,
                    quantity = 999
                )
            )
        }.exceptionOrNull()

        val mapped = (thrown as HttpException).toUserFacingException()
        assertEquals("Cannot sell 999 of 'Carnati EDITED': only 6 in stock", mapped.message)
    }

    @Test
    fun `a 401 is surfaced as an expired session so the app can sign out`() = runTest {
        server.enqueue(jsonResponse(UNAUTHORIZED_BODY, code = 401))

        val thrown = runCatching { api.me() }.exceptionOrNull()

        assertTrue((thrown as HttpException).toUserFacingException() is SessionExpiredException)
    }

    @Test
    fun `an unreachable server is reported as unreachable rather than as a crash`() = runTest {
        server.shutdown()

        val thrown = runCatching { api.me() }.exceptionOrNull()

        assertTrue(thrown!!.toUserFacingException() is ServerUnreachableException)
    }

    private fun jsonResponse(body: String, code: Int = 200) = MockResponse()
        .setResponseCode(code)
        .setHeader("Content-Type", "application/json")
        .setBody(body)

    private companion object {
        // Captured from the running backend.
        const val LOGIN_BODY = """
            {"token":"eyJhbGciOiJIUzI1NiJ9.abc.def","username":"demo",
             "storeName":"Demo Store","expiresInSeconds":43200}
        """

        const val PRODUCTS_PAGE_BODY = """
            {"content":[
              {"id":1,"name":"Carnati EDITED","description":"d","price":30.0,
               "category":"Alimente","barcode":"111","supplierId":null,
               "supplierName":null,"currentStockLevel":6,"minimumStockLevel":2},
              {"id":2,"name":"Brânză de burduf","description":"Brânză sărată","price":18.0,
               "category":"Alimente","barcode":"9876543210987","supplierId":2,
               "supplierName":"Supplier Maria","currentStockLevel":20,"minimumStockLevel":5}],
             "page":0,"size":50,"totalElements":2,"totalPages":1,"last":true}
        """

        const val OVERSELL_ERROR_BODY = """
            {"status":400,"error":"Bad Request",
             "message":"Cannot sell 999 of 'Carnati EDITED': only 6 in stock","fieldErrors":{}}
        """

        const val UNAUTHORIZED_BODY = """
            {"status":401,"error":"Unauthorized",
             "message":"Authentication required — your session may have expired","fieldErrors":{}}
        """
    }
}
