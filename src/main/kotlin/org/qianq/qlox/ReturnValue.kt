package org.qianq.qlox

/**
 * (Note: this is blatantly abusing the Exception system -@valbaca)
 */

class ReturnValue(val value: Any?) : RuntimeException(null, null, false, false)
