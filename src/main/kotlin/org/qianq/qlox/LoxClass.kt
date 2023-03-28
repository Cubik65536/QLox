package org.qianq.qlox

class LoxClass(val name: String, val methods: Map<String, Fn>): Callable {
    override fun arity(): Int {
        val initializer = findMethod("init") ?: return 0
        return initializer.arity()
    }

    fun findMethod(name: String): Fn? {
        if (methods.containsKey(name)) {
            return methods[name] as Fn
        }
        return null
    }

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val instance = LoxInstance(this)

        val initializer = findMethod("init")
        initializer?.bind(instance)?.call(interpreter, arguments)

        return instance
    }

    override fun toString(): String {
        return name
    }
}
