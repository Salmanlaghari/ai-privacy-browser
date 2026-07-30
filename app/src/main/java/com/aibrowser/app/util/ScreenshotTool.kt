package com.aibrowser.app.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.webkit.WebView
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

/**
 * Screenshot utility for capturing full web pages or visible area.
 */
object ScreenshotTool {

    /**
     * Capture the visible area of a WebView
     */
    fun captureVisible(webView: WebView, context: Context): String? {
        return try {
            val bitmap = Bitmap.createBitmap(webView.width, webView.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            webView.draw(canvas)
            saveBitmap(bitmap, context)
        } catch (e: Exception) {
            Timber.e(e, "Failed to capture visible area")
            null
        }
    }

    /**
     * Capture the full scrollable page (entire page, not just visible area)
     */
    fun captureFullPage(webView: WebView, context: Context): String? {
        return try {
            val originalHeight = webView.height
            val contentHeight = (webView.contentHeight * webView.scale).toInt()
            val bitmap = Bitmap.createBitmap(webView.width, contentHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Draw the full page by scrolling
            webView.scrollTo(0, 0)
            webView.draw(canvas)

            // Restore original scroll position
            webView.scrollTo(0, webView.scrollY)

            saveBitmap(bitmap, context)
        } catch (e: Exception) {
            Timber.e(e, "Failed to capture full page")
            null
        }
    }

    /**
     * Save bitmap to gallery
     */
    private fun saveBitmap(bitmap: Bitmap, context: Context): String? {
        val filename = "AI_Browser_Screenshot_${System.currentTimeMillis()}.png"

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/AI_Browser")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                    contentValues
                ) ?: return null

                context.contentResolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }

                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)

                Timber.d("Screenshot saved to gallery: $filename")
                uri.toString()
            } else {
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "AI_Browser")
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, filename)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                Timber.d("Screenshot saved: ${file.absolutePath}")
                file.absolutePath
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to save screenshot")
            null
        }
    }
}
