package org.qianq.qlox

import org.qianq.qlox.token.Token
import java.util.*

private enum class FunctionType {
    NONE, FUNCTION
}

class Resolver (val interpreter: Interpreter): Expr.Visitor<Void?>, Stmt.Visitor<Void?> {
    private val scopes = Stack<MutableMap<String, Boolean>>()
    private var currentFunction = FunctionType.NONE

    private fun beginScope() {
        scopes.push(HashMap())
    }

    private fun resolve(stmt: Stmt) {
        stmt.accept(this)
    }

    private fun resolve(expr: Expr) {
        expr.accept(this)
    }

    fun resolve(statements: List<Stmt>) {
        for (statement in statements) {
            resolve(statement)
        }
    }

    private fun endScope() {
        scopes.pop()
    }

    private fun declare(name: Token) {
        if (scopes.isEmpty()) return

        val scope = scopes.peek()
        if (scope.containsKey(name.lexeme)) {
            QLox.error(name, "Variable with this name already declared in this scope.")
        }

        scope[name.lexeme] = false
    }

    private fun define(name: Token) {
        if (scopes.isEmpty()) return
        scopes.peek()[name.lexeme] = true
    }

    private fun resolveLocal(expr: Expr, name: Token) {
        for (i in scopes.size - 1 downTo 0) {
            if (scopes[i].containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size - 1 - i)
                return
            }
        }
    }

    private fun resolveFunction(function: Function, type: FunctionType) {
        val enclosingFunction = currentFunction
        currentFunction = type
        beginScope()
        for (param in function.params) {
            declare(param)
            define(param)
        }
        resolve(function.body)
        endScope()
        currentFunction = enclosingFunction
    }

    override fun visitStmt(stmt: Block): Void? {
        beginScope()
        resolve(stmt.statements)
        endScope()
        return null
    }

    override fun visitStmt(stmt: Class): Void? {
        TODO("Not yet implemented")
    }

    override fun visitStmt(stmt: Var): Void? {
        declare(stmt.name)
        if (stmt.initializer != null) {
            resolve(stmt.initializer)
        }
        define(stmt.name)
        return null
    }

    override fun visitExpr(expr: Variable): Void? {
        if (!scopes.isEmpty() && scopes.peek()[expr.name.lexeme] == false) {
            QLox.error(expr.name, "Cannot read local variable in its own initializer.")
        }
        resolveLocal(expr, expr.name)
        return null
    }

    override fun visitExpr(expr: Assign): Void? {
        resolve(expr.value)
        resolveLocal(expr, expr.name)
        return null
    }

    override fun visitStmt(stmt: Function): Void? {
        declare(stmt.name)
        define(stmt.name)
        resolveFunction(stmt, FunctionType.FUNCTION)
        return null
    }

    override fun visitStmt(stmt: Expression): Void? {
        resolve(stmt.expression)
        return null
    }

    override fun visitStmt(stmt: If): Void? {
        resolve(stmt.condition)
        resolve(stmt.thenBranch)
        if (stmt.elseBranch != null) resolve(stmt.elseBranch)
        return null
    }

    override fun visitStmt(stmt: Print): Void? {
        resolve(stmt.expression)
        return null
    }

    override fun visitStmt(stmt: Return): Void? {
        if (currentFunction == FunctionType.NONE) {
            QLox.error(stmt.keyword, "Cannot return from top-level code.")
        }
        if (stmt.value != null) {
            resolve(stmt.value)
        }
        return null
    }

    override fun visitStmt(stmt: While): Void? {
        resolve(stmt.condition)
        resolve(stmt.body)
        return null
    }

    override fun visitExpr(expr: Binary): Void? {
        resolve(expr.left)
        resolve(expr.right)
        return null
    }

    override fun visitExpr(expr: Call): Void? {
        resolve(expr.callee)
        for (argument in expr.arguments) {
            resolve(argument)
        }
        return null
    }

    override fun visitExpr(expr: Grouping): Void? {
        resolve(expr.expression)
        return null
    }

    override fun visitExpr(expr: Literal): Void? {
        return null
    }

    override fun visitExpr(expr: Logical): Void? {
        resolve(expr.left)
        resolve(expr.right)
        return null
    }

    override fun visitExpr(expr: Unary): Void? {
        resolve(expr.right)
        return null
    }
}
