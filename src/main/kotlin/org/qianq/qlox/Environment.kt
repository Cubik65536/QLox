package org.qianq.qlox

import org.qianq.qlox.token.Token

class Environment {
    val enclosing: Environment?

    private val values: MutableMap<String, Any?> = mutableMapOf()

    constructor() {
        enclosing = null
    }

    constructor(enclosing: Environment) {
        this.enclosing = enclosing
    }

    val ancestor: (Int) -> Environment = { distance ->
        var environment: Environment = this
        for (i in 0 until distance) {
            environment = environment.enclosing!!
        }
        environment
    }

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun getAt(distance: Int, name: String): Any? {
        return ancestor(distance).values[name]
    }

    fun assignAt(distance: Int, name: Token, value: Any?) {
        ancestor(distance).values[name.lexeme] = value
    }

    fun get(name: Token): Any? {
        if (values.containsKey(name.lexeme)) {
            return values[name.lexeme]
        }

        if (enclosing != null) return enclosing.get(name)

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun assign(name: Token, value: Any?) {
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
            return
        }

        if (enclosing != null) {
            enclosing.assign(name, value)
            return
        }

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }
}