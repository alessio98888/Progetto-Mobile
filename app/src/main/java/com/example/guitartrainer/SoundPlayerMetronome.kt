/*
 * @(#) SoundPlayerMetronome.java     1.0 05/01/2022
 */
package com.example.guitartrainer

import android.content.Context
import android.media.SoundPool
import com.example.guitartrainer.SoundPlayerMetronome
import com.example.guitartrainer.ObserverData
import android.media.AudioAttributes
import android.os.Handler
import com.example.guitartrainer.R

/**
 *
 * Implements the actual metronome playing.
 *
 * @version
 * 1.00 05/01/2022
 * @author
 * Alessio Ardu
 */
class SoundPlayerMetronome(context: Context, bpm: Int) : Runnable, Observer {
    var bpm: Double
        private set
    val context: Context
    private val soundPool: SoundPool
    private val handler: Handler
    private var isPlaying = false
    private var interval: Long
    private var soundId = -1
    fun start(bpm: Int, initialDelay: Int) {
        setBpm(bpm)
        pause()
        play(initialDelay)
    }

    private fun play(initialDelay: Int) {

        // delayed because rapid incrementing/decrementing causes sound glitch
        handler.postDelayed(this, initialDelay.toLong())
        isPlaying = true
    }

    fun pause() {
        handler.removeCallbacks(this)
        isPlaying = false
    }

    fun setBpm(bpm: Int) {
        this.bpm = bpm.toDouble()
        interval = toInterval(bpm)
    }

    override fun run() {
        if (isPlaying) {
            handler.postDelayed(this, interval)
            if (soundId != -1) {
                soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
            }
        }
    }

    override fun update(o: Any) {
        if (isPlaying) {
            val obsData = o as ObserverData
            start(obsData.value, 100)
        }
    }

    companion object {
        const val MILLIS_IN_MINUTE = 60000
        private fun toBpm(interval: Long): Int {
            return (MILLIS_IN_MINUTE / interval).toInt()
        }

        private fun toInterval(bpm: Int): Long {
            return MILLIS_IN_MINUTE.toLong() / bpm
        }
    }

    init {
        this.bpm = bpm.toDouble()
        this.context = context
        soundPool = SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .build()
        handler = Handler()
        interval = toInterval(bpm)
        soundId = soundPool.load(context, R.raw.metronome_beat1, 1)
    }
}