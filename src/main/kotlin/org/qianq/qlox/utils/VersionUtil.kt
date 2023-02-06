package org.qianq.qlox.utils

import java.io.InputStream
import java.util.Properties

class VersionUtil {
    companion object {
        private val properties = Properties()

        fun loadVersionProperties() {
            val inputStream: InputStream? = this::class.java.getResourceAsStream("/version.properties")
            properties.load(inputStream)
        }

        fun getProperty(key: String): String {
            val property = properties.getProperty(key)
            return property
        }
    }
}
