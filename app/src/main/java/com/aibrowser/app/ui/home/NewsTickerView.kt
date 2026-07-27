package com.aibrowser.app.ui.home

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 * Custom TextView for rotating local South Asian news headlines like a ticker.
 */
class NewsTickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val headlines = listOf(
        "📰 Dawn News: Pakistan launches digital privacy and cybersecurity initiative.",
        "⚽ Cricbuzz: Historic victory for Pakistan in the regional championship tournament!",
        "🚀 TechJuice: Tech start-ups in South Asia secure record-breaking funding series."
    )

    private var currentIndex = 0
    private val tickerRunnable = object : Runnable {
        override fun run() {
            if (headlines.isNotEmpty()) {
                text = headlines[currentIndex]
                currentIndex = (currentIndex + 1) % headlines.size
                postDelayed(this, 4000L) // Rotate every 4 seconds
            }
        }
    }

    init {
        // Default text on initialization
        text = headlines[0]
        startTicker()
    }

    /**
     * Starts rotating the news ticker headlines.
     */
    fun startTicker() {
        removeCallbacks(tickerRunnable)
        postDelayed(tickerRunnable, 4000L)
    }

    /**
     * Stops the ticker rotation to prevent memory leaks.
     */
    fun stopTicker() {
        removeCallbacks(tickerRunnable)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopTicker()
    }
}
