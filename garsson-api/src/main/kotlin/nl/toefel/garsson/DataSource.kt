package nl.toefel.garsson

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.sql.DataSource

const val maxBackoffMs = 8000L
val defaultBackoffMsSequence = generateSequence(1000L) { Math.min(it * 2, maxBackoffMs)}

val logger: Logger = LoggerFactory.getLogger("DataSource.kt")

/**
 * Creates a HikariDataSource and returns it. If any exception is thrown, the operation is retried according
 * to the provided backoff sequence. If the sequence runs out of entries, the operation fails with the last
 * encountered exception.
 */
tailrec fun createHikariDataSource(cfg: Config, backoffSequence: Iterator<Long> = defaultBackoffMsSequence.iterator()): HikariDataSource {
    try {
        val config = HikariConfig()
        config.jdbcUrl = cfg.datasourceUrl
        config.username = cfg.datasourceUser
        config.password = cfg.datasourcePassword
        config.driverClassName = cfg.datasourceDriverClassName
        return HikariDataSource(config)
    } catch (ex: Exception) {
        logger.error("Failed to create data source ${ex.message}")
        if (!backoffSequence.hasNext()) throw ex
    }
    val backoffMillis = backoffSequence.next() / 1000
    logger.info("Trying again in $backoffMillis millis")
    Thread.sleep(backoffMillis)
    return createHikariDataSource(cfg, backoffSequence)
}

fun migrate(ds: DataSource) {
    val flyway = Flyway.configure().dataSource(ds).load()
    flyway.migrate()
}