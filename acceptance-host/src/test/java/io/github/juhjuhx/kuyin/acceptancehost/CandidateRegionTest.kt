package io.github.juhjuhx.kuyin.acceptancehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CandidateRegionTest {
    // Hand-checked layout: header y100..148, strip y148..196, keys start196.
    private val clear = CandidateRegionBounds(300, 100, 348, 148)

    @Test fun nextRowCandidateAtLeftEdgeIsEligible() {
        assertTrue(isInCandidateRegion(CandidateRegionBounds(8, 150, 70, 194), clear, 196))
    }
    @Test fun longCandidateAcrossTheStripIsEligible() {
        assertTrue(isInCandidateRegion(CandidateRegionBounds(8, 148, 355, 196), clear, 196))
    }
    @Test fun headerControlToTheRightIsNotACandidate() {
        assertFalse(isInCandidateRegion(CandidateRegionBounds(350, 100, 398, 148), clear, 196))
    }
    @Test fun sameLabelKeyboardKeyIsNotACandidate() {
        assertFalse(isInCandidateRegion(CandidateRegionBounds(8, 196, 70, 244), clear, 196))
    }
    @Test fun targetCrossingKeyboardBoundaryIsRejected() {
        assertFalse(isInCandidateRegion(CandidateRegionBounds(8, 150, 70, 220), clear, 196))
    }
    @Test fun missingKeyboardAnchorFailsClosed() {
        assertFalse(isInCandidateRegion(CandidateRegionBounds(350, 110, 398, 140), clear, null))
    }
    @Test fun reversedHeaderKeyboardOrderFailsClosed() {
        assertFalse(isInCandidateRegion(CandidateRegionBounds(350, 110, 398, 140), clear, 90))
    }
    @Test fun zeroWidthTargetIsRejected() {
        assertFalse(isInCandidateRegion(CandidateRegionBounds(350, 110, 350, 140), clear, 196))
    }
}
