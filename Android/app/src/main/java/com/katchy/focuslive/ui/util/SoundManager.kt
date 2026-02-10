package com.katchy.focuslive.ui.util



import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.SoundPool
import android.net.Uri
import com.katchy.focuslive.R

object SoundManager {
    private var focusPlayer: MediaPlayer? = null

    private var soundPool: SoundPool? = null
    private var clickSoundId: Int = 0

    fun init(context: Context) {
        val appContext = context.applicationContext
        runCatching {
            if (soundPool == null) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                soundPool = SoundPool.Builder()
                    .setMaxStreams(3)
                    .setAudioAttributes(audioAttributes)
                    .build()

                // Load sounds safely
                clickSoundId = soundPool?.load(appContext, R.raw.click, 1) ?: 0
            }
        }.onFailure { it.printStackTrace() }
    }

    fun playClickSound(context: Context) {
        runCatching {
            val audioManager = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK)
        }.onFailure { it.printStackTrace() }
    }

    fun playFocusSound(context: Context, resId: Int) {
        stopFocusSound()
        if (resId == 0) return

        runCatching {
            focusPlayer = MediaPlayer.create(context.applicationContext, resId)?.apply {
                isLooping = true
                try { setVolume(0.6f, 0.6f) } catch(e: Exception) {}
                start()
            }
        }.onFailure { 
            it.printStackTrace()
            focusPlayer = null 
        }
    }

    fun stopFocusSound() {
        runCatching {
            focusPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        }.onFailure { it.printStackTrace() }
        focusPlayer = null
    }

    fun release() {
        runCatching {
            soundPool?.release()
            soundPool = null
        }.onFailure { it.printStackTrace() }
    }
    
    fun playSuccessSound(context: Context) {
        runCatching {
            if (soundPool != null && clickSoundId != 0) {
                soundPool?.play(clickSoundId, 0.8f, 0.8f, 1, 0, 1.5f)
            } else {
                playClickSound(context)
            }
        }.onFailure {
             playClickSound(context)
        }
    }

    fun playTickSound(context: Context) {
        runCatching {
            if (soundPool != null && clickSoundId != 0) {
                // Lower pitch for a "tock" effect, decent volume
                soundPool?.play(clickSoundId, 1.0f, 1.0f, 1, 0, 0.8f) // 0.8f rate for "lento"/deeper sound
            } else {
                playClickSound(context)
            }
        }.onFailure {
            playClickSound(context)
        }
    }

    fun playCompletionSound(context: Context) {
        runCatching {
             val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
             val r = RingtoneManager.getRingtone(context.applicationContext, notification)
             r.play()
        }.onFailure {
            playClickSound(context)
        }
    }
}
