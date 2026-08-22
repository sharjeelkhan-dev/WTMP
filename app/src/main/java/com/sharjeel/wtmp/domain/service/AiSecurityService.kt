package com.sharjeel.wtmp.domain.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.content
import com.sharjeel.wtmp.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiSecurityService @Inject constructor(
    private val generativeModel: GenerativeModel?
) {
    private val TAG = "AiSecurityService"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    suspend fun analyzeEventWithVision(event: SecurityEvent): AiEventAnalysis? = withContext(Dispatchers.IO) {
        if (generativeModel == null) {
            Log.e(TAG, "GenerativeModel is null. Firebase might not be initialized.")
            return@withContext null
        }

        val appList = if (event.accessedApps.isEmpty()) "None" else event.accessedApps.joinToString(", ") { it.appName }
        
        val promptText = """
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

        try {
            val bitmap = event.evidencePath?.let { path ->
                if (File(path).exists()) {
                    Log.d(TAG, "Loading bitmap from: $path")
                    BitmapFactory.decodeFile(path)
                } else {
                    Log.w(TAG, "Evidence file does not exist: $path")
                    null
                }
            }

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

    suspend fun generateCustomReport(userPrompt: String, events: List<SecurityEvent>): AiSecurityReport? = withContext(Dispatchers.IO) {
        if (generativeModel == null) return@withContext null

        val eventSummary = events.take(20).joinToString("\n") { 
            "${dateFormat.format(Date(it.timestamp))}: ${it.type.title} (${it.deviceState})" 
        }

        val prompt = """
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

        try {
            val response = generativeModel.generateContent(prompt)
            parseSecurityReport(response.text ?: "")
        } catch (e: Exception) {
            Log.e(TAG, "Error generating report: ${e.localizedMessage}")
            null
        }
    }

    private fun parseEventAnalysis(text: String): AiEventAnalysis {
        // Handle variations in AI output (sometimes it adds markdown symbols)
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
}
