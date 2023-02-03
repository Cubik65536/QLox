package org.qianq.qlox

import org.qianq.qlox.token.Token

class RuntimeError(token: Token, message: String) : RuntimeException(message)
