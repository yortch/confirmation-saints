package com.yortch.confirmationsaints.data.model

import com.yortch.confirmationsaints.data.json.SaintsFile
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Smoke test: verify that a minimal JSON payload matching the SharedContent
 * schema deserializes into the [Saint] / [SaintsFile] model. Guards against
 * accidental model field renames breaking production asset loading.
 */
class SaintParsingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    @Test fun `decodes SaintsFile envelope`() {
        val payload = """
            {
              "version": "1.0",
              "language": "en",
              "lastUpdated": "2025-01-01",
              "saints": [
                {
                  "id": "therese-lisieux",
                  "name": "St. Thérèse of Lisieux",
                  "feastDay": "10-01",
                  "birthDate": "1873-01-02",
                  "deathDate": "1897-09-30",
                  "country": "France",
                  "region": "Europe",
                  "gender": "female",
                  "lifeState": "religious",
                  "ageCategory": "young",
                  "patronOf": ["Missionaries", "Florists"],
                  "tags": ["Doctor of the Church"],
                  "affinities": ["simplicity"],
                  "biography": "The Little Flower.",
                  "image": { "filename": "therese.jpg", "attribution": "PD" },
                  "sources": [{"name": "Catholic Encyclopedia", "url": "https://example.org/therese"}]
                }
              ]
            }
        """.trimIndent()

        val file = json.decodeFromString(SaintsFile.serializer(), payload)
        assertEquals(1, file.saints.size)
        val saint = file.saints.first()
        assertEquals("therese-lisieux", saint.id)
        assertEquals("10-01", saint.feastDay)
        assertTrue(saint.patronOf.contains("Missionaries"))
        assertEquals("therese.jpg", saint.image?.filename)
        assertTrue(saint.isYoung)
    }

    @Test fun `tolerates missing optional fields`() {
        val payload = """
            {"version":"1.0","language":"en","lastUpdated":"2025-01-01",
             "saints":[{"id":"x","name":"X","feastDay":"01-01","biography":"b"}]}
        """.trimIndent()
        val file = json.decodeFromString(SaintsFile.serializer(), payload)
        val saint = file.saints.single()
        assertEquals(emptyList<String>(), saint.patronOf)
        assertEquals(emptyList<String>(), saint.tags)
        assertEquals(null, saint.image)
    }
}
