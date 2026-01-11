package io.tashtabash.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import java.util.Collections


class InputDatabaseTest {
    @TempDir
    lateinit var tempDir: Path

    private fun createMockFile(name: String, content: String): String {
        val file = File(tempDir.toFile(), name)
        file.writeText(content)
        return file.absolutePath
    }

    @Test
    fun `Loads a single file`() {
        val path = createMockFile("single.txt", "-Hello\nWorld")
        val db = InputDatabase(path)

        assertEquals("Hello World", db.readLine())
        assertNull(db.readLine())
    }

    @Test
    fun `Loads multiple files`() {
        val path1 = createMockFile("data1.txt", """
            / A comment
            -Entry One
             Continued line
            
            / Another comment
            -Entry Two
        """.trimIndent())
        val path2 = createMockFile("data2.txt", """
            -Entry Three
             Smth
             Content
        """.trimIndent())

        val db = InputDatabase(listOf(path1, path2))
        val results = db.readLines()

        assertEquals(3, results.size)
        assertEquals("Entry One  Continued line", results[0])
        assertEquals("Entry Two", results[1])
        assertEquals("Entry Three  Smth  Content", results[2])
    }

    @Test
    fun `Loads an empty file`() {
        val path = createMockFile("empty.txt", "/ Comment\n\n   \n/ Comment")
        val db = InputDatabase(path)

        assertTrue(db.readLines().isEmpty())
    }

    @Test
    fun `Loads via Enumerations`() {
        val path = createMockFile("enum.txt", "-EnumTest")
        val url = File(path).toURI().toURL()
        val enumeration = Collections.enumeration(listOf(url))

        val db = InputDatabase(enumeration)
        assertEquals("EnumTest", db.readLine())
    }
}
