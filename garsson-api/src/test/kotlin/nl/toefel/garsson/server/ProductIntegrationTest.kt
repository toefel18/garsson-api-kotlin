package nl.toefel.garsson.server

import io.kotlintest.extensions.TestListener
import io.kotlintest.specs.FunSpec
import nl.toefel.garsson.ApplicationRestTest
import nl.toefel.garsson.ApplicationTest
import nl.toefel.garsson.dto.Product
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.greaterThan
import java.time.LocalDate


class ProductIntegrationTest : ApplicationRestTest, FunSpec() {
    override fun listeners(): List<TestListener> = listOf(ApplicationTest)

    init {
        test("Create a product") {
            val product = createProduct("Radler", "Grols")

            post("/api/v1/products", product)
                .statusCode(200)
                .contentType("application/json")
                .body("id", greaterThan(0))
                .body("name", equalTo("Radler"))
                .body("brand", equalTo("Grols"))
                .body("barcode", equalTo("123"))
                .body("pricePerUnit", equalTo("3.75"))
                .body("purchasePricePerUnit", equalTo("1.75"))
                .body("createdTime", containsString(LocalDate.now().year.toString()))
                .body("lastEditTime", containsString(LocalDate.now().year.toString()))

        }

        test("Missing required properties should error") {
            val productMissingName = """{
                "brand": "Coca Cola",
                "barcode": "123",
                "unit": "BOTTLE",
                "pricePerUnit": "3.75",
                "purchasePricePerUnit": "1.75"
            }"""

            post("/api/v1/products", productMissingName)
                .statusCode(400)
                .contentType("application/json")
                .body("message", containsString("Failed to parse request body to Product, missing property name"))

        }

        test("Multiple times same barcode not allowed") {
            val product1 = createProduct("Radler", "Grols").copy(barcode = "same")
            val product2 = createProduct("Radler", "Bavaria").copy(barcode = "same")

            post("/api/v1/products", product1)
                .statusCode(200)
                .contentType("application/json")
                .body("id", greaterThan(0))
                .body("name", equalTo("Radler"))
                .body("brand", equalTo("Grols"))
                .body("barcode", equalTo("same"))
                .body("pricePerUnit", equalTo("3.75"))
                .body("purchasePricePerUnit", equalTo("1.75"))
                .body("createdTime", containsString(LocalDate.now().year.toString()))
                .body("lastEditTime", containsString(LocalDate.now().year.toString()))

            post("/api/v1/products", product2)
                .statusCode(400)
                .contentType("application/json")
                .body("message", containsString("barcode"))
        }

        test("Invalid price") {
            val invalidProduct = createProduct("Radler", "Grols").copy(
                barcode = "999",
                pricePerUnit = "!~#$~")

            post("/api/v1/products", invalidProduct)
                .statusCode(400)
                .contentType("application/json")
        }

        test("Create -> update -> read -> delete product") {
            val product = createProduct("Fanta", "Nestle")

            val productResponse = post("/api/v1/products", product)
                .statusCode(200)
                .contentType("application/json")
                .body("id", greaterThan(0))
                .body("name", equalTo("Fanta"))
                .body("brand", equalTo("Nestle"))
                .body("barcode", equalTo("123"))
                .body("pricePerUnit", equalTo("3.75"))
                .body("purchasePricePerUnit", equalTo("1.75"))
                .body("createdTime", containsString(LocalDate.now().year.toString()))
                .body("lastEditTime", containsString(LocalDate.now().year.toString()))
                .extract().body().`as`(Product::class.java)

            val updatedProduct = product.copy(brand = "Coca Cola")

            put("/api/v1/products/${productResponse.id}", updatedProduct)
                .statusCode(200)
                .contentType("application/json")
                .body("brand", equalTo("Coca Cola"))

            get("/api/v1/products/${productResponse.id}")
                .statusCode(200)
                .contentType("application/json")
                .body("brand", equalTo("Coca Cola"))

            delete("/api/v1/products/${productResponse.id}")
                .statusCode(200)
                .contentType("application/json")
                .body("brand", equalTo("Coca Cola"))

            get("/api/v1/products/${productResponse.id}")
                .statusCode(404)

            delete("/api/v1/products/${productResponse.id}")
                .statusCode(404)
        }
    }

    private fun createProduct(name: String, brand: String): Product = Product(
            id = null,
            name = name,
            brand = brand,
            barcode = "123",
            unit = "BOTTLE",
            pricePerUnit = "3.75",
            purchasePricePerUnit = "1.75",
            createdTime = "will be overwritten",
            lastEditTime = null
        )
}