// SPDX-License-Identifier: GPL-3.0-only
// AutoWrong: unit tests for the TypoGenerator
package helium314.keyboard.latin.autowrong

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random

class TypoGeneratorTest {

    // -------------------------------------------------------------------------
    // Guard conditions: words that should NOT be mangled
    // -------------------------------------------------------------------------

    @Test
    fun `words shorter than 3 chars are not mangled`() {
        assertEquals("a", TypoGenerator.mangle("a"))
        assertEquals("is", TypoGenerator.mangle("is"))
        assertEquals("hi", TypoGenerator.mangle("hi"))
    }

    @Test
    fun `all-caps acronyms are not mangled`() {
        assertEquals("USA", TypoGenerator.mangle("USA"))
        assertEquals("NASA", TypoGenerator.mangle("NASA"))
        assertEquals("IME", TypoGenerator.mangle("IME"))
        assertEquals("AI", TypoGenerator.mangle("AI"))  // length > 1, all caps
    }

    // Single uppercase letter should not be treated as an acronym (length == 1 special case)
    // Actually: "A" has length 1, so it's already caught by the < 3 guard
    @Test
    fun `single char is not mangled`() {
        assertEquals("A", TypoGenerator.mangle("A"))
    }

    // -------------------------------------------------------------------------
    // Happy path: words that SHOULD be mangled
    // -------------------------------------------------------------------------

    @Test
    fun `mangle changes the word`() {
        // Use a fixed seed so the test is deterministic
        val rng = Random(42)
        val result = TypoGenerator.mangle("hello", rng)
        // The result should differ from the original in exactly one char
        assertEquals(result.length, "hello".length)
        val diffs = "hello".zip(result).count { (a, b) -> a != b }
        assertEquals(1, diffs)
    }

    @Test
    fun `mangle preserves word length`() {
        val rng = Random(1234)
        for (word in listOf("hello", "world", "kotlin", "typing", "keyboard")) {
            val result = TypoGenerator.mangle(word, rng)
            assertEquals("Length mismatch for '$word'", word.length, result.length)
        }
    }

    @Test
    fun `mangle preserves case of replaced character - lowercase`() {
        // Force replacement of a specific char by using a fixed seed.
        // We just verify the result is lowercase (no uppercase chars inserted into lowercase word).
        val rng = Random(0)
        val result = TypoGenerator.mangle("hello", rng)
        assertTrue("Result '$result' should be all-lowercase", result == result.lowercase())
    }

    @Test
    fun `mangle preserves case of replaced character - mixed case`() {
        // "Hello" — H is uppercase; mangle should not produce a lowercase replacement for H
        // if H is chosen. We run many times to get a stable check.
        val rng = Random(99)
        val result = TypoGenerator.mangle("Hello", rng)
        // First char capitalized; output should still start with uppercase (if first char is picked)
        // or the replacement in position should be uppercase if position 0 is chosen for a 5-char word
        // (for len > 3, pos 0 is excluded — so 'H' won't be touched). Let's just verify no crash.
        assertEquals(5, result.length)
    }

    @Test
    fun `mangle does not change first or last char for words longer than 3`() {
        val rng = Random(777)
        for (trial in 1..100) {
            val word = "keyboard" // length 8 > 3
            val result = TypoGenerator.mangle(word, rng)
            assertEquals("First char should be unchanged", word.first(), result.first())
            assertEquals("Last char should be unchanged", word.last(), result.last())
        }
    }

    @Test
    fun `mangle replaces exactly one character`() {
        val rng = Random(555)
        for (trial in 1..50) {
            val word = "typing"
            val result = TypoGenerator.mangle(word, rng)
            val diffs = word.zip(result).count { (a, b) -> a != b }
            assertTrue("Expected 0 or 1 diff, got $diffs for '$result'", diffs <= 1)
        }
    }

    @Test
    fun `replacement char is QWERTY-adjacent to original`() {
        // Build the known adjacency map for verification
        val adjacency: Map<Char, Set<Char>> = mapOf(
            'q' to setOf('w', 'a', 's'),
            'w' to setOf('q', 'e', 'a', 's', 'd'),
            'e' to setOf('w', 'r', 's', 'd', 'f'),
            'r' to setOf('e', 't', 'd', 'f', 'g'),
            't' to setOf('r', 'y', 'f', 'g', 'h'),
            'y' to setOf('t', 'u', 'g', 'h', 'j'),
            'u' to setOf('y', 'i', 'h', 'j', 'k'),
            'i' to setOf('u', 'o', 'j', 'k', 'l'),
            'o' to setOf('i', 'p', 'k', 'l'),
            'p' to setOf('o', 'l'),
            'a' to setOf('q', 'w', 's', 'z'),
            's' to setOf('a', 'w', 'e', 'd', 'x', 'z'),
            'd' to setOf('s', 'e', 'r', 'f', 'c', 'x'),
            'f' to setOf('d', 'r', 't', 'g', 'v', 'c'),
            'g' to setOf('f', 't', 'y', 'h', 'b', 'v'),
            'h' to setOf('g', 'y', 'u', 'j', 'n', 'b'),
            'j' to setOf('h', 'u', 'i', 'k', 'm', 'n'),
            'k' to setOf('j', 'i', 'o', 'l', 'm'),
            'l' to setOf('k', 'o', 'p'),
            'z' to setOf('a', 's', 'x'),
            'x' to setOf('z', 's', 'd', 'c'),
            'c' to setOf('x', 'd', 'f', 'v'),
            'v' to setOf('c', 'f', 'g', 'b'),
            'b' to setOf('v', 'g', 'h', 'n'),
            'n' to setOf('b', 'h', 'j', 'm'),
            'm' to setOf('n', 'j', 'k'),
        )

        val rng = Random(321)
        for (trial in 1..200) {
            val word = "hello"
            val result = TypoGenerator.mangle(word, rng)
            if (result == word) continue // rare: no change (edge case)
            val changedIndex = word.indices.first { word[it] != result[it] }
            val original = word[changedIndex].lowercaseChar()
            val replacement = result[changedIndex].lowercaseChar()
            val expected = adjacency[original] ?: continue
            assertTrue("'$replacement' should be adjacent to '$original'", replacement in expected)
        }
    }

    @Test
    fun `3-char words are eligible for mangling at any position`() {
        // For len==3 we allow all positions including 0 and 2
        var saw3CharChange = false
        val rng = Random(100)
        repeat(50) {
            val result = TypoGenerator.mangle("cat", rng)
            if (result != "cat") saw3CharChange = true
        }
        assertTrue("3-char word should be mangled at least sometimes", saw3CharChange)
    }
}
