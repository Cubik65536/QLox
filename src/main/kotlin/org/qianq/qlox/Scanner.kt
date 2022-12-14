package org.qianq.qlox

import org.qianq.qlox.token.Token
import org.qianq.qlox.token.TokenType

class Scanner (val src: String) {
    private val keywords: Map<String, TokenType> = mapOf(
        "and" to TokenType.AND,
        "class" to TokenType.CLASS,
        "else" to TokenType.ELSE,
        "false" to TokenType.FALSE,
        "for" to TokenType.FOR,
        "fun" to TokenType.FUN,
        "if" to TokenType.IF,
        "nil" to TokenType.NIL,
        "or" to TokenType.OR,
        "print" to TokenType.PRINT,
        "return" to TokenType.RETURN,
        "super" to TokenType.SUPER,
        "this" to TokenType.THIS,
        "true" to TokenType.TRUE,
        "var" to TokenType.VAR,
        "while" to TokenType.WHILE
    )
    val tokens = mutableListOf<Token>()

    private var start: Int = 0
    private var current: Int = 0
    private var line: Int = 1

    private fun addToken(type: TokenType, literal: Any? = null) {
        val text = src.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    private fun nextChar(): Char {
        return src[current++]
    }

    private fun isAtEnd(): Boolean {
        return current >= src.length
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (src[current] != expected) return false
        current++
        return true
    }

    private fun peek(): Char {
        if (isAtEnd()) return '\u0000'
        return src[current]
    }

    private fun peekNext(): Char {
        if (current + 1 >= src.length) return '\u0000'
        return src[current + 1]
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            nextChar()
        }

        if (isAtEnd()) {
            QLox.error(line, "Unterminated string.")
            return
        }

        // The " at the end of the string
        nextChar()

        // Trim the " at the beginning and end of the string
        val value = src.substring(start + 1, current - 1)
        addToken(TokenType.STRING, value)
    }

    private fun isDigit(c: Char): Boolean {
        return c in '0'..'9'
    }

    private fun number() {
        while (isDigit(peek())) nextChar()
        if (peek() == '.' && isDigit(peekNext())) {
            // Looking for a fractional part
            // Consume the "."
            nextChar()
            while (isDigit(peek())) nextChar()
        }
        addToken(TokenType.NUMBER, src.substring(start, current).toDouble())
    }

    private fun isAlpha(c: Char): Boolean {
        return c in 'a'..'z' || c in 'A'..'Z' || c == '_'
    }

    private fun isNumericAlpha(c: Char): Boolean {
        return isAlpha(c) || isDigit(c)
    }

    private fun identifier() {
        while (isNumericAlpha(peek())) nextChar()
        val text = src.substring(start, current)
        val type = keywords[text] ?: TokenType.IDENTIFIER
        addToken(type)
    }

    private fun scanToken() {
        val c = nextChar()
        when (c) {
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '-' -> addToken(TokenType.MINUS)
            '+' -> addToken(TokenType.PLUS)
            ';' -> addToken(TokenType.SEMICOLON)
            '*' -> addToken(TokenType.STAR)
            '!' -> addToken(if (match('=')) TokenType.BANG_EQUAL else TokenType.BANG)
            '=' -> addToken(if (match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL)
            '<' -> addToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            '>' -> addToken(if (match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)
            '/' -> {
                if (match('/')) {
                    // A comment goes until the end of the line
                    while (peek() != '\n' && !isAtEnd()) nextChar()
                } else {
                    // A division operator
                    addToken(TokenType.SLASH)
                }
            }
            ' ', '\r', '\t' -> {} // Ignore whitespace
            '\n' -> line++ // New line
            '"' -> string() // Read a string
            else -> {
                if (isDigit(c)) {
                    // Read a number
                    number()
                } else if (isAlpha(c)) {
                    // Read a reserved word or identifier
                    identifier()
                } else {
                    // Unknown character
                    QLox.error(line, "Unexpected character.")
                }
            }
        }
    }

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }
}
