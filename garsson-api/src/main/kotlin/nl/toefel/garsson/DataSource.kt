package nl.toefel.garsson

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Duration.ofSeconds
import javax.sql.DataSource

val defaultBackoffSequence = listOf<Duration>(
    ofSeconds(1),
    ofSeconds(2),
    ofSeconds(5),
    ofSeconds(10),
    ofSeconds(10),
    ofSeconds(10),
    ofSeconds(10)
)

val logger: Logger = LoggerFactory.getLogger("DataSource.kt")

tailrec fun createHikariDataSource(cfg: Config, backoffSequence: List<Duration> = defaultBackoffSequence): HikariDataSource {
    try {
        val config = HikariConfig()
        config.jdbcUrl = cfg.datasourceUrl
        config.username = cfg.datasourceUser
        config.password = cfg.datasourcePassword
        config.driverClassName = cfg.datasourceDriverClassName
        return HikariDataSource(config)
    } catch (ex: Exception) {
        logger.error("Failed to create data source ${ex.message}")
        if (backoffSequence.size <= 1) throw ex
    }
    logger.info("Trying again in ${backoffSequence.first().seconds} seconds")
    Thread.sleep(backoffSequence.first().toMillis())
    return createHikariDataSource(cfg, backoffSequence.drop(1))
}

fun migrate(ds: DataSource) {
    val flyway = Flyway.configure().dataSource(ds).load()
    flyway.migrate()
}