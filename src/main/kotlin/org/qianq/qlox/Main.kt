package org.qianq.qlox

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Scanner
import kotlin.system.exitProcess

class QLox {
    companion object {
        private fun run(src: String) {
            // Run the instruction
            val scanner = Scanner(src)
            val tokens: List<Token> = scanner.scanTokens()
            for (token in tokens) {
                println(token)
            }
        }

        private fun runPrompt() {
            // Read new instruction from stdin
            while (true) {
                print("> ")
                val line = readlnOrNull() ?: break
                // Execute the instruction
                run(line)
            }
        }

        private fun runFile(path: String) {
            // Read the file and convert it to a string
            val bytes = Files.readAllBytes(Paths.get(path))
            // Execute the instructions
            run(String(bytes, StandardCharsets.UTF_8))
        }

        @JvmStatic
        fun main(args: Array<String>) {
            // Entry point
            when (args.size) {
                // If no arguments were given when running the program, run the REPL
                0 -> runPrompt()
                // If one argument was given (the file path), run the file
                1 -> runFile(args[0])
                // Otherwise, print the usage help message and exit
                else -> {
                    println("Usage: qlox [script]")
                    exitProcess(64)
                }
            }
        }
    }
}
