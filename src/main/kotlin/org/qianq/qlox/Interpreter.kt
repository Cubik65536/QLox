package org.qianq.qlox

import org.qianq.qlox.token.TokenType.*

class Interpreter: Expr.Visitor<Any> {
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
                    null
                }
            }
            MINUS -> (left as Double) - (right as Double)
            STAR -> (left as Double) * (right as Double)
            SLASH -> (left as Double) / (right as Double)
            GREATER -> (left as Double) > (right as Double)
            GREATER_EQUAL -> (left as Double) >= (right as Double)
            LESS -> (left as Double) < (right as Double)
            LESS_EQUAL -> (left as Double) <= (right as Double)
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
            MINUS -> -(right as Double)
            // Unreachable.
            else -> null
        }!!

    }
}
