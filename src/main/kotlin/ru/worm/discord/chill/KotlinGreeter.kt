package ru.worm.discord.chill

import org.slf4j.LoggerFactory

class KotlinGreeter {
    val log = LoggerFactory.getLogger(KotlinGreeter::class.java)

    fun greet() {
        log.info("kotlin is up and running.")
    }
}