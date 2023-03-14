package org.qianq.qlox

import org.qianq.qlox.token.Token

class LoxInstance(val clazz: LoxClass) {
    private val fields = HashMap<String, Any?>()

    fun get(name: Token): Any? {
        if (fields.containsKey(name.lexeme)) {
            return fields[name.lexeme]
        }

        val method = clazz.findMethod(name.lexeme)
        if (method != null) return method

        throw RuntimeError(name, "Undefined property '${name.lexeme}'.")
    }

    fun set(name: Token, value: Any?) {
        fields[name.lexeme] = value
    }

    override fun toString(): String {
        return clazz.name + " instance"
    }
}
