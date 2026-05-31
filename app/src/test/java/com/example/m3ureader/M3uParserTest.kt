package com.example.m3ureader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class M3uParserTest {

    @Test
    fun parsesExtinfWithAttributesAndName() {
        val content = """
            #EXTM3U
            #EXTINF:-1 tvg-id="c1" tvg-name="Canal Um" tvg-logo="http://logo/1.png" group-title="Filmes",Canal Um
            http://stream/1
        """.trimIndent()

        val channels = M3uParser.parse(content)

        assertEquals(1, channels.size)
        val ch = channels[0]
        assertEquals("Canal Um", ch.name)
        assertEquals("http://stream/1", ch.url)
        assertEquals("http://logo/1.png", ch.logo)
        assertEquals("Filmes", ch.group)
    }

    @Test
    fun parsesMultipleChannels() {
        val content = """
            #EXTM3U
            #EXTINF:-1 group-title="A",Canal A
            http://stream/a
            #EXTINF:-1 group-title="B",Canal B
            https://stream/b
        """.trimIndent()

        val channels = M3uParser.parse(content)

        assertEquals(2, channels.size)
        assertEquals("Canal A", channels[0].name)
        assertEquals("A", channels[0].group)
        assertEquals("Canal B", channels[1].name)
        assertEquals("https://stream/b", channels[1].url)
    }

    @Test
    fun handlesUrlWithoutExtinf() {
        val content = """
            #EXTM3U
            http://stream/no-info
        """.trimIndent()

        val channels = M3uParser.parse(content)

        assertEquals(1, channels.size)
        assertEquals("http://stream/no-info", channels[0].name)
        assertEquals("http://stream/no-info", channels[0].url)
        assertNull(channels[0].group)
    }

    @Test
    fun ignoresBlankAndUnknownDirectives() {
        val content = """
            #EXTM3U

            #EXT-X-VERSION:3
            #EXTINF:-1,Canal C
            http://stream/c

        """.trimIndent()

        val channels = M3uParser.parse(content)

        assertEquals(1, channels.size)
        assertEquals("Canal C", channels[0].name)
        assertTrue(channels[0].group == null)
    }
}
