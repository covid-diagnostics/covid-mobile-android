package com.example.coronadiagnosticapp.ui.activities

import java.util.*

/**
 *
 * @param length the maximum length
 */
class SMA(val length: Int) {
    private val values = LinkedList<Double>()
    private var sum = 0.0
    private var average = 0.0

    init {
        require(length > 0) { "length must be greater than zero" }
    }

    fun currentAverage() = average

    /**
     * Compute the moving average.
     * Synchronised so that no changes in the underlying data is made during calculation.
     * @param value The value
     * @return The average
     */
    @Synchronized
    fun compute(value: Double): Double {
        if (values.size == length && length > 0) {
            sum -= values.first
            values.removeFirst()
        }
        sum += value
        values.addLast(value)
        average = sum / values.size
        return average
    }
}