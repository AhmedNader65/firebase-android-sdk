package com.google.firebase.inappmessaging.logger

import java.util.logging.Logger

class FirebaseLogger {
    private val logger by lazy {
        Logger.getLogger("FirebaseLogger")
    }

    fun logMessage(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            logger.severe("$tag: $message\n${throwable.message}")
        } else {
            logger.info("$tag: $message")
        }
    }
}