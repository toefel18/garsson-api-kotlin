package nl.toefel.garsson.repository

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.Column
import java.math.BigDecimal

/**
 * @see https://medium.com/@OhadShai/bits-and-blobs-of-kotlin-exposed-jdbc-framework-f1ee56dc8840
 * @See https://github.com/JetBrains/Exposed/wiki/DSL
 */

object ProductsTable : LongIdTable(columnName = "id") {
    val name: Column<String> = varchar("name", 255).uniqueIndex()
    val brand: Column<String> = varchar("brand", 255)
    val barcode: Column<String?> = varchar("barcode", 255).nullable()
    val unit: Column<String> = varchar("unit", 255)
    val pricePerUnit: Column<BigDecimal> = decimal("price_per_unit", 13, 2)
    val purchasePricePerUnit: Column<BigDecimal?> = decimal("purchase_price_per_unit", 13, 2).nullable()
    val createdTime: Column<String> = varchar("created_time", 64)
    val lastEditTime: Column<String?> = varchar("last_edit_time", 64).nullable()
}

class ProductEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ProductEntity>(ProductsTable)

    var name by ProductsTable.name
    var brand by ProductsTable.brand
    var barcode by ProductsTable.barcode
    var unit by ProductsTable.unit
    var pricePerUnit by ProductsTable.pricePerUnit
    var purchasePricePerUnit by ProductsTable.purchasePricePerUnit
    var createdTime by ProductsTable.createdTime
    var lastEditTime by ProductsTable.lastEditTime
}
