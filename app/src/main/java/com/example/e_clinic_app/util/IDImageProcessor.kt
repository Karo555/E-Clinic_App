package com.example.e_clinic_app.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException

/**
 * Helper class for processing images with ML Kit OCR
 */
class IDImageProcessor(private val context: Context) {

    private val recognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Process a bitmap image with OCR
     *
     * @param bitmap The bitmap image to process
     * @param onSuccess Callback for successful text recognition
     * @param onError Callback for errors
     */
    fun processImage(
        bitmap: Bitmap,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val image = InputImage.fromBitmap(bitmap, 0)
        processInputImage(image, onSuccess, onError)
    }

    /**
     * Process an image Uri with OCR
     *
     * @param uri The Uri of the image to process
     * @param onSuccess Callback for successful text recognition
     * @param onError Callback for errors
     */
    fun processImage(
        uri: Uri,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val image = InputImage.fromFilePath(context, uri)
            processInputImage(image, onSuccess, onError)
        } catch (e: IOException) {
            Log.e("IDImageProcessor", "Error creating InputImage from Uri", e)
            onError(e)
        }
    }

    /**
     * Process an InputImage with OCR
     */
    private fun processInputImage(
        image: InputImage,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                onSuccess(visionText.text)
            }
            .addOnFailureListener { e ->
                Log.e("IDImageProcessor", "Error processing image with OCR", e)
                onError(e)
            }
    }
}
