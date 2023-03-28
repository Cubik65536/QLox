package org.qianq.qlox

class Clock: Callable {
    override fun arity(): Int {
        return 0
    }

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any {
        return System.currentTimeMillis().toDouble() / 1000.0
    }

    override fun toString(): String {
        return "<native fn>"
    }
}
