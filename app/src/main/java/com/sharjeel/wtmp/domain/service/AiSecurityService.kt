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
import kotlin.math.max

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
            Log.e(TAG, "GenerativeModel is null. Firebase AI might not be initialized.")
            return@withContext null
        }

        val appList = formatAccessedApps(event)
        val promptText = buildVisionPrompt(event, appList)

        try {
            val bitmap = loadBitmapFromPath(event.evidencePath)

            val response = if (bitmap != null) {
                Log.d(TAG, "Generating content with image (${bitmap.width}x${bitmap.height})...")
                generativeModel.generateContent(
                    content {
                        image(bitmap)
                        text(promptText)
                    }
                )
            } else {
                Log.d(TAG, "Generating content without image (evidencePath=${event.evidencePath})...")
                generativeModel.generateContent(promptText)
            }

            val resultText = response.text?.trim().orEmpty()
            Log.d(TAG, "AI Response: $resultText")
            if (resultText.isBlank()) {
                return@withContext null
            }
            parseEventAnalysis(resultText, hasImage = bitmap != null)
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
            val raw = response.text?.trim().orEmpty()
            if (raw.isBlank()) return@withContext null
            parseSecurityReport(raw)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating report: ${e.localizedMessage}", e)
            null
        }
    }

    // =================================================================
    // 3. PRIVATE RESPONSE PARSERS
    // =================================================================

    private fun parseEventAnalysis(text: String, hasImage: Boolean): AiEventAnalysis {
        val cleaned = text.replace("```", "").trim()

        fun extractSection(vararg keys: String): String? {
            val joinedKeys = keys.joinToString("|") { Regex.escape(it) }
            val pattern = "(?im)^\\s*(?:\\*\\*)?(?:$joinedKeys)(?:\\*\\*)?\\s*:\\s*(.*?)(?=^\\s*(?:\\*\\*)?(?:Explanation|Risk\\s*Level|RiskLevel|Category|Description|Intruder\\s*Description|IntruderDescription|Recommendation)(?:\\*\\*)?\\s*:|\\z)"
                .toRegex(setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE))

            return pattern.find(cleaned)
                ?.groupValues
                ?.getOrNull(1)
                ?.trim()
                ?.removeSurrounding("**")
                ?.trim()
                ?.takeIf { it.isNotBlank() }
        }

        val explanation = extractSection("Explanation")
            ?: cleaned
        val riskLevel = extractSection("Risk Level", "RiskLevel")
            ?: "Low"
        val category = extractSection("Category")
            ?: "Normal"
        val description = extractSection("Description", "Intruder Description", "IntruderDescription")
            ?: if (hasImage) {
                "Visual details were not separated by the model; check event explanation."
            } else {
                "No image attached for visual subject analysis."
            }
        val recommendation = extractSection("Recommendation")
            ?: "Keep your device credentials secure."

        return AiEventAnalysis(
            explanation = explanation,
            riskLevel = riskLevel,
            category = category,
            intruderDescription = description,
            recommendation = recommendation
        )
    }

    private fun parseSecurityReport(text: String): AiSecurityReport {
        val cleaned = text.replace("```", "").trim()

        fun extractLine(key: String): String? {
            val pattern = "(?im)^\\s*(?:\\*\\*)?${Regex.escape(key)}(?:\\*\\*)?\\s*:\\s*(.+)$".toRegex()
            return pattern.find(cleaned)
                ?.groupValues
                ?.getOrNull(1)
                ?.trim()
                ?.removeSurrounding("**")
                ?.trim()
                ?.takeIf { it.isNotBlank() }
        }

        return AiSecurityReport(
            reportTitle = extractLine("Title") ?: "Security Audit",
            summary = extractLine("Summary") ?: "Report generated.",
            securityScore = extractLine("Score")?.filter { it.isDigit() }?.toIntOrNull()?.coerceIn(0, 100) ?: 80,
            detailedInsights = extractLine("Insights")
                ?.split("|")
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() }
                ?: emptyList(),
            recommendations = extractLine("Recommendations")
                ?.split("|")
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() }
                ?: emptyList()
        )
    }

    // =================================================================
    // 4. PRIVATE HELPER UTILITIES
    // =================================================================

    private fun loadBitmapFromPath(path: String?): Bitmap? {
        if (path.isNullOrBlank()) return null
        val file = File(path)
        if (!file.exists() || !file.canRead()) {
            Log.w(TAG, "Evidence file missing or unreadable: $path")
            return null
        }

        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)

            val maxDim = max(bounds.outWidth, bounds.outHeight)
            var sampleSize = 1
            while (maxDim / sampleSize > 1024) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            BitmapFactory.decodeFile(path, decodeOptions)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode bitmap from $path: ${e.localizedMessage}", e)
            null
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

            IMPORTANT: If an image is provided, provide an accurate, objective visual description of what is visible in the image (person's appearance, clothing, surroundings) without guessing identity.

            Respond in this EXACT format:
            Explanation: [Concise explanation of the event and context]
            RiskLevel: [Low/Medium/High]
            Category: [Normal/Unusual/Suspicious/High-Risk]
            Description: [Objective visual description of person/scene. If no person is visible, say 'No person detected']
            Recommendation: [One actionable security tip]
        """.trimIndent()
    }

    private fun buildReportPrompt(userPrompt: String, eventSummary: String): String {
        return """
            User Request: "$userPrompt"

            Based on these recent security events, generate a professional security report:
            $eventSummary

            Respond in this EXACT format:
            Title: [Report Title]
            Summary: [Concise summary]
            Score: [0-100]
            Insights: [Key points separated by |]
            Recommendations: [Actionable tips separated by |]
        """.trimIndent()
    }
}