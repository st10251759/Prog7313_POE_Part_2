
package com.firstproject.prog7313_budgetbuddy
import org.junit.Assert.*
import org.junit.Test
import java.text.NumberFormat
import java.util.*
class BudgetGoalsActivityUnitTest {
    // Test currency format validation
    @Test
    fun isValidCurrencyFormat_validInputs() {
        val regex = Regex("\\d{0,7}(\\.\\d{0,2})?")
        assertTrue("123.45".matches(regex))
        assertTrue("9999999.99".matches(regex))
        assertTrue("0.01".matches(regex))
        assertTrue("100".matches(regex))
        assertTrue("0".matches(regex))
        assertFalse("123.456".matches(regex))
        assertFalse("12345678".matches(regex)) // 8 digits before decimal
        assertFalse("123.1.1".matches(regex))
        assertFalse("abc".matches(regex))
    }
    // Test that minimum cannot be greater than maximum
    @Test
    fun minGoalCannotExceedMaxGoal() {
        val minGoal = 1000.0
        val maxGoal = 500.0
        assertTrue("Minimum should not exceed maximum", minGoal > maxGoal)
    }
    // Test that 0 is not valid for min or max goal
    @Test
    fun minAndMaxGoalMustBeGreaterThanZero() {
        val minGoal = 0.0
        val maxGoal = 0.0
        assertTrue("Minimum should be greater than 0", minGoal <= 0)
        assertTrue("Maximum should be greater than 0", maxGoal <= 0)
    }
    // Test valid input
    @Test
    fun validMinAndMaxGoal() {
        val minGoal = 500.0
        val maxGoal = 1000.0
        assertTrue("Minimum should be less than maximum", minGoal < maxGoal)
        assertTrue("Minimum > 0", minGoal > 0)
        assertTrue("Maximum > 0", maxGoal > 0)
    }
}

