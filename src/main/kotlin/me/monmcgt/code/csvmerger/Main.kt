package me.monmcgt.code.csvmerger

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val path = if (args.isNotEmpty()) {
            args[0]
        } else {
            println("Enter path to directory containing CSV files")
            readLine()!!
        }
        val lines = if (args.size <= 1) {
            readCsv(path)
        } else {
            readCsv(path, args[1])
        }
        val head = lines.first()
        val body = lines.drop(1)

        println("Please enter format string using the following indexes:")
        println("Example: Hello, {0}! Your age is {1}")
        println(head.withIndex().joinToString("\n") { "${it.index}: ${it.value}" })
        val format = readLine()!!

        println("Please select rows to merge:")
        println(body.withIndex().joinToString("\n") { "${it.index}: ${it.value}" })
        // delimiter is either a space or a comma
        val rows = readLine()!!.split(Regex("[ ,]")).map { it.toInt() }

        val regex = Regex("[{]\\d+[}]")
        val list = mutableListOf<String>()
        for (row in rows) {
            val line = body[row]
            val matches = regex.findAll(format)
            var str = format
            for (match in matches) {
                val index = match.value.substring(1, match.value.length - 1).toInt()
                str = str.replace(match.value, line[index])
            }
            list.add(str)
        }

        val string = list.joinToString("\n")

        println("Please enter path to output file")
        val out = readLine()!!
        val outFile = File(out)
        outFile.parentFile.mkdirs()
        Files.write(outFile.toPath(), string.toByteArray())
    }

    fun readCsv(path: String, delimiter: String = ","): List<List<String>> {
        val reader = Files.newBufferedReader(Paths.get(path))
        val lines = reader.readLines()
        // split but check for quotes
        return lines.map { line ->
            val split = line.split(delimiter)
            val result = mutableListOf<String>()
            var current = ""
            split.forEach { part ->
                if (part.startsWith("\"") && !part.endsWith("\"")) {
                    current = part
                } else if (part.endsWith("\"")) {
                    current += delimiter + part
                    result.add(current)
                    current = ""
                } else if (current.isNotEmpty()) {
                    current += delimiter + part
                } else {
                    result.add(part)
                }
            }
            result
        }
    }

    fun mergeToString(body: List<List<String>>, columns: List<Int>, rows: List<Int>, format: String): String {
        val merged = mutableListOf<String>()
        rows.forEach { row ->
            val rowValues = body[row]
            val mergedRow = columns.joinToString(",") { rowValues[it] }
            merged.add(mergedRow)
        }
        return merged.joinToString("\n") { format.format(it) }
    }
}