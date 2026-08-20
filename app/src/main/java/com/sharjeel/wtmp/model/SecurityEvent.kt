package com.sharjeel.wtmp.model

import java.util.UUID

enum class SecurityEventType(val title: String) {
    DEVICE_UNLOCKED("Device Unlocked"),
    FAILED_UNLOCK("Failed Unlock Attempt"),
    UNEXPECTED_UNLOCK("Unexpected Unlock"),
    FAILED_ATTEMPT("Failed Attempt"),
    APP_OPENED("App Opened"),
    POWER_CONNECTED("Power Connected")
}

enum class EventSeverity {
    LOW, MEDIUM, HIGH
}

data class SecurityEvent(
    val id: String = UUID.randomUUID().toString(),
    val type: SecurityEventType,
    val timestamp: Long,
    val severity: EventSeverity = EventSeverity.LOW,
    val sessionDuration: String = "0m",
    val deviceState: String = "Locked",
    val evidencePath: String? = null
)