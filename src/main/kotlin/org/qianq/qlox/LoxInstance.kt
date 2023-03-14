package org.qianq.qlox

class LoxInstance(val clazz: LoxClass) {
    override fun toString(): String {
        return clazz.name + " instance"
    }
}
