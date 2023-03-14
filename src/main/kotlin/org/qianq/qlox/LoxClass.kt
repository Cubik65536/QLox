package org.qianq.qlox

class LoxClass(val name: String, val methods: Map<String, Fn>, override val arity: Int): Callable {
    fun findMethod(name: String): Fn? {
        if (methods.containsKey(name)) {
            return methods[name] as Fn
        }
        return null
    }

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        return LoxInstance(this)
    }

    override fun toString(): String {
        return name
    }
}
