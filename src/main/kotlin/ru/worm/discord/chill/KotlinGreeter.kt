package ru.worm.discord.chill

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component

@Component
class KotlinGreeter : InitializingBean {
    val log = LoggerFactory.getLogger(KotlinGreeter::class.java)

    fun greet() {
        log.info("kotlin is up and running.")
    }

    override fun afterPropertiesSet() {
        greet()
    }
}