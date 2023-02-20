package org.qianq.qlox

interface LoxCallable {
    fun call(interpreter: Interpreter, arguments: List<Any?>): Any?
}
