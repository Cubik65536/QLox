package org.qianq.qlox

import org.qianq.qlox.token.Token
import org.qianq.qlox.token.TokenType

class Scanner (val src: String) {
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
            else -> QLox.error(line, "Unexpected character.")
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