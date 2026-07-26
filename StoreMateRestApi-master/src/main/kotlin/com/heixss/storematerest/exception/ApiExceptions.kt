package com.heixss.storematerest.exception

/** The requested entity does not exist. Rendered as 404. */
class NotFoundException(message: String) : RuntimeException(message)

/**
 * The entity exists but belongs to another store. Rendered as 404 rather than
 * 403 so the API does not confirm the existence of other stores' records.
 */
class ForeignStoreException(message: String) : RuntimeException(message)

/** The request is well-formed but not valid for the current state. Rendered as 400. */
class InvalidRequestException(message: String) : RuntimeException(message)

/** The entity already exists. Rendered as 409. */
class ConflictException(message: String) : RuntimeException(message)
