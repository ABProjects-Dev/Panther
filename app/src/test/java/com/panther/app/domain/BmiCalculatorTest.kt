package com.panther.app.domain

import com.panther.app.domain.model.BmiCalculator
import com.panther.app.domain.model.BmiCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class BmiCalculatorTest {

    @Test
    fun returnsNullWhenEitherMeasurementIsMissing() {
        assertNull(BmiCalculator.calculate(null, 80.0))
        assertNull(BmiCalculator.calculate(180.0, null))
        assertNull(BmiCalculator.calculate(null, null))
    }

    @Test
    fun returnsNullForNonFiniteValues() {
        assertNull(BmiCalculator.calculate(Double.NaN, 80.0))
        assertNull(BmiCalculator.calculate(180.0, Double.POSITIVE_INFINITY))
    }

    @Test
    fun returnsNullOutsideTheAcceptedMeasurementRange() {
        assertNull(BmiCalculator.calculate(29.9, 70.0))
        assertNull(BmiCalculator.calculate(300.1, 70.0))
        assertNull(BmiCalculator.calculate(180.0, 0.9))
        assertNull(BmiCalculator.calculate(180.0, 500.1))
    }

    @Test
    fun acceptsTheRangeBoundaries() {
        // 30 cm / 1 kg and 300 cm / 500 kg sit exactly on the accepted limits, so a result exists
        // even though the derived numbers are extreme.
        assertNotNull(BmiCalculator.calculate(30.0, 1.0))
        assertNotNull(BmiCalculator.calculate(300.0, 500.0))
    }

    @Test
    fun classifiesUnderweight() {
        val result = BmiCalculator.calculate(180.0, 55.0)
        assertEquals(17.0, result!!.value, 0.01)
        assertEquals(BmiCategory.UNDERWEIGHT, result.category)
    }

    @Test
    fun classifiesNormalAtTheLowerBoundary() {
        // 18.5 is the first "normal" value after rounding.
        val result = BmiCalculator.calculate(170.0, 53.5)
        assertEquals(BmiCategory.NORMAL, result!!.category)
    }

    @Test
    fun classifiesOverweight() {
        val result = BmiCalculator.calculate(180.0, 87.0)
        assertEquals(26.9, result!!.value, 0.01)
        assertEquals(BmiCategory.OVERWEIGHT, result.category)
    }

    @Test
    fun classifiesObese() {
        val result = BmiCalculator.calculate(180.0, 110.0)
        assertEquals(34.0, result!!.value, 0.01)
        assertEquals(BmiCategory.OBESE, result.category)
    }

    @Test
    fun roundsToOneDecimalPlace() {
        // 70 / 1.75^2 = 22.857... -> 22.9
        val result = BmiCalculator.calculate(175.0, 70.0)
        assertEquals(22.9, result!!.value, 0.0001)
    }
}
