package org.qianq.qlox

class Fn (
    private val declaration: Function,
    private val closure: Environment,
    private val isInitializer: Boolean
): Callable {
    override fun arity(): Int {
        return declaration.params.size
    }

    fun bind(instance: LoxInstance): Fn {
        val environment = Environment(closure)
        environment.define("this", instance)
        return Fn(declaration, environment, isInitializer)
    }

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)

        for (i in declaration.params.indices) {
            environment.define(declaration.params[i].lexeme, arguments[i])
        }

        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue: ReturnValue) {
            if (isInitializer) {
                return closure.getAt(0, "this")
            }
            return returnValue.value
        }

        if (isInitializer) {
            return closure.getAt(0, "this")
        }

        return null
    }

    override fun toString(): String {
        return "<fn ${declaration.name.lexeme}>"
    }
}