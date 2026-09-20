package com.yortch.confirmationsaints.ui.components

import androidx.compose.ui.layout.ContentScale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The official FMA logo used for `maria-troncatti` must be displayed
 * uncropped, with its shape/colors/design unaltered (legal requirement).
 * Every other saint keeps the existing circular-cropped photo treatment.
 */
class SaintImageTest {
    @Test fun `maria-troncatti uses Fit scaling to avoid cropping the official logo`() {
        assertEquals(ContentScale.Fit, contentScaleForSaint("maria-troncatti"))
    }

    @Test fun `maria-troncatti is not clipped to a circle`() {
        assertFalse(shouldClipToCircle("maria-troncatti"))
    }

    @Test fun `other saints keep Crop scaling and circular clip`() {
        assertEquals(ContentScale.Crop, contentScaleForSaint("therese-of-lisieux"))
        assertTrue(shouldClipToCircle("therese-of-lisieux"))
    }
}
