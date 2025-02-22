package io.github.t45k.jooq_rollback

import io.r2dbc.spi.ConnectionFactory
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.reactor.extensions.CoreSubscriberProvider
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@SpringBootApplication
class JooqRollbackApplication

fun main(args: Array<String>) {
    runApplication<JooqRollbackApplication>(*args)
}

@Configuration
class JooqConfiguration {
    @Bean
    fun dslContext(connectionFactory: ConnectionFactory): DSLContext =
        DSL.using(connectionFactory)
            .apply { configuration().set(CoreSubscriberProvider()) }
            .dsl()
}
