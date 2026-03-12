package com.econdashboard.exception

class NotFoundException(
    entityName: String,
    id: Any
) : BusinessException(
    errorCode = "NOT_FOUND",
    message = "$entityName not found with id: $id"
)
