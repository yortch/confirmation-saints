package com.yortch.confirmationsaints.data

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Disabled

/**
 * STUB — bodies to be filled in once Aragorn lands Phases 2–7 and the final
 * SaintRepository method signatures stabilize. See android/app/src/test/README.md.
 *
 * Contract under test (from docs/android-architecture.md §3.5 and the
 * "SharedContent/ is the Canonical Cross-Platform Data Source" decision):
 *  - Repository loads from `assets/SharedContent/saints/saints-{en,es}.json`.
 *  - Both languages must expose the same 70 saint ids.
 */
@Disabled("Stub — awaiting SaintRepository implementation (Aragorn, Phase 2)")
class SaintRepositoryTest {

    @Test
    fun should_load_english_saints_list() {
        // TODO: Instantiate SaintRepository with a test Context whose assets
        // expose SharedContent/. Assert loadSaints(AppLanguage.EN) returns a
        // non-empty list whose first-N fields round-trip through kotlinx.serialization
        // without throwing (ignoreUnknownKeys = true).
    }

    @Test
    fun should_return_exactly_70_saints_for_each_language() {
        // TODO: Assert loadSaints(EN).size == 70 and loadSaints(ES).size == 70.
        // 70 is the committed roster size (see .squad/decisions.md).
    }

    @Test
    fun should_return_identical_id_set_across_languages() {
        // TODO: val enIds = loadSaints(EN).map { it.id }.toSet()
        //       val esIds = loadSaints(ES).map { it.id }.toSet()
        //       assertEquals(enIds, esIds)  — canonical ids are English per 2026-04-21 decision.
    }

    @Test
    fun should_deserialize_optional_fields_without_throwing_on_null() {
        // TODO: Verify a saint with canonizationDate = null (e.g. pre-congregation
        // saints like "patrick", "pius-x") deserializes cleanly.
    }

    @Test
    fun should_expose_image_filename_matching_saint_id() {
        // TODO: For every saint, assert image?.filename == "${saint.id}.jpg"
        // (one image per id — cross-platform contract).
    }
}
