package com.jhaiian.kate.data

import android.content.Context
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.roundToInt

class ReplayGainParams(
    val mode: String,
    val preampDb: Float,
    val fallbackDb: Float,
    val preventClipping: Boolean
)

class ChannelParams(
    val mono: Boolean,
    val balance: Float,
    val width: Float
)

data class PipelineParams(
    val hiRes: Boolean,
    val resample: Boolean,
    val bitPerfect: Boolean,
    val native: Boolean
)

class DspParams(
    val limiter: Boolean,
    val bassDb: Float,
    val smoothSeeking: Boolean,
    val trebleDb: Float
)

object AudioSettings {
    const val MAX_CROSSFADE_SECONDS = 12
    const val REPLAY_GAIN_OFF = "off"
    const val REPLAY_GAIN_TRACK = "track"
    const val REPLAY_GAIN_ALBUM = "album"
    const val MIN_RG_PREAMP_DB = -12f
    const val MAX_RG_PREAMP_DB = 12f
    const val MIN_RG_FALLBACK_DB = -12f
    const val MAX_RG_FALLBACK_DB = 6f
    val ReplayGainModes = listOf(REPLAY_GAIN_OFF, REPLAY_GAIN_TRACK, REPLAY_GAIN_ALBUM)
    const val CURVE_SMOOTH = "smooth"
    const val CURVE_LINEAR = "linear"
    const val CURVE_S = "s_curve"
    val CrossfadeCurves = listOf(CURVE_SMOOTH, CURVE_LINEAR, CURVE_S)
    const val FOCUS_PAUSE = "pause"
    const val FOCUS_DUCK = "duck"
    const val FOCUS_IGNORE = "ignore"
    val FocusModes = listOf(FOCUS_PAUSE, FOCUS_DUCK, FOCUS_IGNORE)
    const val MAX_PLAY_PAUSE_FADE_MS = 1_000
    const val PLAY_PAUSE_FADE_STEP_MS = 100
    const val MIN_DUCK_PERCENT = 5
    const val MAX_DUCK_PERCENT = 50
    const val DUCK_PERCENT_STEP = 5
    const val MAX_BASS_DB = 12
    const val MAX_TREBLE_DB = 12
    const val MAX_STEREO_WIDTH = 200
    const val STEREO_WIDTH_STEP = 10
    const val NORMAL_STEREO_WIDTH = 100
    const val MIN_VOLUME_LIMIT = 10
    const val MAX_VOLUME_LIMIT = 100
    const val VOLUME_LIMIT_STEP = 5
    const val MAX_REWIND_SECONDS = 30
    const val REWIND_STEP_SECONDS = 5
    val LongTrackMinutes = listOf(0, 10, 20, 45)
    const val MIN_SPEED = 0.5f
    const val MAX_SPEED = 2f
    const val SPEED_STEP = 0.05f
    const val MAX_START_VOLUME = 100
    const val START_VOLUME_STEP = 5
    val VolumeSubSteps = listOf(1, 2, 3, 4)
    const val MAX_SLEEP_FADE_SECONDS = 60
    const val SLEEP_FADE_STEP_SECONDS = 5
    const val DEFAULT_SLEEP_FADE_SECONDS = 30

    private const val PREFS_NAME = "audio_settings"
    private const val KEY_CROSSFADE_SECONDS = "crossfade_seconds"
    private const val KEY_RG_MODE = "replay_gain_mode"
    private const val KEY_RG_PREAMP = "replay_gain_preamp"
    private const val KEY_RG_FALLBACK = "replay_gain_fallback"
    private const val KEY_RG_PREVENT_CLIPPING = "replay_gain_prevent_clipping"
    private const val KEY_GAPLESS_ALBUMS = "gapless_albums"
    private const val KEY_SKIP_SILENCE = "skip_silence"
    private const val KEY_MONO = "mono_audio"
    private const val KEY_BALANCE = "channel_balance"
    private const val KEY_PLAY_PAUSE_FADE = "play_pause_fade_ms"
    private const val KEY_CROSSFADE_CURVE = "crossfade_curve"
    private const val KEY_CROSSFADE_ON_SKIP = "crossfade_on_skip"
    private const val KEY_CROSSFADE_ANIMATION = "crossfade_animation"
    private const val KEY_FOCUS_MODE = "focus_mode"
    private const val KEY_DUCK_PERCENT = "duck_percent"
    private const val KEY_RESUME_AFTER_INTERRUPTION = "resume_after_interruption"
    private const val KEY_PAUSE_ON_DISCONNECT = "pause_on_disconnect"
    private const val KEY_AUTOPLAY_WIRED = "autoplay_wired"
    private const val KEY_AUTOPLAY_BLUETOOTH = "autoplay_bluetooth"
    private const val KEY_AUDIO_OFFLOAD = "audio_offload"
    private const val KEY_LIMITER = "limiter"
    private const val KEY_BASS_DB = "bass_boost_db"
    private const val KEY_TREBLE_DB = "treble_boost_db"
    private const val KEY_STEREO_WIDTH = "stereo_width"
    private const val KEY_VOLUME_LIMIT = "volume_limit"
    private const val KEY_SMOOTH_SEEKING = "smooth_seeking"
    private const val KEY_REWIND_SECONDS = "rewind_seconds"
    private const val KEY_LONG_TRACK_MINUTES = "long_track_minutes"
    private const val KEY_SPEED = "playback_speed"
    private const val KEY_KEEP_PITCH = "keep_pitch"
    private const val KEY_START_VOLUME = "start_volume"
    private const val KEY_PAUSE_WHEN_MUTED = "pause_when_muted"
    private const val KEY_VOLUME_SUB_STEPS = "volume_sub_steps"
    private const val KEY_BT_ONLY_CHOSEN = "bluetooth_only_chosen"
    private const val KEY_BT_KNOWN = "bluetooth_known"
    private const val KEY_BT_CHOSEN = "bluetooth_chosen"
    private const val KEY_HI_RES = "hi_res_output"
    private const val KEY_RESAMPLE = "resample_to_device"
    private const val KEY_BIT_PERFECT = "bit_perfect_usb"
    private const val KEY_NATIVE_OUTPUT = "native_output"
    private const val KEY_SLEEP_FADE = "sleep_fade_seconds"
    private const val MAX_KNOWN_DEVICES = 30

    var crossfadeSeconds by mutableIntStateOf(0)
        private set
    var replayGainMode by mutableStateOf(REPLAY_GAIN_OFF)
        private set
    var replayGainPreampDb by mutableFloatStateOf(0f)
        private set
    var replayGainFallbackDb by mutableFloatStateOf(0f)
        private set
    var replayGainPreventClipping by mutableStateOf(true)
        private set
    var gaplessAlbums by mutableStateOf(true)
        private set
    var skipSilence by mutableStateOf(false)
        private set
    var monoAudio by mutableStateOf(false)
        private set
    var balance by mutableFloatStateOf(0f)
        private set
    var playPauseFadeMs by mutableIntStateOf(0)
        private set
    var crossfadeCurve by mutableStateOf(CURVE_SMOOTH)
        private set
    var crossfadeOnSkip by mutableStateOf(false)
        private set
    var crossfadeAnimation by mutableStateOf(true)
        private set
    var focusMode by mutableStateOf(FOCUS_PAUSE)
        private set
    var duckPercent by mutableIntStateOf(20)
        private set
    var resumeAfterInterruption by mutableStateOf(true)
        private set
    var pauseOnDisconnect by mutableStateOf(true)
        private set
    var autoPlayWired by mutableStateOf(false)
        private set
    var autoPlayBluetooth by mutableStateOf(false)
        private set
    var audioOffload by mutableStateOf(false)
        private set
    var limiter by mutableStateOf(true)
        private set
    var bassBoostDb by mutableIntStateOf(0)
        private set
    var trebleBoostDb by mutableIntStateOf(0)
        private set
    var stereoWidth by mutableIntStateOf(NORMAL_STEREO_WIDTH)
        private set
    var volumeLimit by mutableIntStateOf(MAX_VOLUME_LIMIT)
        private set
    var smoothSeeking by mutableStateOf(true)
        private set
    var rewindSeconds by mutableIntStateOf(0)
        private set
    var longTrackMinutes by mutableIntStateOf(0)
        private set
    var playbackSpeed by mutableFloatStateOf(1f)
        private set
    var keepPitch by mutableStateOf(true)
        private set
    var startVolume by mutableIntStateOf(0)
        private set
    var pauseWhenMuted by mutableStateOf(false)
        private set
    var volumeSubSteps by mutableIntStateOf(1)
        private set
    var bluetoothOnlyChosen by mutableStateOf(false)
        private set
    var knownBluetoothDevices by mutableStateOf<List<String>>(emptyList())
        private set
    var chosenBluetoothDevices by mutableStateOf<Set<String>>(emptySet())
        private set

    @Volatile
    var fineVolumeGain = 1f
        private set
    var hiResOutput by mutableStateOf(false)
        private set
    var resampleToDevice by mutableStateOf(false)
        private set
    var bitPerfect by mutableStateOf(false)
        private set
    var bitPerfectEngaged by mutableStateOf(false)
        private set
    var nativeOutput by mutableStateOf(false)
        private set
    var sleepFadeSeconds by mutableIntStateOf(DEFAULT_SLEEP_FADE_SECONDS)
        private set

    @Volatile
    var pipelineParams = PipelineParams(false, false, false, false)
        private set

    val bitPerfectSupported: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

    @Volatile
    var dspParams = DspParams(true, 0f, true, 0f)
        private set

    @Volatile
    var channelParams = ChannelParams(false, 0f, 1f)
        private set

    private val listeners = CopyOnWriteArrayList<() -> Unit>()

    @Volatile
    var replayGainParams = ReplayGainParams(REPLAY_GAIN_OFF, 0f, 0f, true)
        private set

    private var loaded = false

    val crossfadeMs: Long get() = crossfadeSeconds * 1_000L

    val offloadSupported: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    val effectsBlockOffload: Boolean
        get() = crossfadeSeconds > 0 ||
            AudioEffects.blocksOffload ||
            skipSilence ||
            monoAudio ||
            balance != 0f ||
            resampleToDevice ||
            stereoWidth != NORMAL_STEREO_WIDTH ||
            replayGainMode != REPLAY_GAIN_OFF ||
            (EqualizerSettings.enabled && (EqualizerSettings.preampDb != 0f || EqualizerSettings.gains.any { it != 0f } || bassBoostDb != 0 || trebleBoostDb != 0))

    val offloadActive: Boolean get() = offloadSupported && audioOffload && !effectsBlockOffload && !bitPerfectEngaged

    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        val prefs = prefs(context)
        crossfadeSeconds = prefs.getInt(KEY_CROSSFADE_SECONDS, 0).coerceIn(0, MAX_CROSSFADE_SECONDS)
        replayGainMode = prefs.getString(KEY_RG_MODE, REPLAY_GAIN_OFF).takeIf { it in ReplayGainModes } ?: REPLAY_GAIN_OFF
        replayGainPreampDb = prefs.getFloat(KEY_RG_PREAMP, 0f).coerceIn(MIN_RG_PREAMP_DB, MAX_RG_PREAMP_DB)
        replayGainFallbackDb = prefs.getFloat(KEY_RG_FALLBACK, 0f).coerceIn(MIN_RG_FALLBACK_DB, MAX_RG_FALLBACK_DB)
        replayGainPreventClipping = prefs.getBoolean(KEY_RG_PREVENT_CLIPPING, true)
        gaplessAlbums = prefs.getBoolean(KEY_GAPLESS_ALBUMS, true)
        skipSilence = prefs.getBoolean(KEY_SKIP_SILENCE, false)
        monoAudio = prefs.getBoolean(KEY_MONO, false)
        balance = prefs.getFloat(KEY_BALANCE, 0f).coerceIn(-1f, 1f)
        playPauseFadeMs = prefs.getInt(KEY_PLAY_PAUSE_FADE, 0).coerceIn(0, MAX_PLAY_PAUSE_FADE_MS)
        crossfadeCurve = prefs.getString(KEY_CROSSFADE_CURVE, CURVE_SMOOTH).takeIf { it in CrossfadeCurves } ?: CURVE_SMOOTH
        crossfadeOnSkip = prefs.getBoolean(KEY_CROSSFADE_ON_SKIP, false)
        crossfadeAnimation = prefs.getBoolean(KEY_CROSSFADE_ANIMATION, true)
        focusMode = prefs.getString(KEY_FOCUS_MODE, FOCUS_PAUSE).takeIf { it in FocusModes } ?: FOCUS_PAUSE
        duckPercent = prefs.getInt(KEY_DUCK_PERCENT, 20).coerceIn(MIN_DUCK_PERCENT, MAX_DUCK_PERCENT)
        resumeAfterInterruption = prefs.getBoolean(KEY_RESUME_AFTER_INTERRUPTION, true)
        pauseOnDisconnect = prefs.getBoolean(KEY_PAUSE_ON_DISCONNECT, true)
        autoPlayWired = prefs.getBoolean(KEY_AUTOPLAY_WIRED, false)
        autoPlayBluetooth = prefs.getBoolean(KEY_AUTOPLAY_BLUETOOTH, false)
        audioOffload = prefs.getBoolean(KEY_AUDIO_OFFLOAD, false)
        limiter = prefs.getBoolean(KEY_LIMITER, true)
        bassBoostDb = prefs.getInt(KEY_BASS_DB, 0).coerceIn(0, MAX_BASS_DB)
        trebleBoostDb = prefs.getInt(KEY_TREBLE_DB, 0).coerceIn(0, MAX_TREBLE_DB)
        stereoWidth = prefs.getInt(KEY_STEREO_WIDTH, NORMAL_STEREO_WIDTH).coerceIn(0, MAX_STEREO_WIDTH)
        volumeLimit = prefs.getInt(KEY_VOLUME_LIMIT, MAX_VOLUME_LIMIT).coerceIn(MIN_VOLUME_LIMIT, MAX_VOLUME_LIMIT)
        smoothSeeking = prefs.getBoolean(KEY_SMOOTH_SEEKING, true)
        rewindSeconds = prefs.getInt(KEY_REWIND_SECONDS, 0).coerceIn(0, MAX_REWIND_SECONDS)
        longTrackMinutes = prefs.getInt(KEY_LONG_TRACK_MINUTES, 0).takeIf { it in LongTrackMinutes } ?: 0
        playbackSpeed = snapSpeed(prefs.getFloat(KEY_SPEED, 1f))
        keepPitch = prefs.getBoolean(KEY_KEEP_PITCH, true)
        startVolume = prefs.getInt(KEY_START_VOLUME, 0).coerceIn(0, MAX_START_VOLUME)
        pauseWhenMuted = prefs.getBoolean(KEY_PAUSE_WHEN_MUTED, false)
        volumeSubSteps = prefs.getInt(KEY_VOLUME_SUB_STEPS, 1).takeIf { it in VolumeSubSteps } ?: 1
        bluetoothOnlyChosen = prefs.getBoolean(KEY_BT_ONLY_CHOSEN, false)
        knownBluetoothDevices = prefs.getString(KEY_BT_KNOWN, null).orEmpty().split('\n').filter { it.isNotBlank() }
        chosenBluetoothDevices = prefs.getString(KEY_BT_CHOSEN, null).orEmpty().split('\n').filter { it.isNotBlank() }.toSet()
        hiResOutput = prefs.getBoolean(KEY_HI_RES, false)
        resampleToDevice = prefs.getBoolean(KEY_RESAMPLE, false)
        bitPerfect = prefs.getBoolean(KEY_BIT_PERFECT, false) && bitPerfectSupported
        nativeOutput = prefs.getBoolean(KEY_NATIVE_OUTPUT, false)
        sleepFadeSeconds = prefs.getInt(KEY_SLEEP_FADE, DEFAULT_SLEEP_FADE_SECONDS).coerceIn(0, MAX_SLEEP_FADE_SECONDS)
        publishPipeline()
        publishReplayGain()
        publishChannels()
        publishDsp()
    }

    fun addListener(listener: () -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: () -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyChanged() {
        listeners.forEach { it() }
    }

    fun setSkipSilence(context: Context, value: Boolean) {
        ensureLoaded(context)
        if (value == skipSilence) return
        skipSilence = value
        prefs(context).edit().putBoolean(KEY_SKIP_SILENCE, value).apply()
        notifyChanged()
    }

    fun setMonoAudio(context: Context, value: Boolean) {
        ensureLoaded(context)
        if (value == monoAudio) return
        monoAudio = value
        publishChannels()
        prefs(context).edit().putBoolean(KEY_MONO, value).apply()
        notifyChanged()
    }

    fun setBalance(context: Context, value: Float) {
        ensureLoaded(context)
        val clamped = value.coerceIn(-1f, 1f)
        if (clamped == balance) return
        balance = clamped
        publishChannels()
        prefs(context).edit().putFloat(KEY_BALANCE, clamped).apply()
        notifyChanged()
    }

    fun setPlayPauseFadeMs(context: Context, value: Int) {
        ensureLoaded(context)
        val clamped = value.coerceIn(0, MAX_PLAY_PAUSE_FADE_MS)
        if (clamped == playPauseFadeMs) return
        playPauseFadeMs = clamped
        prefs(context).edit().putInt(KEY_PLAY_PAUSE_FADE, clamped).apply()
        notifyChanged()
    }

    fun setCrossfadeCurve(context: Context, value: String) {
        ensureLoaded(context)
        if (value !in CrossfadeCurves || value == crossfadeCurve) return
        crossfadeCurve = value
        prefs(context).edit().putString(KEY_CROSSFADE_CURVE, value).apply()
        notifyChanged()
    }

    fun setCrossfadeOnSkip(context: Context, value: Boolean) {
        ensureLoaded(context)
        if (value == crossfadeOnSkip) return
        crossfadeOnSkip = value
        prefs(context).edit().putBoolean(KEY_CROSSFADE_ON_SKIP, value).apply()
        notifyChanged()
    }

    fun setCrossfadeAnimation(context: Context, value: Boolean) {
        ensureLoaded(context)
        if (value == crossfadeAnimation) return
        crossfadeAnimation = value
        prefs(context).edit().putBoolean(KEY_CROSSFADE_ANIMATION, value).apply()
    }

    fun setFocusMode(context: Context, value: String) {
        ensureLoaded(context)
        if (value !in FocusModes || value == focusMode) return
        focusMode = value
        prefs(context).edit().putString(KEY_FOCUS_MODE, value).apply()
        notifyChanged()
    }

    fun setDuckPercent(context: Context, value: Int) {
        ensureLoaded(context)
        val clamped = value.coerceIn(MIN_DUCK_PERCENT, MAX_DUCK_PERCENT)
        if (clamped == duckPercent) return
        duckPercent = clamped
        prefs(context).edit().putInt(KEY_DUCK_PERCENT, clamped).apply()
        notifyChanged()
    }

    fun setResumeAfterInterruption(context: Context, value: Boolean) {
        ensureLoaded(context)
        if (value == resumeAfterInterruption) return
        resumeAfterInterruption = value
        prefs(context).edit().putBoolean(KEY_RESUME_AFTER_INTERRUPTION, value).apply()
        notifyChanged()
    }

    fun setPauseOnDisconnect(context: Context, value: Boolean) {
        ensureLoaded(context)
        if (value == pauseOnDisconnect) return
        pauseOnDisconnect = value
        prefs(context).edit().putBoolean(KEY_PAUSE_ON_DISCONNECT, value).apply()
        notifyChanged()
    }

    fun setAutoPlayWired(context: Context, value: Boolean) {
        ensureLoaded(context)
        if (value == autoPlayWired) return
        autoPlayWired = value
        prefs(context).edit().putBoolean(KEY_AUTOPLAY_WIRED, value).apply()
        notifyChanged()
    }

    fun setAutoPlayBluetooth(context: Context, value: Boolean) {
        ensureLoaded(context)
        if (value == autoPlayBluetooth) return
        autoPlayBluetooth = value
        prefs(context).edit().putBoolean(KEY_AUTOPLAY_BLUETOOTH, value).apply()
        notifyChanged()
    }

    fun setLimiter(context: Context, value: Boolean) {
        ensureLoaded(context)
        if (value == limiter) return
        limiter = value
        publishDsp()
        prefs(context).edit().putBoolean(KEY_LIMITER, value).apply()
        notifyChanged()
    }

    fun setBassBoostDb(context: Context, value: Int) {
        ensureLoaded(context)
        val clamped = value.coerceIn(0, MAX_BASS_DB)
        if (clamped == bassBoostDb) return
        bassBoostDb = clamped
        publishDsp()
        prefs(context).edit().putInt(KEY_BASS_DB, clamped).apply()
        notifyChanged()
    }

    fun setTrebleBoostDb(context: Context, value: Int) {
        ensureLoaded(context)
        val clamped = value.coerceIn(0, MAX_TREBLE_DB)
        if (clamped == trebleBoostDb) return
        trebleBoostDb = clamped
        publishDsp()
        prefs(context).edit().putInt(KEY_TREBLE_DB, clamped).apply()
        notifyChanged()
    }

    fun setStereoWidth(context: Context, value: Int) {
        ensureLoaded(context)
        val clamped = value.coerceIn(0, MAX_STEREO_WIDTH)
        if (clamped == stereoWidth) return
        stereoWidth = clamped
        publishChannels()
        prefs(context).edit().putInt(KEY_STEREO_WIDTH, clamped).apply()
        notifyChanged()
    }

    fun setVolumeLimit(context: Context, value: Int) {
        ensureLoaded(context)
        val clamped = value.coerceIn(MIN_VOLUME_LIMIT, MAX_VOLUME_LIMIT)
        if (clamped == volumeLimit) return
        volumeLimit = clamped
        prefs(context).edit().putInt(KEY_VOLUME_LIMIT, clamped).apply()
        notifyChanged()
    }

    fun setSmoothSeeking(context: Context, value: Boolean) {
        ensureLoaded(context)
        if (value == smoothSeeking) return
        smoothSeeking = value
        publishDsp()
        prefs(context).edit().putBoolean(KEY_SMOOTH_SEEKING, value).apply()
        notifyChanged()
    }

    fun setRewindSeconds(context: Context, value: Int) {
        ensureLoaded(context)
        val clamped = value.coerceIn(0, MAX_REWIND_SECONDS)
        if (clamped == rewindSeconds) return
        rewindSeconds = clamped
        prefs(context).edit().putInt(KEY_REWIND_SECONDS, clamped).apply()
        notifyChanged()
    }

    fun setLongTrackMinutes(context: Context, value: Int) {
        ensureLoaded(context)
        if (value !in LongTrackMinutes || value == longTrackMinutes) return
        longTrackMinutes = value
        prefs(context).edit().putInt(KEY_LONG_TRACK_MINUTES, value).apply()
        notifyChanged()
    }

    fun setPlaybackSpeed(context: Context, value: Float) {
        ensureLoaded(context)
        val snapped = snapSpeed(value)
        if (snapped == playbackSpeed) return
        playbackSpeed = snapped
        prefs(context).edit().putFloat(KEY_SPEED, snapped).apply()
        notifyChanged()
    }

    fun setKeepPitch(context: Context, value: Boolean) {
        ensureLoaded(context)
        if (value == keepPitch) return
        keepPitch = value
        prefs(context).edit().putBoolean(KEY_KEEP_PITCH, value).apply()
        notifyChanged()
    }

    fun setStartVolume(context: Context, value: Int) {
        ensureLoaded(context)
        val clamped = value.coerceIn(0, MAX_START_VOLUME)
        if (clamped == startVolume) return
        startVolume = clamped
        prefs(context).edit().putInt(KEY_START_VOLUME, clamped).apply()
        notifyChanged()
    }

    fun setPauseWhenMuted(context: Context, value: Boolean) {
        ensureLoaded(context)
        if (value == pauseWhenMuted) return
        pauseWhenMuted = value
        prefs(context).edit().putBoolean(KEY_PAUSE_WHEN_MUTED, value).apply()
        notifyChanged()
    }

    fun setVolumeSubSteps(context: Context, value: Int) {
        ensureLoaded(context)
        if (value !in VolumeSubSteps || value == volumeSubSteps) return
        volumeSubSteps = value
        prefs(context).edit().putInt(KEY_VOLUME_SUB_STEPS, value).apply()
        if (value == 1) setFineVolumeGain(1f) else notifyChanged()
    }

    fun setFineVolumeGain(value: Float) {
        val clamped = value.coerceIn(0f, 1f)
        if (clamped == fineVolumeGain) return
        fineVolumeGain = clamped
        notifyChanged()
    }

    fun setBluetoothOnlyChosen(context: Context, value: Boolean) {
        ensureLoaded(context)
        if (value == bluetoothOnlyChosen) return
        bluetoothOnlyChosen = value
        prefs(context).edit().putBoolean(KEY_BT_ONLY_CHOSEN, value).apply()
        notifyChanged()
    }

    fun setBluetoothChosen(context: Context, name: String, chosen: Boolean) {
        ensureLoaded(context)
        val updated = if (chosen) chosenBluetoothDevices + name else chosenBluetoothDevices - name
        if (updated == chosenBluetoothDevices) return
        chosenBluetoothDevices = updated
        prefs(context).edit().putString(KEY_BT_CHOSEN, updated.joinToString("\n")).apply()
        notifyChanged()
    }

    fun rememberBluetoothDevice(context: Context, name: String) {
        ensureLoaded(context)
        val clean = name.trim().replace('\n', ' ')
        if (clean.isEmpty()) return
        val updated = (listOf(clean) + knownBluetoothDevices.filterNot { it == clean }).take(MAX_KNOWN_DEVICES)
        if (updated == knownBluetoothDevices) return
        knownBluetoothDevices = updated
        prefs(context).edit().putString(KEY_BT_KNOWN, updated.joinToString("\n")).apply()
    }

    fun forgetBluetoothDevice(context: Context, name: String) {
        ensureLoaded(context)
        if (name !in knownBluetoothDevices && name !in chosenBluetoothDevices) return
        knownBluetoothDevices = knownBluetoothDevices - name
        chosenBluetoothDevices = chosenBluetoothDevices - name
        prefs(context).edit()
            .putString(KEY_BT_KNOWN, knownBluetoothDevices.joinToString("\n"))
            .putString(KEY_BT_CHOSEN, chosenBluetoothDevices.joinToString("\n"))
            .apply()
        notifyChanged()
    }

    fun allowsBluetoothAutoPlay(name: String?): Boolean {
        if (!bluetoothOnlyChosen) return true
        val clean = name?.trim().orEmpty()
        return clean.isNotEmpty() && clean in chosenBluetoothDevices
    }

    private fun snapSpeed(value: Float): Float {
        val steps = ((value.coerceIn(MIN_SPEED, MAX_SPEED) - MIN_SPEED) / SPEED_STEP).roundToInt()
        return (MIN_SPEED + steps * SPEED_STEP).coerceIn(MIN_SPEED, MAX_SPEED)
    }

    fun setHiResOutput(context: Context, value: Boolean) {
        ensureLoaded(context)
        if (value == hiResOutput) return
        hiResOutput = value
        publishPipeline()
        prefs(context).edit().putBoolean(KEY_HI_RES, value).apply()
        notifyChanged()
    }

    fun setResampleToDevice(context: Context, value: Boolean) {
        ensureLoaded(context)
        if (value == resampleToDevice) return
        resampleToDevice = value
        publishPipeline()
        prefs(context).edit().putBoolean(KEY_RESAMPLE, value).apply()
        notifyChanged()
    }

    fun setSleepFadeSeconds(context: Context, value: Int) {
        ensureLoaded(context)
        val clamped = value.coerceIn(0, MAX_SLEEP_FADE_SECONDS)
        if (clamped == sleepFadeSeconds) return
        sleepFadeSeconds = clamped
        prefs(context).edit().putInt(KEY_SLEEP_FADE, clamped).apply()
        notifyChanged()
    }

    fun setNativeOutput(context: Context, value: Boolean) {
        ensureLoaded(context)
        if (value == nativeOutput) return
        nativeOutput = value
        publishPipeline()
        prefs(context).edit().putBoolean(KEY_NATIVE_OUTPUT, value).apply()
        notifyChanged()
    }

    fun setBitPerfect(context: Context, value: Boolean) {
        ensureLoaded(context)
        val allowed = value && bitPerfectSupported
        if (allowed == bitPerfect) return
        bitPerfect = allowed
        prefs(context).edit().putBoolean(KEY_BIT_PERFECT, allowed).apply()
        notifyChanged()
    }

    fun updateBitPerfectEngaged(value: Boolean) {
        if (value == bitPerfectEngaged) return
        bitPerfectEngaged = value
        publishPipeline()
        notifyChanged()
    }

    fun resetBoost(context: Context) {
        setBassBoostDb(context, 0)
        setTrebleBoostDb(context, 0)
    }

    fun resetSound(context: Context) {
        setStereoWidth(context, NORMAL_STEREO_WIDTH)
        setMonoAudio(context, false)
        setBalance(context, 0f)
        EqualizerSettings.setPerDevice(context, false)
    }

    fun resetVolume(context: Context) {
        setReplayGainMode(context, REPLAY_GAIN_OFF)
        setReplayGainPreamp(context, 0f)
        setReplayGainFallback(context, 0f)
        setReplayGainPreventClipping(context, true)
        setLimiter(context, true)
        setVolumeLimit(context, MAX_VOLUME_LIMIT)
        setVolumeSubSteps(context, 1)
    }

    fun resetPlayback(context: Context) {
        setCrossfadeSeconds(context, 0)
        setCrossfadeCurve(context, CURVE_SMOOTH)
        setCrossfadeOnSkip(context, false)
        setCrossfadeAnimation(context, true)
        setGaplessAlbums(context, true)
        setSkipSilence(context, false)
        setSmoothSeeking(context, true)
        setPlayPauseFadeMs(context, 0)
        setRewindSeconds(context, 0)
        setLongTrackMinutes(context, 0)
        setPlaybackSpeed(context, 1f)
        setKeepPitch(context, true)
        setSleepFadeSeconds(context, DEFAULT_SLEEP_FADE_SECONDS)
    }

    fun resetHeadphones(context: Context) {
        setFocusMode(context, FOCUS_PAUSE)
        setDuckPercent(context, 20)
        setResumeAfterInterruption(context, true)
        setPauseWhenMuted(context, false)
        setPauseOnDisconnect(context, true)
        setAutoPlayWired(context, false)
        setAutoPlayBluetooth(context, false)
        setBluetoothOnlyChosen(context, false)
        setStartVolume(context, 0)
    }

    fun resetAdvanced(context: Context) {
        setAudioOffload(context, false)
        setHiResOutput(context, false)
        setResampleToDevice(context, false)
        setBitPerfect(context, false)
        setNativeOutput(context, false)
    }

    fun resetAll(context: Context) {
        resetSound(context)
        resetBoost(context)
        resetVolume(context)
        resetPlayback(context)
        resetHeadphones(context)
        resetAdvanced(context)
        EqualizerSettings.resetToDefaults(context)
    }

    private fun publishPipeline() {
        pipelineParams = PipelineParams(
            hiRes = hiResOutput,
            resample = resampleToDevice,
            bitPerfect = bitPerfectEngaged,
            native = nativeOutput
        )
    }

    fun setAudioOffload(context: Context, value: Boolean) {
        ensureLoaded(context)
        if (value == audioOffload) return
        audioOffload = value
        prefs(context).edit().putBoolean(KEY_AUDIO_OFFLOAD, value).apply()
        notifyChanged()
    }

    fun setCrossfadeSeconds(context: Context, seconds: Int) {
        ensureLoaded(context)
        val value = seconds.coerceIn(0, MAX_CROSSFADE_SECONDS)
        if (value == crossfadeSeconds) return
        crossfadeSeconds = value
        prefs(context).edit().putInt(KEY_CROSSFADE_SECONDS, value).apply()
        notifyChanged()
    }

    fun setReplayGainMode(context: Context, mode: String) {
        ensureLoaded(context)
        if (mode !in ReplayGainModes || mode == replayGainMode) return
        replayGainMode = mode
        publishReplayGain()
        prefs(context).edit().putString(KEY_RG_MODE, mode).apply()
        notifyChanged()
    }

    fun setReplayGainPreamp(context: Context, db: Float) {
        ensureLoaded(context)
        val value = db.coerceIn(MIN_RG_PREAMP_DB, MAX_RG_PREAMP_DB)
        if (value == replayGainPreampDb) return
        replayGainPreampDb = value
        publishReplayGain()
        prefs(context).edit().putFloat(KEY_RG_PREAMP, value).apply()
        notifyChanged()
    }

    fun setReplayGainFallback(context: Context, db: Float) {
        ensureLoaded(context)
        val value = db.coerceIn(MIN_RG_FALLBACK_DB, MAX_RG_FALLBACK_DB)
        if (value == replayGainFallbackDb) return
        replayGainFallbackDb = value
        publishReplayGain()
        prefs(context).edit().putFloat(KEY_RG_FALLBACK, value).apply()
        notifyChanged()
    }

    fun setReplayGainPreventClipping(context: Context, value: Boolean) {
        ensureLoaded(context)
        if (value == replayGainPreventClipping) return
        replayGainPreventClipping = value
        publishReplayGain()
        prefs(context).edit().putBoolean(KEY_RG_PREVENT_CLIPPING, value).apply()
        notifyChanged()
    }

    fun setGaplessAlbums(context: Context, value: Boolean) {
        ensureLoaded(context)
        if (value == gaplessAlbums) return
        gaplessAlbums = value
        prefs(context).edit().putBoolean(KEY_GAPLESS_ALBUMS, value).apply()
        notifyChanged()
    }

    private fun publishReplayGain() {
        replayGainParams = ReplayGainParams(
            mode = replayGainMode,
            preampDb = replayGainPreampDb,
            fallbackDb = replayGainFallbackDb,
            preventClipping = replayGainPreventClipping
        )
    }

    private fun publishChannels() {
        channelParams = ChannelParams(mono = monoAudio, balance = balance, width = stereoWidth / 100f)
    }

    private fun publishDsp() {
        dspParams = DspParams(
            limiter = limiter,
            bassDb = bassBoostDb.toFloat(),
            smoothSeeking = smoothSeeking,
            trebleDb = trebleBoostDb.toFloat()
        )
    }

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
