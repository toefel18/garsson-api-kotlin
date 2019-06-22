package nl.toefel.garsson.server

open class ClientErrorException(message: String, throwable: Throwable? = null) : RuntimeException(message, throwable)

class MissingRequiredParameter(parameterName: String) : ClientErrorException("missing required parameter '$parameterName'")
class InvalidParameterFormat(parameterName: String, expectedType: String, actualValue: String) : ClientErrorException("parameter '$parameterName' is expected to be of type '$expectedType' but has value $actualValue")
class BodyParseException(message: String, val body: ByteArray, cause: Exception) : ClientErrorException(message, cause)
