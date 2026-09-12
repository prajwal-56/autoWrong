// SPDX-License-Identifier: GPL-3.0-only
// AutoWrong: added as part of the AutoWrong fork of HeliBoard
package helium314.keyboard.latin.autowrong

/**
 * TypoGenerator — generates realistic QWERTY fat-finger typos.
 *
 * This is a pure Kotlin object with zero Android dependencies.
 * It can be unit-tested in isolation on the JVM.
 *
 * Usage:
 *   val mangled = TypoGenerator.mangle("hello")  // e.g. "heklo"
 *
 * Rules:
 *  - Words shorter than 3 characters are returned unchanged.
 *  - All-caps words (heuristic: word == word.uppercase() && length > 1) are returned unchanged.
 *  - For words of length > 3, a character in the interior range [1, length-2] is chosen.
 *  - For words of exactly length 3, any position [0, 2] is eligible.
 *  - The chosen character is replaced with a random QWERTY-adjacent key.
 *  - Case of the original character is preserved in the replacement.
 *  - If the chosen character has no adjacency entry (e.g. a digit or punctuation), a different
 *    position is tried up to MAX_RETRIES times; if none work, the word is returned unchanged.
 */
object TypoGenerator {

    // Standard QWERTY layout adjacency map.
    // Each key maps to the list of keys physically adjacent to it on a QWERTY keyboard.
    // Layout reference (rows):
    //   q w e r t y u i o p
    //   a s d f g h j k l
    //   z x c v b n m
    private val ADJACENCY: Map<Char, List<Char>> = mapOf(
        'q' to listOf('w', 'a', 's'),
        'w' to listOf('q', 'e', 'a', 's', 'd'),
        'e' to listOf('w', 'r', 's', 'd', 'f'),
        'r' to listOf('e', 't', 'd', 'f', 'g'),
        't' to listOf('r', 'y', 'f', 'g', 'h'),
        'y' to listOf('t', 'u', 'g', 'h', 'j'),
        'u' to listOf('y', 'i', 'h', 'j', 'k'),
        'i' to listOf('u', 'o', 'j', 'k', 'l'),
        'o' to listOf('i', 'p', 'k', 'l'),
        'p' to listOf('o', 'l'),
        'a' to listOf('q', 'w', 's', 'z'),
        's' to listOf('a', 'w', 'e', 'd', 'x', 'z'),
        'd' to listOf('s', 'e', 'r', 'f', 'c', 'x'),
        'f' to listOf('d', 'r', 't', 'g', 'v', 'c'),
        'g' to listOf('f', 't', 'y', 'h', 'b', 'v'),
        'h' to listOf('g', 'y', 'u', 'j', 'n', 'b'),
        'j' to listOf('h', 'u', 'i', 'k', 'm', 'n'),
        'k' to listOf('j', 'i', 'o', 'l', 'm'),
        'l' to listOf('k', 'o', 'p'),
        'z' to listOf('a', 's', 'x'),
        'x' to listOf('z', 's', 'd', 'c'),
        'c' to listOf('x', 'd', 'f', 'v'),
        'v' to listOf('c', 'f', 'g', 'b'),
        'b' to listOf('v', 'g', 'h', 'n'),
        'n' to listOf('b', 'h', 'j', 'm'),
        'm' to listOf('n', 'j', 'k')
    )

    private const val MAX_RETRIES = 5

    /**
     * Returns a "typo'd" version of [word] by replacing one interior character with a
     * QWERTY-adjacent key. Returns the word unchanged if it cannot be meaningfully mangled
     * (too short, all-caps acronym, no adjacent key found).
     */
    @JvmStatic
    @JvmOverloads
    fun mangle(word: String, random: java.util.Random = java.util.Random()): String {
        if (word.length < 3) return word

        // Skip all-caps acronyms: word == word.uppercase() and length > 1
        if (word == word.uppercase() && word.length > 1) return word

        // For words longer than 3 chars, avoid first and last position
        val range = if (word.length > 3) 1 until (word.length - 1) else word.indices
        val positions = range.toMutableList()
        positions.shuffle(random)

        for (pos in positions.take(MAX_RETRIES)) {
            val originalChar = word[pos]
            val lookupKey = originalChar.lowercaseChar()
            val adjacentKeys = ADJACENCY[lookupKey] ?: continue
            if (adjacentKeys.isEmpty()) continue

            val replacement = adjacentKeys[random.nextInt(adjacentKeys.size)]
            // Preserve case of the original character
            val replacementChar = if (originalChar.isUpperCase()) replacement.uppercaseChar() else replacement

            return word.substring(0, pos) + replacementChar + word.substring(pos + 1)
        }

        // Fallback: couldn't find a good position — return the word untouched
        return word
    }
}
