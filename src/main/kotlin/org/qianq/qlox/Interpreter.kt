package org.qianq.qlox

import org.qianq.qlox.token.Token
import org.qianq.qlox.token.TokenType.*

class Interpreter: Expr.Visitor<Any>, Stmt.Visitor<Unit> {
    private var globals = Environment()

    private fun stringify(obj: Any?): String {
        return if (obj == null) {
            "nil"
        } else if (obj is Double) {
            val text = obj.toString()
            if (text.endsWith(".0")) {
                text.substring(0, text.length - 2)
            } else {
                text
            }
        } else {
            obj.toString()
        }
    }

    private fun checkNumberOperand(operator: Token, operand: Any?) {
        if (operand is Double) return
        throw RuntimeError(operator, "Operand must be a number.")
    }

    private fun checkNumberOperand(operator: Token, left: Any?, right: Any?) {
        if (left is Double && right is Double) return
        throw RuntimeError(operator, "Operands must be numbers.")
    }

    private fun evaluate(expr: Expr): Any {
        return expr.accept(this)
    }

    private fun execute(stmt: Stmt) {
        stmt.accept(this)
    }

    private fun executeBlock(statements: List<Stmt>, environment: Environment) {
        val previous = this.globals
        try {
            this.globals = environment
            for (statement in statements) {
                execute(statement)
            }
        } finally {
            this.globals = previous
        }
    }

    // `false` and `nil` are `false`, everything else is `true`
    private fun isTruthy(obj: Any?): Boolean {
        if (obj == null) return false
        if (obj is Boolean) return obj
        return true
    }

    private fun isEqual(a: Any?, b: Any?): Boolean {
        if (a == null && b == null) return true
        if (a == null) return false
        return a == b
    }

    override fun visitExpr(expr: Assign): Any {
        val value = evaluate(expr.value)
        globals.assign(expr.name, value)
        return value
    }

    override fun visitExpr(expr: Binary): Any {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            PLUS -> {
                if (left is Double && right is Double) {
                    left + right
                } else if (left is String && right is String) {
                    left + right
                } else {
                    throw RuntimeError(expr.operator, "Operands must be two numbers or two strings.")
                }
            }
            MINUS -> {
                checkNumberOperand(expr.operator, left, right)
                (left as Double) - (right as Double)
            }
            STAR -> {
                checkNumberOperand(expr.operator, left, right)
                (left as Double) * (right as Double)
            }
            SLASH -> {
                checkNumberOperand(expr.operator, left, right)
                (left as Double) / (right as Double)
            }
            GREATER -> {
                checkNumberOperand(expr.operator, left, right)
                (left as Double) > (right as Double)
            }
            GREATER_EQUAL -> {
                checkNumberOperand(expr.operator, left, right)
                (left as Double) >= (right as Double)
            }
            LESS -> {
                checkNumberOperand(expr.operator, left, right)
                (left as Double) < (right as Double)
            }
            LESS_EQUAL -> {
                checkNumberOperand(expr.operator, left, right)
                (left as Double) <= (right as Double)
            }
            BANG_EQUAL -> !isEqual(left, right)
            EQUAL_EQUAL -> isEqual(left, right)
            // Unreachable.
            else -> null
        }!!

    }

    // Evaluating parentheses
    override fun visitExpr(expr: Grouping): Any {
        return evaluate(expr.expression)
    }

    // Evaluating literals
    override fun visitExpr(expr: Literal): Any {
        return expr.value!!
    }

    // Evaluating unary expressions
    override fun visitExpr(expr: Unary): Any {
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            BANG -> !isTruthy(right)
            MINUS -> {
                checkNumberOperand(expr.operator, right)
                -(right as Double)
            }
            // Unreachable.
            else -> null
        }!!

    }

    override fun visitExpr(expr: Variable): Any {
        return globals.get(expr.name)!!
    }

    override fun visitStmt(stmt: Block) {
        executeBlock(stmt.statements, Environment(globals))
    }

    // Evaluating statements
    override fun visitStmt(stmt: Expression) {
        evaluate(stmt.expression)
    }

    override fun visitStmt(stmt: If) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }
    }

    override fun visitStmt(stmt: Print) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
    }

    override fun visitStmt(stmt: Var) {
        val value = if (stmt.initializer != null) {
            evaluate(stmt.initializer)
        } else {
            null
        }
        globals.define(stmt.name.lexeme, value)
    }

    fun interpret(statements: List<Stmt>) {
        try {
            for (statement in statements) {
                execute(statement)
            }
        } catch (error: RuntimeError) {
            QLox.runtimeError(error)
        }
    }
}
