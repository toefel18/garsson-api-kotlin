package nl.toefel.garsson.util

import io.restassured.RestAssured

fun main() {
    createProduct("Cassis", "Fanta", "1231324634")
    createProduct("Fanta Exotic", "Fanta", "2231324634")
    createProduct("Fanta Lemon", "Fanta", "3231324634")
    createProduct("Chocolade melk", "Nestle", "4231324634")
    createProduct("Loza Ace", "Nestle", "5231324634")
    createProduct("Jupiler", "Jupiler", "6231324634")
    createProduct("Jupiler Blue", "Juplier", "7231324634")

}

private fun createProduct(name: String, brand: String, barcode: String,
                          unit: String = "Bottle", price:String = "1.75", cost: String= "1.20") {
    post("http://localhost:8080/api/v1/products", """{
        "name":"$name",
        "brand":"$brand",
        "barcode":"$barcode",
        "unit":"$unit",
        "pricePerUnit":"$price",
        "purchasePricePerUnit":"$cost"
        }""".trimIndent())
}


fun post(url: String, data:Any) = RestAssured.given().log().all()
    .contentType("application/json")
    .body(data)
    .post(url)
    .then().log().all()
