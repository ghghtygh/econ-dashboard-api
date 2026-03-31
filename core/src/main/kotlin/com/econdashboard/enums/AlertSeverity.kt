package com.econdashboard.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class AlertSeverity(@get:JsonValue val value: String) {
    INFO("info"),
    WARNING("warning"),
    DANGER("danger");

    companion object {
        fun fromValue(value: String): AlertSeverity =
            entries.firstOrNull { it.value == value } ?: WARNING
    }
}
