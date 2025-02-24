package io.github.t45k.jooq_rollback

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.await
import org.springframework.r2dbc.core.awaitOne

@SpringBootTest
class JooqRollbackApplicationTest {
    @Autowired
    lateinit var databaseClient: DatabaseClient

    @Autowired
    lateinit var rollbackService: RollbackService

    @BeforeEach
    fun setup() = runTest {
        databaseClient.sql("drop table if exists test").await()
        databaseClient.sql("create table test(id int not null)").await()
    }

    @Test
    fun testRollback() = runTest {
        try {
            rollbackService.onDatabaseClient()
            assertTrue(false)
        } catch (_: Exception) {
            assertEquals(0L, databaseClient.sql("select count(*) from test").fetch().awaitOne()["COUNT(*)"])
        }

        try {
            rollbackService.onDatabaseClientWithReactor().awaitSingle()
            assertTrue(false)
        } catch (_: Exception) {
            assertEquals(0L, databaseClient.sql("select count(*) from test").fetch().awaitOne()["COUNT(*)"])
        }

        try {
            rollbackService.onDSLContext()
            assertTrue(false)
        } catch (_: Exception) {
            assertEquals(0L, databaseClient.sql("select count(*) from test").fetch().awaitOne()["COUNT(*)"])
        }

        try {
            rollbackService.onDSLContextWithReactor().awaitSingle()
            assertTrue(false)
        } catch (_: Exception) {
            assertEquals(0L, databaseClient.sql("select count(*) from test").fetch().awaitOne()["COUNT(*)"])
        }
    }
}
