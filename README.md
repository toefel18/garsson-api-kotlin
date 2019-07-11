# Garsson API [![Build Status](https://travis-ci.org/toefel18/garsson-api-kotlin.svg?branch=master)](https://travis-ci.org/toefel18/garsson-api-kotlin)

## Notes:

 Use commit() explicitly when returning a response inside a transaction, 
 otherwise the response might be flushed before the database persists the entity!


 Nested transaction semantics 
 
```kotlin
fun restHandler() {
    transaction {  //outer transaction
        // do some work
        transaction {  // inner transaction
           //do some work
        }  
        // inner transaction does not commit
    }
    // outer transaction + inner transaction commits.
}
```


## Starting a local database 

    docker run --name garsson-api-kotlin-db -p 5432:5432 -e POSTGRES_USER=garsson-api -e POSTGRES_PASSWORD=garsson-api -d postgres