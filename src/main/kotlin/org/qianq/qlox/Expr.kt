package org.qianq.qlox

import org.qianq.qlox.token.Token

abstract class Expr {
    class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr()
    class Grouping(val expression: Expr) : Expr()
    class Literal(val value: Any?) : Expr()
    class Unary(val operator: Token, val right: Expr) : Expr()
}
