package ru.spbau.mit

import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestSource {
    @Test
    fun testEmptyBinarySegmentTree() {
        val tree = BinarySegmentTree(5)

        assertEquals(false, tree.get(0), "Method `get` fails")
        assertEquals(0, tree.getKth(0), "Method `getKth` fails")
        assertEquals(5, tree.getKth(1), "Method `getKth` fails")
    }

    @Test
    fun testBinarySegmentTree() {
        val tree = BinarySegmentTree(5)

        val ones = (0 until 5 step 2)
        ones.forEach { tree.set(it, true) }
        ones.forEach {
            assertEquals(true, tree.get(it), "Method `get` or `set` fails")
        }

        val zeros = (1 until 5 step 2)
        zeros.forEach {
            assertEquals(false, tree.get(it), "Method `get` or `set` fails")
        }

        assertEquals(0, tree.getKth(0), "Method `getKth` fails")
        assertEquals(4, tree.getKth(3), "Method `getKth` fails")
        assertEquals(5, tree.getKth(4), "Method `getKth` fails")
    }

    @Test
    fun testStringManipulationExample1() {
        assertTrue(testExample("""
            |2
            |bac
            |3
            |2 a
            |1 b
            |2 c
            """.trimMargin(), "acb"), "Wrong answer on the first example")
    }

    @Test
    fun testStringManipulationExample2() {
        assertTrue(testExample("""
            |1
            |abacaba
            |4
            |1 a
            |1 a
            |1 c
            |2 b
            """.trimMargin(), "baa"), "Wrong answer on the second example")
    }

    private fun testExample(input: String, answer: String): Boolean {
        return StringManipulation.read(Scanner(input.reader())).solve() == answer
    }
}
