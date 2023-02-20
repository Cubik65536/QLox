package org.qianq.qlox

import org.qianq.qlox.token.Token
import org.qianq.qlox.token.TokenType
import org.qianq.qlox.token.TokenType.*

class Parser(private val tokens: List<Token>) {
    class ParseError : RuntimeException()

    private var current: Int = 0

    private fun error(token: Token, message: String): ParseError {
        QLox.error(token, message)
        return ParseError()
    }

    // Synchronizing a recursive descent parser
    private fun synchronize() {
        advance()

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return

            when (peek().type) {
                CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> return
                else -> {}
            }

            advance()
        }
    }


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

    // Entering Panic Mode
    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw error(peek(), message)
    }

    // Expression parsers

    // Unary Operators

    // primary → NUMBER | STRING | "true" | "false" | "nil"
    //        | "(" expression ")"
    //        | IDENTIFIER ;
    private fun primary(): Expr {
        if (match(FALSE)) return Literal(false)
        if (match(TRUE)) return Literal(true)
        if (match(NIL)) return Literal(null)

        if (match(NUMBER, STRING)) {
            return Literal(previous().literal)
        }

        if (match(IDENTIFIER)) {
            return Variable(previous())
        }

        if (match(LEFT_PAREN)) {
            val expr: Expr = expression()
            consume(RIGHT_PAREN, "Expect ')' after expression.")
            return Grouping(expr)
        }

        throw error(peek(), "Expect expression.")
    }

    // unary → ( "!" | "-" ) unary | call ;
    private fun unary(): Expr {
        if (match(BANG, MINUS)) {
            val operator: Token = previous()
            val right: Expr = unary()
            return Unary(operator, right)
        }

        return call()
    }

    // call → primary ( "(" arguments? ")" )* ;
    private fun call(): Expr {
        var expr: Expr = primary()

        while (true) {
            if (match(LEFT_PAREN)) {
                expr = arguments(expr)
            } else {
                break
            }
        }

        return expr
    }

    // arguments → expression ( "," expression )* ;
    private fun arguments(callee: Expr): Expr {
        val arguments: MutableList<Expr> = mutableListOf()

        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size >= 255) {
                    error(peek(), "Can't have more than 255 arguments.")
                }
                arguments.add(expression())
            } while (match(COMMA))
        }

        val paren = consume(RIGHT_PAREN, "Expect ')' after arguments.")

        return Call(callee, paren, arguments)
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

    // expression → assignment ;
    private fun expression(): Expr {
        return assignment()
    }

    // Statement parsers

    // declaration → funDecl
    //             | varDecl
    //             | statement ;
    private fun declaration(): Stmt {
        return try {
            if (match(FUN)) {
                function("function");
            } else if (match(VAR)) {
                varDeclaration()
            } else {
                statement()
            }
        } catch (error: ParseError) {
            synchronize()
            return null!!
        }
    }

    // statement      → exprStmt
    //                | forStmt
    //                | ifStmt
    //                | printStmt
    //                | returnStmt
    //                | whileStmt
    //                | block ;
    private fun statement(): Stmt {
        if (match(FOR)) return forStatement()
        if (match(IF)) return ifStatement()
        if (match(PRINT)) return printStatement()
        if (match(RETURN)) return returnStatement()
        if (match(WHILE)) return whileStatement()
        if (match(LEFT_BRACE)) return Block(blockStatement())
        return expressionStatement()
    }

    // forStmt        → "for" "(" ( varDecl | exprStmt | ";" )
    //                expression? ";"
    //                expression? ")" statement ;
    private fun forStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'for'.")

        val initializer: Stmt? = if (match(SEMICOLON)) {
            null
        } else if (match(VAR)) {
            varDeclaration()
        } else {
            expressionStatement()
        }

        var condition: Expr? = if (!check(SEMICOLON)) {
            expression()
        } else {
            null
        }
        consume(SEMICOLON, "Expect ';' after loop condition.")

        val increment: Expr? = if (!check(RIGHT_PAREN)) {
            expression()
        } else {
            null
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.")

        var body: Stmt = statement()

        if (increment != null) {
            body = Block(listOf(body, Expression(increment)))
        }

        if (condition == null) condition = Literal(true)
        body = While(condition, body)

        if (initializer != null) {
            body = Block(listOf(initializer, body))
        }

        return body
    }

    // ifStmt         → "if" "(" expression ")" statement
    //                ( "else" statement )? ;
    private fun ifStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'if'.")
        val condition: Expr = expression()
        consume(RIGHT_PAREN, "Expect ')' after if condition.")

        val thenBranch: Stmt = statement()
        var elseBranch: Stmt? = null
        if (match(ELSE)) {
            elseBranch = statement()
        }

        return If(condition, thenBranch, elseBranch)
    }

    // printStmt      → "print" expression ";" ;
    private fun printStatement(): Stmt {
        val value: Expr = expression()
        consume(SEMICOLON, "Expect ';' after value.")
        return Print(value)
    }

    // returnStmt     → "return" expression? ";" ;
    private fun returnStatement(): Stmt {
        val keyword: Token = previous()
        var value: Expr? = null
        if (!check(SEMICOLON)) {
            value = expression()
        }
        consume(SEMICOLON, "Expect ';' after return value.")
        return Return(keyword, value)
    }

    // whileStmt      → "while" "(" expression ")" statement ;
    private fun whileStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'while'.")
        val condition: Expr = expression()
        consume(RIGHT_PAREN, "Expect ')' after condition.")
        val body: Stmt = statement()

        return While(condition, body)
    }

    // varDecl        → "var" IDENTIFIER ( "=" expression )? ";" ;
    private fun varDeclaration(): Stmt {
        val name: Token = consume(IDENTIFIER, "Expect variable name.")

        var initializer: Expr? = null
        if (match(EQUAL)) {
            initializer = expression()
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.")
        return Var(name, initializer)
    }

    // exprStmt       → expression ";" ;
    private fun expressionStatement(): Stmt {
        val expr: Expr = expression()
        consume(SEMICOLON, "Expect ';' after expression.")
        return Expression(expr)
    }

    // funDecl        → "fun" function ;
    // function       → IDENTIFIER "(" parameters? ")" block ;
    private fun function(kind: String): Function {
        val name = consume(IDENTIFIER, "Expect $kind name.")

        consume(LEFT_PAREN, "Expect '(' after $kind name.")

        val parameters: MutableList<Token> = mutableListOf()

        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size >= 255) {
                    error(peek(), "Cannot have more than 255 parameters.")
                }
                parameters.add(consume(IDENTIFIER, "Expect parameter name."))
            } while (match(COMMA))
        }

        consume(RIGHT_PAREN, "Expect ')' after parameters.")

        consume(LEFT_BRACE, "Expect '{' before $kind body.")
        val body = blockStatement()
        return Function(name, parameters, body)
    }

    // block          → "{" declaration* "}" ;
    private fun blockStatement(): List<Stmt> {
        val statements: MutableList<Stmt> = mutableListOf()
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration())
        }
        consume(RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    // assignment     → IDENTIFIER "=" assignment
    //                | logic_or ;
    private fun assignment(): Expr {
        val expr: Expr = or()

        if (match(EQUAL)) {
            val equals: Token = previous()
            val value: Expr = assignment()

            if (expr is Variable) {
                val name: Token = expr.name
                return Assign(name, value)
            }

            error(equals, "Invalid assignment target.")
        }

        return expr
    }

    // logic_or       → logic_and ( "or" logic_and )* ;
    private fun or(): Expr {
        var expr: Expr = and()

        while (match(OR)) {
            val operator: Token = previous()
            val right: Expr = and()
            expr = Logical(expr, operator, right)
        }

        return expr
    }

    // logic_and      → equality ( "and" equality )* ;
    private fun and(): Expr {
        var expr: Expr = equality()

        while (match(AND)) {
            val operator: Token = previous()
            val right: Expr = equality()
            expr = Logical(expr, operator, right)
        }

        return expr
    }

    fun parse(): List<Stmt> {
        val statements: MutableList<Stmt> = mutableListOf()
        while (!isAtEnd()) {
            statements.add(declaration())
        }
        return statements
    }
}
