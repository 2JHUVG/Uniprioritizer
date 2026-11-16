package com.example.uniprioritizer.domain.model

/** Error coming from the data layer. [message] is already written for the user, in Spanish. */
class DataException(
    override val message: String,
    cause: Throwable? = null
) : Exception(message, cause)
