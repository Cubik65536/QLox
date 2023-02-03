package org.qianq.qlox

import org.qianq.qlox.token.Token
import org.qianq.qlox.token.TokenType.*

class Interpreter: Expr.Visitor<Any> {
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
        return expr.value
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
}
