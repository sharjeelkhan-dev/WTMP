package com.sharjeel.wtmp.domain.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.content
import com.sharjeel.wtmp.model.AiEventAnalysis
import com.sharjeel.wtmp.model.AiSecurityReport
import com.sharjeel.wtmp.model.SecurityEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service responsible for processing security events and generating report audits
 * using Firebase AI Generative Models (Gemini Vision & Text).
 */
@Singleton
class AiSecurityService @Inject constructor(
    private val generativeModel: GenerativeModel?
) {

    // =================================================================
    // 1. CONSTANTS & FORMATTERS
    // =================================================================

    private val TAG = "AiSecurityService"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    // =================================================================
    // 2. PUBLIC SERVICE APIs
    // =================================================================

    /**
     * Performs AI Vision and Context analysis on a given [SecurityEvent].
     */
    suspend fun analyzeEventWithVision(event: SecurityEvent): AiEventAnalysis? = withContext(Dispatchers.IO) {
        if (generativeModel == null) {
            Log.e(TAG, "GenerativeModel is null. Firebase might not be initialized.")
            return@withContext null
        }

        val appList = formatAccessedApps(event)
        val promptText = buildVisionPrompt(event, appList)

        try {
            val bitmap = loadBitmapFromPath(event.evidencePath)

            val response = if (bitmap != null) {
                Log.d(TAG, "Generating content with image...")
                generativeModel.generateContent(
                    content {
                        image(bitmap)
                        text(promptText)
                    }
                )
            } else {
                Log.d(TAG, "Generating content without image...")
                generativeModel.generateContent(promptText)
            }

            val resultText = response.text ?: ""
            Log.d(TAG, "AI Response: $resultText")
            parseEventAnalysis(resultText)
        } catch (e: Exception) {
            Log.e(TAG, "Error during AI analysis: ${e.localizedMessage}", e)
            null
        }
    }

    /**
     * Generates a broad security report audit based on a user prompt and recent events list.
     */
    suspend fun generateCustomReport(userPrompt: String, events: List<SecurityEvent>): AiSecurityReport? = withContext(Dispatchers.IO) {
        if (generativeModel == null) return@withContext null

        val eventSummary = events.take(20).joinToString("\n") {
            "${dateFormat.format(Date(it.timestamp))}: ${it.type.title} (${it.deviceState})"
        }

        val prompt = buildReportPrompt(userPrompt, eventSummary)

        try {
            val response = generativeModel.generateContent(prompt)
            parseSecurityReport(response.text ?: "")
        } catch (e: Exception) {
            Log.e(TAG, "Error generating report: ${e.localizedMessage}")
            null
        }
    }

    // =================================================================
    // 3. PRIVATE RESPONSE PARSERS
    // =================================================================

    private fun parseEventAnalysis(text: String): AiEventAnalysis {
        fun extractValue(key: String): String? {
            val pattern = "(?i)$key:\\s*(.*)".toRegex()
            return pattern.find(text)?.groupValues?.get(1)?.trim()?.removeSurrounding("**")
        }

        return AiEventAnalysis(
            explanation = extractValue("Explanation") ?: "Analyzed security event.",
            riskLevel = extractValue("RiskLevel") ?: "Low",
            category = extractValue("Category") ?: "Normal Activity",
            intruderDescription = extractValue("Description"),
            recommendation = extractValue("Recommendation") ?: "Keep your device secure."
        )
    }

    private fun parseSecurityReport(text: String): AiSecurityReport {
        fun extractValue(key: String): String? {
            val pattern = "(?i)$key:\\s*(.*)".toRegex()
            return pattern.find(text)?.groupValues?.get(1)?.trim()?.removeSurrounding("**")
        }

        return AiSecurityReport(
            reportTitle = extractValue("Title") ?: "Security Audit",
            summary = extractValue("Summary") ?: "Report generated.",
            securityScore = extractValue("Score")?.filter { it.isDigit() }?.toIntOrNull() ?: 80,
            detailedInsights = extractValue("Insights")?.split("|")?.map { it.trim() } ?: emptyList(),
            recommendations = extractValue("Recommendations")?.split("|")?.map { it.trim() } ?: emptyList()
        )
    }

    // =================================================================
    // 4. PRIVATE HELPER UTILITIES
    // =================================================================

    private fun loadBitmapFromPath(path: String?): Bitmap? {
        return path?.let {
            if (File(it).exists()) {
                Log.d(TAG, "Loading bitmap from: $it")
                BitmapFactory.decodeFile(it)
            } else {
                Log.w(TAG, "Evidence file does not exist: $it")
                null
            }
        }
    }

    private fun formatAccessedApps(event: SecurityEvent): String {
        return if (event.accessedApps.isEmpty()) "None" else event.accessedApps.joinToString(", ") { it.appName }
    }

    private fun buildVisionPrompt(event: SecurityEvent, appList: String): String {
        return """
            Analyze this security event from my "Who Touched My Phone" app:
            - Event Type: ${event.type.title}
            - Time: ${dateFormat.format(Date(event.timestamp))}
            - Device State: ${event.deviceState}
            - Accessed Apps: $appList
            
            IMPORTANT: If an image is provided, provide a detailed visual description of the person (gender, age group, clothing, identifying features).
            
            Respond in this EXACT format:
            Explanation: [Conversational explanation]
            RiskLevel: [Low/Medium/High]
            Category: [Normal/Unusual/Suspicious/High-Risk]
            Description: [Detailed visual description of person. If no person, say 'No person detected']
            Recommendation: [One actionable tip]
        """.trimIndent()
    }

    private fun buildReportPrompt(userPrompt: String, eventSummary: String): String {
        return """
            User Request: "$userPrompt"
            
            Based on these recent security events, generate a professional security report:
            $eventSummary
            
            Respond in this EXACT format:
            Title: [Report Title]
            Summary: [Conversational summary]
            Score: [0-100]
            Insights: [Bullet points separated by |]
            Recommendations: [Actionable tips separated by |]
        """.trimIndent()
    }
}