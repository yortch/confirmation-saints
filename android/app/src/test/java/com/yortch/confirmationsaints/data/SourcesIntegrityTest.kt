package com.yortch.confirmationsaints.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Integrity test for the sources schema (see
 * .squad/decisions/inbox/gandalf-sources-schema.md §6).
 *
 * Parses saints-en.json and saints-es.json directly as JSON (without the
 * [com.yortch.confirmationsaints.data.model.Saint] data class) so the test
 * remains robust against model refactors. Guards against regressions in the
 * new `sources: [{name, url}]` schema.
 */
class SourcesIntegrityTest {

    private val json = Json { ignoreUnknownKeys = true }

    private data class SaintSources(val id: String, val entries: List<Pair<String, String>>)

    private fun loadSaints(fileName: String): List<SaintSources> {
        // JVM unit tests run with cwd = android/app. SharedContent is the canonical
        // data source at the repo root (see docs/android-architecture.md).
        val candidates = listOf(
            File("../../SharedContent/saints/$fileName"),
            File("SharedContent/saints/$fileName"),
        )
        val file = candidates.firstOrNull { it.exists() }
            ?: error("Could not locate $fileName. Tried: ${candidates.map { it.absolutePath }}")

        val root = json.parseToJsonElement(file.readText()).jsonObject
        val saints = root["saints"]?.jsonArray
            ?: error("$fileName missing top-level 'saints' array")

        return saints.map { element ->
            val obj = element.jsonObject
            val id = obj["id"]?.jsonPrimitive?.content
                ?: error("Saint entry missing 'id' in $fileName")
            val sourcesEl = obj["sources"]
                ?: error("Saint '$id' in $fileName missing 'sources' field")
            val entries = sourcesEl.jsonArray.map { s ->
                val so = s.jsonObject
                val name = so["name"]?.jsonPrimitive?.content ?: ""
                val url = so["url"]?.jsonPrimitive?.content ?: ""
                name to url
            }
            SaintSources(id, entries)
        }
    }

    private fun assertWellFormed(saints: List<SaintSources>, lang: String) {
        saints.forEach { saint ->
            assertTrue(
                "[$lang] Saint '${saint.id}' has empty sources list",
                saint.entries.isNotEmpty()
            )
            saint.entries.forEachIndexed { index, (name, url) ->
                assertTrue(
                    "[$lang] Saint '${saint.id}' source[$index] has blank name",
                    name.isNotBlank()
                )
                assertTrue(
                    "[$lang] Saint '${saint.id}' source[$index] ('$name') has blank url",
                    url.isNotBlank()
                )
                assertTrue(
                    "[$lang] Saint '${saint.id}' source[$index] ('$name') url must start with https://, got: $url",
                    url.startsWith("https://")
                )
            }
        }
    }

    @Test
    fun test_en_sources_well_formed() {
        assertWellFormed(loadSaints("saints-en.json"), "en")
    }

    @Test
    fun test_es_sources_well_formed() {
        assertWellFormed(loadSaints("saints-es.json"), "es")
    }

    @Test
    fun test_en_es_parity() {
        val en = loadSaints("saints-en.json").associateBy { it.id }
        val es = loadSaints("saints-es.json").associateBy { it.id }

        assertEquals(
            "EN and ES saint id sets must match (only in EN: ${en.keys - es.keys}; only in ES: ${es.keys - en.keys})",
            en.keys,
            es.keys
        )

        en.keys.forEach { id ->
            val enEntries = en.getValue(id).entries
            val esEntries = es.getValue(id).entries
            assertEquals(
                "Saint '$id' source count differs between EN (${enEntries.size}) and ES (${esEntries.size})",
                enEntries.size,
                esEntries.size
            )
            val enUrls = enEntries.map { it.second }.toSet()
            val esUrls = esEntries.map { it.second }.toSet()
            assertEquals(
                "Saint '$id' URL set differs between EN and ES " +
                    "(only in EN: ${enUrls - esUrls}; only in ES: ${esUrls - enUrls})",
                enUrls,
                esUrls
            )
        }
    }
}
