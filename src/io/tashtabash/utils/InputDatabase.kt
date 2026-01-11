package io.tashtabash.utils

import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.lang.AutoCloseable
import java.net.URI
import java.net.URL
import java.util.*


class InputDatabase(private val paths: List<String>) : AutoCloseable {
    private var readerIndex = 0
    private lateinit var bufferedReader: BufferedReader
    private var lastLine: String? = null

    init {
        if (paths.isNotEmpty()) {
            bufferedReader = createReader(paths[readerIndex])

            lastLine = bufferedReader.readLine()
            while ((lastLine != null || nextReader()) && doSkipLine(lastLine!!))
                lastLine = bufferedReader.readLine()
        }
    }

    constructor(path: String) : this(listOf(path))
    constructor(urls: Enumeration<URL>) : this(urls.toList().map { it.toString() })

    private fun createReader(path: String): BufferedReader {
        val url = if (path.contains(":/"))
            URI.create(path).toURL()
        else
            File(path).toURI().toURL()

        val connection = url.openConnection()
        connection.useCaches = false // Otherwise the connections stays open after the file is read

        return connection.getInputStream().bufferedReader()
    }

    private fun doSkipLine(string: String) = string.isBlank() || string[0] == '/'

    private fun nextReader(): Boolean {
        bufferedReader.close()
        readerIndex++

        if (readerIndex >= paths.size)
            return false

        bufferedReader = createReader(paths[readerIndex])

        return true
    }

    fun readLine(): String? {
        lastLine?.let { wholeLine ->
            val line = StringBuilder(wholeLine.drop(1))

            while (true) {
                val newLine = try {
                    bufferedReader.readLine()
                } catch (e: IOException) {
                    System.err.println(e.toString())
                    return null
                }

                if (newLine == null)
                    if (nextReader())
                        continue
                    else {
                        lastLine = null
                        return line.toString()
                    }
                if (doSkipLine(newLine))
                    continue
                if (newLine[0] == '-') {
                    lastLine = newLine
                    return line.toString()
                }
                line.append(" ").append(newLine)
            }
        }

        return null
    }

    fun readLines(): List<String> {
        val lines: MutableList<String> = ArrayList()
        var line = readLine()
        while (line != null) {
            lines.add(line)
            line = readLine()
        }
        return lines
    }

    override fun close() {
        bufferedReader.close()
    }
}
