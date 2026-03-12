package com.econdashboard.exception

open class BusinessException(
    val errorCode: String,
    override val message: String
) : RuntimeException(message)
