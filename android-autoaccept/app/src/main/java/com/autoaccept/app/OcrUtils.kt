package com.autoaccept.app

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object OcrUtils {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun readDistances(bitmap: Bitmap): Pair<Double, Double>? = suspendCancellableCoroutine { continuation ->
        val image = InputImage.fromBitmap(bitmap, 0)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val text = visionText.text.lowercase()

                val pickupRegex = """pickup\s*distance[^0-9]*([0-9]+(?:\.[0-9]+)?)""".toRegex()
                val dropRegex = """drop\s*distance[^0-9]*([0-9]+(?:\.[0-9]+)?)""".toRegex()

                val pickupMatch = pickupRegex.find(text)
                val dropMatch = dropRegex.find(text)

                if (pickupMatch != null && dropMatch != null) {
                    val pickup = pickupMatch.groupValues[1].toDoubleOrNull()
                    val drop = dropMatch.groupValues[1].toDoubleOrNull()

                    if (pickup != null && drop != null) {
                        continuation.resume(Pair(pickup, drop))
                        return@addOnSuccessListener
                    }
                }

                continuation.resume(null)
            }
            .addOnFailureListener {
                continuation.resume(null)
            }
    }
}
