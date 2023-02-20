package org.qianq.qlox

class AstPrinter: Expr.Visitor<String> {
    private fun parenthesize(name: String, vararg exprs: Expr): String {
        val stringBuilder = StringBuilder()

        stringBuilder.append("(").append(name)
        for (expr in exprs) {
            stringBuilder.append(" ")
            stringBuilder.append(expr.accept(this))
        }
        stringBuilder.append(")")

        return stringBuilder.toString()
    }

    fun print(expr: Expr): String {
        return expr.accept(this)
    }

    override fun visitExpr(expr: Assign): String {
        TODO("Not yet implemented")
    }

    override fun visitExpr(expr: Binary): String {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right)
    }

    override fun visitExpr(expr: Call): String {
        TODO("Not yet implemented")
    }

    override fun visitExpr(expr: Grouping): String {
        return parenthesize("group", expr.expression)
    }

    override fun visitExpr(expr: Literal): String {
        if (expr.value == null) return "nil"
        return expr.value.toString()
    }

    override fun visitExpr(expr: Logical): String {
        TODO("Not yet implemented")
    }

    override fun visitExpr(expr: Unary): String {
        return parenthesize(expr.operator.lexeme, expr.right)
    }

    override fun visitExpr(expr: Variable): String {
        TODO("Not yet implemented")
    }
}
