package nl.toefel.garsson

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import javax.sql.DataSource

fun createHikariDataSource(cfg: Config): HikariDataSource {
    val config = HikariConfig()
    config.jdbcUrl = cfg.datasourceUrl
    config.username = cfg.datasourceUser
    config.password = cfg.datasourcePassword
    config.driverClassName = cfg.datasourceDriverClassName
    return HikariDataSource(config)
}

fun migrate (ds : DataSource) {
    val flyway = Flyway.configure().dataSource(ds).load()
    flyway.migrate()
}