package com.citysemaphores.game

/**
 * Simple timer for time intervals in the game.
 *
 * Used for:
 * - Vehicle spawn intervals
 * - Intersection blocking times
 * - Gridlock grace period
 *
 * @property interval The time interval in seconds
 * @property elapsed Elapsed time since last reset
 */
data class GameTimer(
    val interval: Float,
    val elapsed: Float = 0f
) {
    init {
        require(interval > 0f) { "Timer interval must be positive, got $interval" }
        require(elapsed >= 0f) { "Timer elapsed time cannot be negative" }
    }

    /**
     * Updates the timer with elapsed time
     *
     * @param deltaTime Elapsed time in seconds
     * @return Updated timer
     */
    fun tick(deltaTime: Float): GameTimer =
        copy(elapsed = elapsed + deltaTime)

    /**
     * Checks if the interval has expired
     */
    fun isExpired(): Boolean =
        elapsed >= interval

    /**
     * Resets the timer to 0
     */
    fun reset(): GameTimer =
        copy(elapsed = 0f)

    /**
     * Returns progress as a value between 0.0 and 1.0
     */
    fun progress(): Float =
        (elapsed / interval).coerceIn(0f, 1f)

    /**
     * Returns remaining time
     */
    fun remaining(): Float =
        (interval - elapsed).coerceAtLeast(0f)

    override fun toString(): String {
        val elapsedStr = elapsed.toDecimalString(1)
        val intervalStr = interval.toDecimalString(1)
        return "GameTimer(${elapsedStr}s / ${intervalStr}s)"
    }

    companion object {
        /**
         * Creates a new timer with the given interval
         */
        fun create(intervalSeconds: Float): GameTimer =
            GameTimer(interval = intervalSeconds, elapsed = 0f)
    }
}

/**
 * Formats a Float to a string with specified decimal places.
 * Multiplatform compatible implementation.
 */
private fun Float.toDecimalString(decimals: Int): String {
    val multiplier = when (decimals) {
        0 -> 1
        1 -> 10
        2 -> 100
        3 -> 1000
        else -> {
            // Calculate 10^decimals without using pow
            var result = 1
            repeat(decimals) { result *= 10 }
            result
        }
    }
    val rounded = (this * multiplier).toInt().toFloat() / multiplier
    return rounded.toString()
}
