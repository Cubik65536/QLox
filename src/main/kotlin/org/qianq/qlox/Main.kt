package org.qianq.qlox

import org.qianq.qlox.token.Token
import org.qianq.qlox.token.TokenType
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

class QLox {
    companion object {
        // If a known error have been found
        private var hadError: Boolean = false

        fun error(line: Int, message: String) {
            report(line = line, message = message)
        }

        private fun report(line: Int, where: String = "", message: String) {
            // Print error message
            System.err.println("[line $line]\tERROR $where: $message")
            // Set hadError to true
            hadError = true
        }

        fun error(token: Token, message: String?) {
            if (token.type === TokenType.EOF) {
                report(token.line, " at end", message!!)
            } else {
                report(token.line, " at '" + token.lexeme + "'", message!!)
            }
        }

        fun log(line: Int, message: String, printNewLine: Boolean = false) {
            logger(line = line, message = message, printNewLine = printNewLine)
        }

        private fun logger(line: Int, where: String = "", message: String, printNewLine: Boolean) {
            if (!printNewLine) {
                // Don't add new lines when printing logger message
                println("[line $line]\tLOG $where: ${message.replace("\n", "\\n", true)}")
            } else {
                // Print log message
                println("[line $line]\tLOG $where: $message")
            }
        }

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
                // Don't kill the entire session if user made a mistake
                hadError = false
            }
        }

        private fun runFile(path: String) {
            // Read the file and convert it to a string
            val bytes = Files.readAllBytes(Paths.get(path))
            // Execute the instructions
            run(String(bytes, StandardCharsets.UTF_8))
            // Found and error and exit
            if (hadError) exitProcess(65)
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
