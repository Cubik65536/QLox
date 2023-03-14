package org.qianq.qlox

class LoxClass(val name: String, override val arity: Int): Callable {
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        return LoxInstance(this)
    }

    override fun toString(): String {
        return name
    }
}
