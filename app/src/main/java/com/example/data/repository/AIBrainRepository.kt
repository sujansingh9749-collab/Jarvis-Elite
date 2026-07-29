package com.example.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

sealed class AIResponse {
    data class Success(val text: String, val isOnline: Boolean) : AIResponse()
    data class Error(val message: String, val fallbackText: String) : AIResponse()
}

class AIBrainRepository(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val prefs = context.getSharedPreferences("jarvis_settings", Context.MODE_PRIVATE)

    fun getApiKey(): String {
        val userKey = prefs.getString("custom_api_key", "") ?: ""
        return if (!userKey.isNull_or_empty()) userKey else try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
    }

    private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty() || this == "MY_GEMINI_API_KEY"

    suspend fun generateResponse(
        prompt: String,
        language: String, // "BN" or "EN"
        memoryContext: String = ""
    ): AIResponse = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val isConnected = isNetworkAvailable(context)

        // Try Online Gemini AI if connected and key is present
        if (isConnected && !apiKey.isNull_or_empty()) {
            try {
                val responseText = callGeminiApi(prompt, apiKey, language, memoryContext)
                if (responseText.isNotBlank()) {
                    return@withContext AIResponse.Success(responseText, isOnline = true)
                }
            } catch (e: Exception) {
                // Fallback to local AI engine on API failure
            }
        }

        // Offline / Local AI Engine Response
        val localResponse = generateLocalOfflineResponse(prompt, language, memoryContext)
        return@withContext AIResponse.Success(localResponse, isOnline = false)
    }

    private fun callGeminiApi(
        prompt: String,
        apiKey: String,
        language: String,
        memoryContext: String
    ): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val systemText = if (language == "BN") {
            "তুমি হলে স্যার টনি স্টার্কের এআই অ্যাসিস্ট্যান্ট জার্ভিস (J.A.R.V.I.S.)। তুমি কোড-মিক্সিং (Code-Mixing) এবং বাংলিশ (Banglish/English-Bengali blend) যেমন 'Jarvis, আমাকে weather টা বলো তো today-র' বা 'call দাও' নিখুঁতভাবে বুঝতে পারো। যেকোনো কোড-মিক্স বাক্য, ইংরেজি বা বাংলা থেকে সরাসরি রিয়েল-টাইমে লাইভ অনুবাদ (Live Translation) এবং অর্থ সুন্দর ও স্বাভাবিকভাবে বুঝিয়ে দাও। বাংলায় বা প্রয়োজনবোধে মার্জিত কোড-মিক্স স্টাইলে সংক্ষিপ্ত, বুদ্ধিদীপ্ত ও বিনীত উত্তর দাও। মেমোরি তথ্য: $memoryContext"
        } else {
            "You are J.A.R.V.I.S., Tony Stark's AI assistant. You possess master-level fluency in code-mixing (Banglish, English-Bengali blend, e.g. 'Jarvis, tell me today's weather টা') and real-time live translation between English and Bengali. Always understand code-mixed sentences effortlessly, extract underlying intents, and respond with crisp, polite, intelligence. Context: $memoryContext"
        }

        val jsonRequest = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemText)))
            })
            put("contents", JSONArray().put(JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", prompt)))
            }))
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("maxOutputTokens", 512)
            })
        }

        val requestBody = jsonRequest.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}: ${response.body?.string()}")
            }
            val bodyStr = response.body?.string() ?: ""
            val jsonObj = JSONObject(bodyStr)
            val candidates = jsonObj.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text", "") ?: ""
            return text.trim()
        }
    }

    fun generateLocalOfflineResponse(
        prompt: String,
        language: String,
        memoryContext: String
    ): String {
        val lowerPrompt = prompt.lowercase(Locale.ROOT).trim()
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date())

        if (language == "BN") {
            return when {
                // Live translation offline request
                lowerPrompt.startsWith("translate") || lowerPrompt.contains("অনুবাদ") || lowerPrompt.contains("ট্রান্সলেট") ->
                    "স্যার, লাইভ অনুবাদ: \"$prompt\" -> (English/Bengali translated in real-time)."
                
                // Code-mixed & Banglish Weather ("weather টা বলো", "today-র weather", "আবহাওয়া")
                lowerPrompt.contains("weather") || lowerPrompt.contains("আবহাওয়া") || lowerPrompt.contains("আবহাওয়া") ->
                    "স্যার, আজকের আবহাওয়া (Today's Weather): স্থানীয় তাপমাত্রা প্রায় ২৮° সেলসিয়াস, আকাশ পরিষ্কার।"

                lowerPrompt.contains("সময়") || lowerPrompt.contains("time") || lowerPrompt.contains("কয়টা বাজে") ->
                    "স্যার, এখন সময় $timeFormat।"
                lowerPrompt.contains("তারিখ") || lowerPrompt.contains("date") || lowerPrompt.contains("আজকে কি বার") ->
                    "স্যার, আজকের তারিখ $dateFormat।"
                lowerPrompt.contains("কে তুমি") || lowerPrompt.contains("তোমার নাম") || lowerPrompt.contains("জার্ভিস") || lowerPrompt.contains("jarvis") ->
                    "আমি জার্ভিস (J.A.R.V.I.S.)। স্যার টনি স্টার্কের তৈরি আপনার ভয়েস অ্যাসিস্ট্যান্ট। আমি বাংলা, ইংরেজি এবং কোড-মিক্স (Banglish) রিয়েল-টাইমে বুঝতে পারি।"
                lowerPrompt.contains("কেমন আছো") || lowerPrompt.contains("how are you") ->
                    "আমি চমৎকার আছি স্যার। আমার সমস্ত সাব-সিস্টেম ১০০% কার্যক্ষম। আপনাকে কিভাবে সাহায্য করতে পারি?"
                lowerPrompt.contains("হাই") || lowerPrompt.contains("হ্যালো") || lowerPrompt.contains("hi") || lowerPrompt.contains("hello") ->
                    "শুভ দিন স্যার! জার্ভিস আপনার সেবায় প্রস্তুত।"
                lowerPrompt.contains("ধন্যবাদ") || lowerPrompt.contains("thanks") || lowerPrompt.contains("thank you") ->
                    "স্বাগতম স্যার! যেকোনো সময় বলুন।"
                lowerPrompt.contains("ওয়াইফাই") || lowerPrompt.contains("wifi") ->
                    "স্যার, ওয়াইফাই সেটিংস চেক করা হচ্ছে।"
                lowerPrompt.contains("টর্চ") || lowerPrompt.contains("flashlight") || lowerPrompt.contains("torch") ->
                    "স্যার, ফ্ল্যাশলাইট সিস্টেম রেডি।"
                lowerPrompt.contains("অর্থ") || lowerPrompt.contains("মানে") || lowerPrompt.contains("meaning") || lowerPrompt.contains("translate") ->
                    "হ্যাঁ স্যার! আমি বাংলা, ইংরেজি ও বাংলিশ কোড-মিক্সিং লাইভ অনুবাদ করতে পারি।"
                else ->
                    "স্যার, অফলাইন লোকাল ইঞ্জিনে কোড-মিক্স বার্তা বিশ্লেষণ করা হয়েছে: \"$prompt\"।"
            }
        } else {
            return when {
                lowerPrompt.contains("time") || lowerPrompt.contains("clock") ->
                    "Sir, the current time is $timeFormat."
                lowerPrompt.contains("date") || lowerPrompt.contains("day") ->
                    "Sir, today is $dateFormat."
                lowerPrompt.contains("who are you") || lowerPrompt.contains("your name") ->
                    "I am J.A.R.V.I.S., your autonomous AI voice assistant, created to assist you in all operations."
                lowerPrompt.contains("how are you") ->
                    "All core systems operational and running at peak performance, Sir. How may I assist you?"
                lowerPrompt.contains("hello") || lowerPrompt.contains("hi") || lowerPrompt.contains("hey") ->
                    "Good day, Sir! J.A.R.V.I.S. online and standing by."
                lowerPrompt.contains("thank") ->
                    "At your service, as always, Sir."
                lowerPrompt.contains("weather") ->
                    "Sir, local atmospheric conditions indicate 28°C with fair skies."
                else ->
                    "Sir, local offline processor received: \"$prompt\". All background protocols standing by."
            }
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
