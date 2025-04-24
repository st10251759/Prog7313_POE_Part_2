package com.firstproject.prog7313_budgetbuddy

import org.junit.Assert.*
import org.junit.Test

class BudgetGoalsActivityUnitTest {

    // Test currency format validation
    @Test
    fun isValidCurrencyFormat_validInputs() {
        val regex = Regex("\\d{0,7}(\\.\\d{0,2})?")
        assertTrue("Should be valid", "123.45".matches(regex))
        assertTrue("Should be valid", "9999999.99".matches(regex))
        assertTrue("Should be valid", "0.01".matches(regex))
        assertTrue("Should be valid", "100".matches(regex))
        assertTrue("Should be valid", "0".matches(regex))
        assertFalse("Should not be valid", "123.456".matches(regex))
        assertFalse("Should not be valid", "12345678".matches(regex))
        assertFalse("Should not be valid", "123.1.1".matches(regex))
        assertFalse("Should not be valid", "abc".matches(regex))
    }

    // Budget validation identical to logic in the method in the BudgetGoalsActivity, except for UI display
    private fun isMinGreaterThanMax(min: Double, max: Double): Boolean = min > max
    private fun isGreaterThanZero(value: Double): Boolean = value > 0

    // Test: Minimum Goal cannot be greater than maximum
    @Test
    fun minGoalCannotExceedMaxGoal() {
        assertFalse("minGoal=1000, maxGoal=500 should fail", isMinGreaterThanMax(1000.0, 500.0).not())
        assertTrue("minGoal=500, maxGoal=1000 should pass", isMinGreaterThanMax(500.0, 1000.0).not())
    }

    // Test: 0 is not valid for Minimum or Maximum goal
    @Test
    fun minAndMaxGoalMustBeGreaterThanZero() {
        assertFalse("Minimum 0 should not be valid", isGreaterThanZero(0.0))
        assertFalse("Maximum 0 should not be valid", isGreaterThanZero(0.0))
        assertTrue("Minimum 100 should be valid", isGreaterThanZero(100.0))
        assertTrue("Maximum 500 should be valid", isGreaterThanZero(500.0))
    }

    // Test: valid Minimum and Maximum
    @Test
    fun validMinAndMaxGoal() {
        val minGoal = 500.0
        val maxGoal = 1000.0
        assertTrue("Minimum should be less than maximum", minGoal < maxGoal)
        assertTrue("Minimum > 0", isGreaterThanZero(minGoal))
        assertTrue("Maximum > 0", isGreaterThanZero(maxGoal))
    }
}

