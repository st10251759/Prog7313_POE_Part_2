package com.firstproject.prog7313_budgetbuddy

/*
 --------------------------------Project Details----------------------------------
 STUDENT NUMBERS: ST10251759   | ST10252746      | ST10266994
 STUDENT NAMES: Cameron Chetty | Theshara Narain | Alyssia Sookdeo
 COURSE: BCAD Year 3
 MODULE: Programming 3C
 MODULE CODE: PROG7313
 ASSESSMENT: Portfolio of Evidence (POE) Part 2
 Github REPO LINK: https://github.com/st10251759/Prog7313_POE_Part_2
 --------------------------------Project Details----------------------------------
*/

/*
 --------------------------------Code Attribution----------------------------------
 Title: est code using JUnit in JVM – tutorial | Kotlin Documentation
 Author: Kotlin
 Date Published: 06 November 2024
 Date Accessed: 07 February 2025
 Code Version: v21.20
 Availability: https://kotlinlang.org/docs/jvm-test-using-junit.html#what-s-next
  --------------------------------Code Attribution----------------------------------
*/

import org.junit.Assert.*
import org.junit.Test

// A unit test class for testing core validation logic related to budget goals input
class BudgetGoalsActivityUnitTest {

    // -----------------------------
    // Test: Validating currency format
    // -----------------------------
    @Test
    fun isValidCurrencyFormat_validInputs() {
        // Regex explanation:
        // \d{0,7}       → Up to 7 digits before the decimal
        // (\.\d{0,2})?  → Optional group: a dot followed by up to 2 decimal digits
        val regex = Regex("\\d{0,7}(\\.\\d{0,2})?")

        // These assertions test valid currency inputs
        assertTrue("Should be valid", "123.45".matches(regex))         // Valid: 2 decimal places
        assertTrue("Should be valid", "9999999.99".matches(regex))     // Valid: max digits before and after decimal
        assertTrue("Should be valid", "0.01".matches(regex))           // Valid: small value with decimal
        assertTrue("Should be valid", "100".matches(regex))            // Valid: no decimal
        assertTrue("Should be valid", "0".matches(regex))              // Valid: zero with no decimal

        // These assertions test **invalid** inputs
        assertFalse("Should not be valid", "123.456".matches(regex))   // Invalid: 3 decimal places
        assertFalse("Should not be valid", "12345678".matches(regex))  // Invalid: 8 digits before decimal
        assertFalse("Should not be valid", "123.1.1".matches(regex))   // Invalid: multiple decimals
        assertFalse("Should not be valid", "abc".matches(regex))       // Invalid: non-numeric string
    }

    // -----------------------------
    // Helper methods for logic validation
    // These replicate business logic found in BudgetGoalsActivity
    // -----------------------------

    // Returns true if the minimum value is greater than the maximum (which is invalid)
    private fun isMinGreaterThanMax(min: Double, max: Double): Boolean = min > max

    // Returns true if a value is greater than 0 (zero is not allowed)
    private fun isGreaterThanZero(value: Double): Boolean = value > 0

    // -----------------------------
    // Test: Minimum goal cannot exceed maximum goal
    // -----------------------------
    @Test
    fun minGoalCannotExceedMaxGoal() {
        // Case 1: 1000 > 500 → invalid → should return true → .not() makes it false → assertFalse
        assertFalse("minGoal=1000, maxGoal=500 should fail", isMinGreaterThanMax(1000.0, 500.0).not())

        // Case 2: 500 < 1000 → valid → should return false → .not() makes it true → assertTrue
        assertTrue("minGoal=500, maxGoal=1000 should pass", isMinGreaterThanMax(500.0, 1000.0).not())
    }

    // -----------------------------
    // Test: Goals must be greater than zero
    // -----------------------------
    @Test
    fun minAndMaxGoalMustBeGreaterThanZero() {
        // Zero is not a valid goal value
        assertFalse("Minimum 0 should not be valid", isGreaterThanZero(0.0))
        assertFalse("Maximum 0 should not be valid", isGreaterThanZero(0.0))

        // Positive values are valid
        assertTrue("Minimum 100 should be valid", isGreaterThanZero(100.0))
        assertTrue("Maximum 500 should be valid", isGreaterThanZero(500.0))
    }

    // -----------------------------
    // Test: Valid min and max goals pass all conditions
    // -----------------------------
    @Test
    fun validMinAndMaxGoal() {
        val minGoal = 500.0
        val maxGoal = 1000.0

        // Check that min is less than max
        assertTrue("Minimum should be less than maximum", minGoal < maxGoal)

        // Ensure both are greater than zero
        assertTrue("Minimum > 0", isGreaterThanZero(minGoal))
        assertTrue("Maximum > 0", isGreaterThanZero(maxGoal))
    }
}
