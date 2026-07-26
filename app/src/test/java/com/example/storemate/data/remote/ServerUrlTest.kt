package com.example.storemate.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The server address is typed by a shop owner, not a developer, so the parser
 * has to be forgiving about scheme, port and trailing slashes.
 */
class ServerUrlTest {

    @Test
    fun `bare host and port gets an http scheme`() {
        assertEquals("http://192.168.1.10:8080/", ServerUrl.normalize("192.168.1.10:8080"))
    }

    @Test
    fun `bare host without a port gets the default StoreMate port`() {
        assertEquals("http://192.168.1.10:8080/", ServerUrl.normalize("192.168.1.10"))
    }

    @Test
    fun `explicit http scheme is preserved`() {
        assertEquals("http://shop.local:9000/", ServerUrl.normalize("http://shop.local:9000"))
    }

    @Test
    fun `https on its default port is not rewritten to 8080`() {
        assertEquals("https://shop.example.com/", ServerUrl.normalize("https://shop.example.com"))
    }

    @Test
    fun `surrounding whitespace and trailing slash are ignored`() {
        assertEquals("http://192.168.1.10:8080/", ServerUrl.normalize("  192.168.1.10:8080/  "))
    }

    @Test
    fun `blank input is rejected`() {
        assertNull(ServerUrl.normalize("   "))
    }

    @Test
    fun `garbage input is rejected rather than silently accepted`() {
        assertNull(ServerUrl.normalize("http://"))
    }
}
