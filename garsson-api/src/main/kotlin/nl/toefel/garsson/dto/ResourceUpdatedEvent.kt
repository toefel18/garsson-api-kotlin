package nl.toefel.garsson.dto

enum class UpdateType {
    ADDED,
    DELETED,
    UPDATED
}

data class ResourceUpdatedEvent(
    val resourceName: String,
    val resourceId: String,
    val updateType: UpdateType,
    val byUser: String
)