package org.qianq.qlox

import org.qianq.qlox.token.Token
import org.qianq.qlox.token.TokenType
import org.qianq.qlox.token.TokenType.*

class Parser(private val tokens: List<Token>) {
    class ParseError : RuntimeException()

    private var current: Int = 0

    private fun isAtEnd(): Boolean {
        return peek().type === EOF
    }

    private fun peek(): Token {
        return tokens.get(current)
    }

    private fun previous(): Token {
        return tokens.get(current - 1)
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    // Expression parsers

    // Unary Operators

    // primary → NUMBER | STRING | "true" | "false" | "nil"
    //        | "(" expression ")" ;
    private fun primary(): Expr {
        if (match(FALSE)) return Literal(false)
        if (match(TRUE)) return Literal(true)
        if (match(NIL)) return Literal(null)

        if (match(NUMBER, STRING)) {
            return Literal(previous().literal)
        }

        if (match(LEFT_PAREN)) {
            val expr: Expr = expression()
            consume(RIGHT_PAREN, "Expect ')' after expression.")
            return Grouping(expr)
        }
    }

    // unary → ( "!" | "-" ) unary
    //       | primary ;
    private fun unary(): Expr {
        if (match(BANG, MINUS)) {
            val operator: Token = previous()
            val right: Expr = unary()
            return Unary(operator, right)
        }

        return primary()
    }

    // Binary Operators

    // factor → unary ( ( "/" | "*" ) unary )* ;
    private fun factor(): Expr {
        var expr: Expr = unary()

        while (match(SLASH, STAR)) {
            val operator: Token = previous()
            val right: Expr = unary()
            expr = Binary(expr, operator, right)
        }

        return expr
    }

    // term → factor ( ( "-" | "+" ) factor )* ;
    private fun term(): Expr {
        var expr: Expr = factor()

        while (match(MINUS, PLUS)) {
            val operator: Token = previous()
            val right: Expr = factor()
            expr = Binary(expr, operator, right)
        }

        return expr
    }

    // comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    private fun comparison(): Expr {
        var expr: Expr = term()

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            val operator: Token = previous()
            val right: Expr = term()
            expr = Binary(expr, operator, right)
        }

        return expr
    }

    // equality → comparison ( ( "!=" | "==" ) comparison )* ;
    private fun equality(): Expr {
        var expr: Expr = comparison()

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            val operator: Token = previous()
            val right: Expr = comparison()
            expr = Binary(expr, operator, right)
        }

        return expr
    }

    // expression → equality ;
    private fun expression(): Expr {
        return equality()
    }
}
