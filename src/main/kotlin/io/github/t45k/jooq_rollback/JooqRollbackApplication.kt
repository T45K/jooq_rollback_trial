package io.github.t45k.jooq_rollback

import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.reactive.awaitSingle
import org.jooq.DSLContext
import org.jooq.impl.DefaultConfiguration
import org.jooq.reactor.extensions.CoreSubscriberProvider
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.r2dbc.connection.TransactionAwareConnectionFactoryProxy
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.await
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@SpringBootApplication
class JooqRollbackApplication

fun main(args: Array<String>) {
    runApplication<JooqRollbackApplication>(*args)
}

@Configuration
class JooqConfiguration {
    @Bean
    fun dslContext(connectionFactory: ConnectionFactory): DSLContext {
        val transactionAwareConnectionFactoryProxy = TransactionAwareConnectionFactoryProxy(connectionFactory)
        return DefaultConfiguration()
            .set(transactionAwareConnectionFactoryProxy)
            .set(CoreSubscriberProvider())
            .dsl()
    }
}

@Service
class RollbackService(
    private val databaseClient: DatabaseClient,
    private val dslContext: DSLContext,
) {
    @Transactional
    suspend fun onDatabaseClient() {
        databaseClient.sql("insert into test values (1)").await()
        throw RuntimeException()
    }

    @Transactional
    suspend fun onDSLContext() {
        Mono.from(dslContext.query("insert into test values (2)")).awaitSingle()
        throw RuntimeException()
    }
}
