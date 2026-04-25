package com.yortch.confirmationsaints.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.yortch.confirmationsaints.data.repository.SaintRepository
import com.yortch.confirmationsaints.localization.AppLanguage
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Tests SaintRepository loading from bundled JSON assets.
 *
 * Contract under test (from docs/android-architecture.md §3.5 and the
 * "SharedContent/ is the Canonical Cross-Platform Data Source" decision):
 *  - Repository loads from `assets/saints-{en,es}.json`.
 *  - Both languages must expose the same 103 saint ids.
 */
@RunWith(RobolectricTestRunner::class)
class SaintRepositoryTest {

    private lateinit var context: Context
    private lateinit var repository: SaintRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        val json = Json { ignoreUnknownKeys = true }
        repository = SaintRepository(context, json)
    }

    @Test
    fun should_load_english_saints_list() {
        val saints = repository.loadSaints(AppLanguage.EN)
        assertTrue("EN saints list should not be empty", saints.isNotEmpty())
        
        // Verify basic fields round-trip
        val first = saints.first()
        assertNotNull("Saint should have an id", first.id)
        assertNotNull("Saint should have a name", first.name)
        assertNotNull("Saint should have a biography", first.biography)
    }

    @Test
    fun should_return_exactly_103_saints_for_each_language() {
        val enSaints = repository.loadSaints(AppLanguage.EN)
        val esSaints = repository.loadSaints(AppLanguage.ES)
        assertEquals("EN should have 103 saints", 103, enSaints.size)
        assertEquals("ES should have 103 saints", 103, esSaints.size)
    }

    @Test
    fun should_return_identical_id_set_across_languages() {
        val enIds = repository.loadSaints(AppLanguage.EN).map { it.id }.toSet()
        val esIds = repository.loadSaints(AppLanguage.ES).map { it.id }.toSet()
        assertEquals("EN and ES should have identical saint ID sets", enIds, esIds)
    }

    @Test
    fun should_deserialize_optional_fields_without_throwing_on_null() {
        val saints = repository.loadSaints(AppLanguage.EN)
        // Find a pre-congregation saint that has null canonizationDate
        val preConSaint = saints.firstOrNull { it.canonizationDate == null }
        assertNotNull("At least one saint should have null canonizationDate", preConSaint)
    }

    @Test
    fun should_expose_image_filename_matching_saint_id() {
        val saints = repository.loadSaints(AppLanguage.EN)
        saints.forEach { saint ->
            val expectedFilename = "${saint.id}.jpg"
            assertEquals(
                "Image filename for ${saint.id} should match saint id",
                expectedFilename,
                saint.image?.filename
            )
        }
    }
}
