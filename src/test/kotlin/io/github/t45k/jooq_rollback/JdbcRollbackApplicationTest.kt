package io.github.t45k.jooq_rollback

import kotlin.test.Test
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.test.runTest
import org.jooq.DSLContext
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.await
import org.springframework.r2dbc.core.awaitOne
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@SpringBootTest
class JdbcRollbackApplicationTest {
    @Autowired
    lateinit var dslContext: DSLContext

    @Autowired
    lateinit var databaseClient: DatabaseClient

    @Autowired
    lateinit var transactionalOperator: TransactionalOperator

    @BeforeEach
    fun setup() = runTest {
        databaseClient.sql("drop table if exists test").await()
        databaseClient.sql("create table test(id int not null)").await()
    }

    @Test
    fun testRollback() = runTest {
        try {
            transactionalOperator.executeAndAwait {
                databaseClient.sql("insert into test values (1)").await()
                throw RuntimeException()
            }
        } catch (_: Exception) {
        }

        println(databaseClient.sql("select count(*) from test").fetch().awaitOne())

        try {
            transactionalOperator.executeAndAwait {
                dslContext.query("insert into test values (2)").awaitSingle()
                throw RuntimeException()
            }
        } catch (_: Exception) {
        }

        println(databaseClient.sql("select count(*) from test").fetch().awaitOne())
    }
}
