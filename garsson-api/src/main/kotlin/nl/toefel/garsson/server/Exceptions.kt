package nl.toefel.garsson.server

open class ServerException(message: String, throwable: Throwable? = null) : RuntimeException(message, throwable)

class MissingRequiredParameter(parameterName: String) : ServerException("missing required parameter '$parameterName'")
class InvalidRequiredParameter(parameterName: String, expectedType: String, actualValue: String) : ServerException("parameter '$parameterName' is expected to be of type '$expectedType' but has value $actualValue")

