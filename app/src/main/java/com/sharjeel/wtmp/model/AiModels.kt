package com.sharjeel.wtmp.model

import kotlinx.serialization.Serializable

@Serializable
data class AiEventAnalysis(
    val explanation: String,
    val riskLevel: String,
    val category: String, // Normal, Unusual, Suspicious, High-Risk
    val recommendation: String,
    val intruderDescription: String? = null // For visual description
)

@Serializable
data class AiSecurityReport(
    val reportTitle: String,
    val summary: String,
    val securityScore: Int,
    val detailedInsights: List<String> = emptyList(),
    val recommendations: List<String> = emptyList()
)
