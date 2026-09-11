// SPDX-License-Identifier: GPL-3.0-only
// AutoWrong: added as part of the AutoWrong fork of HeliBoard
package helium314.keyboard.latin.autowrong

import android.content.Context

/**
 * WordsDestroyedCounter — tracks how many correctly-spelled words AutoWrong has mangled.
 *
 * - [totalCount]: persisted in SharedPreferences across app restarts.
 * - [sessionCount]: in-memory only, resets each time the IME service restarts.
 *
 * Thread-safety: increment() may be called from the IME input thread; the SharedPreferences
 * write is fire-and-forget (apply(), not commit()), which is safe.
 */
object WordsDestroyedCounter {

    private const val PREFS_NAME = "autowrong_prefs"
    private const val KEY_TOTAL = "autowrong_words_destroyed_total"

    @Volatile
    var sessionCount: Int = 0
        private set

    fun getTotalCount(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_TOTAL, 0L)
    }

    fun increment(context: Context) {
        sessionCount++
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getLong(KEY_TOTAL, 0L)
        prefs.edit().putLong(KEY_TOTAL, current + 1L).apply()
    }

    fun resetSession() {
        sessionCount = 0
    }
}
