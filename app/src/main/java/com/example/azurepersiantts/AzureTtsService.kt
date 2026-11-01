package com.example.azurepersiantts

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioFormat
import android.speech.tts.SynthesisCallback
import android.speech.tts.SynthesisRequest
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeechService
import android.speech.tts.Voice
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.*

class AzureTtsService : TextToSpeechService() {

    private lateinit var prefs: SharedPreferences
    private val client = OkHttpClient()

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("azure_tts", Context.MODE_PRIVATE)
    }

    override fun onGetLanguage(): Array<String> {
        return arrayOf("fas", "IRN", "")
    }

    override fun onIsLanguageAvailable(lang: String?, country: String?, variant: String?): Int {
        return if (lang == "fas" || lang == "fa") TextToSpeech.LANG_AVAILABLE
        else TextToSpeech.LANG_NOT_SUPPORTED
    }

    override fun onLoadLanguage(lang: String?, country: String?, variant: String?): Int {
        return onIsLanguageAvailable(lang, country, variant)
    }

    override fun onGetVoices(): MutableList<Voice> {
        val voices = mutableListOf<Voice>()
        val features = HashSet<String>()
        voices.add(Voice("Dilara", Locale("fa", "IR"), Voice.QUALITY_VERY_HIGH, Voice.LATENCY_NORMAL, false, features))
        return voices
    }

    override fun onIsValidVoiceName(voiceName: String?): Int {
        return if (voiceName == "Dilara") TextToSpeech.SUCCESS else TextToSpeech.ERROR
    }

    override fun onLoadVoice(voiceName: String?): Int {
        return onIsValidVoiceName(voiceName)
    }

    override fun onStop() {}

    override fun onSynthesizeText(request: SynthesisRequest?, callback: SynthesisCallback?) {
        if (request == null || callback == null) return

        val text = request.charSequenceText?.toString() ?: return
        val key = prefs.getString("azure_key", null)
        val region = prefs.getString("azure_region", "eastus") ?: "eastus"

        if (key.isNullOrEmpty()) {
            callback.error()
            return
        }

        callback.start(16000, AudioFormat.ENCODING_PCM_16BIT, 1)

        val ssml = """
            <speak version='1.0' xml:lang='fa-IR'>
                <voice name='fa-IR-DilaraNeural'>$text</voice>
            </speak>
        """.trimIndent()

        val body = ssml.toRequestBody("application/ssml+xml".toMediaType())
        val url = "https://$region.tts.speech.microsoft.com/cognitiveservices/v1"

        val req = Request.Builder()
            .url(url)
            .addHeader("Ocp-Apim-Subscription-Key", key)
            .addHeader("Content-Type", "application/ssml+xml")
            .addHeader("X-Microsoft-OutputFormat", "raw-16khz-16bit-mono-pcm")
            .addHeader("User-Agent", "AzurePersianTTS")
            .post(body)
            .build()

        client.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.error()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback.error()
                    return
                }
                val bytes = response.body?.bytes()
                if (bytes != null) {
                    callback.audioAvailable(bytes, 0, bytes.size)
                    callback.done()
                } else {
                    callback.error()
                }
            }
        })
    }
}
