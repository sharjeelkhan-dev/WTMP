package com.sharjeel.wtmp.model

import kotlinx.serialization.Serializable

// =================================================================
// 1. EVENT VISION ANALYSIS MODEL
// =================================================================

/**
 * Data class representing AI Gemini Vision analysis for a specific security event.
 */
@Serializable
data class AiEventAnalysis(
    val explanation: String = "",
    val riskLevel: String = "Low",
    val category: String = "Normal", // Normal, Unusual, Suspicious, High-Risk
    val recommendation: String = "",
    val intruderDescription: String? = null, // Objective visual description of subject/scene
    val isLiveVisionResult: Boolean = true
)

// =================================================================
// 2. SECURITY AUDIT REPORT MODEL
// =================================================================

/**
 * Data class representing a consolidated AI Security Audit Report.
 */
@Serializable
data class AiSecurityReport(
    val reportTitle: String = "Security Audit",
    val summary: String = "",
    val securityScore: Int = 80,
    val detailedInsights: List<String> = emptyList(),
    val recommendations: List<String> = emptyList()
)