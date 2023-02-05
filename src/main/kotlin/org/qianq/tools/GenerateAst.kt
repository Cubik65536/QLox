package org.qianq.tools

import java.io.PrintWriter
import kotlin.system.exitProcess

class GenerateAst {
    companion object {
        private fun defineVisitor(writer: PrintWriter, baseName: String, types: List<String>) {
            writer.println("    interface Visitor<R> {")
            for (type in types) {
                val typeName = type.split("->")[0].trim()
                writer.println("        fun visit$baseName(${baseName.lowercase()}: $typeName): R")
            }
            writer.println("    }")
        }

        private fun defineType(writer: PrintWriter, baseName: String, className: String, fields: String) {
            writer.println("class $className($fields) : $baseName() {")

            // Visitor pattern.
            writer.println("    override fun <R> accept(visitor: Visitor<R>): R {")
            writer.println("        return visitor.visit$baseName(this)")
            writer.println("    }")

            writer.println("}")
            writer.println()
        }

        private fun defineAst(outputDir: String, baseName: String, types: List<String>) {
            val path = "$outputDir/$baseName.kt"
            val writer = PrintWriter(path, "UTF-8")

            writer.println("package org.qianq.qlox")
            writer.println()
            writer.println("import org.qianq.qlox.token.Token")
            writer.println()
            writer.println("abstract class $baseName {")

            // The base accept() method.
            writer.println("    abstract fun <R> accept(visitor: Visitor<R>): R")

            defineVisitor(writer, baseName, types)
            writer.println("}")
            writer.println()

            for (type in types) {
                val subclass = type.split("->")
                val className = subclass[0].trim()
                val fields = subclass[1].trim()
                defineType(writer, baseName, className, fields)
            }

            writer.close()
        }

        // args when running from JetBrains IntelliJ IDE: "$FileDir$/../qlox"
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size != 1) {
                System.err.println("Usage: generate_ast <output directory>")
                exitProcess(64)
            }
            val outputDir = args[0]
            defineAst(
                outputDir, "Expr", listOf(
                    "Assign   -> val name: Token, val value: Expr",
                    "Binary   -> val left: Expr, val operator: Token, val right: Expr",
                    "Grouping -> val expression: Expr",
                    "Literal  -> val value: Any?",
                    "Unary    -> val operator: Token, val right: Expr",
                    "Variable -> val name: Token"
                )
            )
            defineAst(
                outputDir, "Stmt", listOf(
                    "Expression -> val expression: Expr",
                    "Print      -> val expression: Expr",
                    "Var        -> val name: Token, val initializer: Expr?"
                )
            )
        }
    }
}
