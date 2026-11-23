package com.jhaiian.kate.playback

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import com.jhaiian.kate.data.AudioSettings
import java.nio.ByteBuffer
import kotlin.math.abs

@OptIn(UnstableApi::class)
internal class FloatSilenceSkipper : BaseAudioProcessor() {
    private companion object {
        const val THRESHOLD = 1024.0 / 32768.0
        const val MIN_SILENCE_SECONDS = 0.1
        const val KEEP_SILENCE_SECONDS = 0.02
    }

    private var channelCount = 0
    private var minFrames = 0
    private var keepFrames = 0
    private var held = FloatArray(0)
    private var heldFrames = 0
    private var dropping = false

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        if (AudioSettings.pipelineParams.bitPerfect) return AudioProcessor.AudioFormat.NOT_SET
        if (inputAudioFormat.encoding != C.ENCODING_PCM_FLOAT || inputAudioFormat.channelCount <= 0 || inputAudioFormat.sampleRate <= 0) {
            return AudioProcessor.AudioFormat.NOT_SET
        }
        return inputAudioFormat
    }

    override fun onFlush(streamMetadata: AudioProcessor.StreamMetadata) {
        val rate = inputAudioFormat.sampleRate
        channelCount = inputAudioFormat.channelCount
        minFrames = (rate * MIN_SILENCE_SECONDS).toInt().coerceAtLeast(1)
        keepFrames = (rate * KEEP_SILENCE_SECONDS).toInt().coerceIn(0, minFrames)
        held = FloatArray(minFrames * channelCount.coerceAtLeast(1))
        heldFrames = 0
        dropping = false
    }

    override fun onReset() {
        channelCount = 0
        held = FloatArray(0)
        heldFrames = 0
        dropping = false
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val size = inputBuffer.remaining()
        if (size == 0) return
        val frameBytes = channelCount * 4
        if (frameBytes <= 0) {
            inputBuffer.position(inputBuffer.limit())
            return
        }
        if (!AudioSettings.skipSilence) {
            val output = replaceOutputBuffer(size + heldFrames * frameBytes)
            emitHeld(output, heldFrames)
            heldFrames = 0
            dropping = false
            output.put(inputBuffer)
            output.flip()
            return
        }
        val frames = size / frameBytes
        val output = replaceOutputBuffer((frames + minFrames) * frameBytes)
        val frame = FloatArray(channelCount)
        repeat(frames) {
            var peak = 0.0
            for (channel in 0 until channelCount) {
                val sample = inputBuffer.getFloat()
                frame[channel] = sample
                val level = abs(sample.toDouble())
                if (level > peak) peak = level
            }
            if (peak < THRESHOLD) {
                when {
                    dropping -> Unit
                    heldFrames < minFrames -> {
                        System.arraycopy(frame, 0, held, heldFrames * channelCount, channelCount)
                        heldFrames += 1
                    }
                    else -> {
                        emitHeld(output, keepFrames)
                        heldFrames = 0
                        dropping = true
                    }
                }
            } else {
                if (!dropping) emitHeld(output, heldFrames)
                heldFrames = 0
                dropping = false
                for (channel in 0 until channelCount) output.putFloat(frame[channel])
            }
        }
        inputBuffer.position(inputBuffer.limit())
        output.flip()
    }

    private fun emitHeld(output: ByteBuffer, count: Int) {
        val frames = count.coerceAtMost(heldFrames)
        for (index in 0 until frames * channelCount) output.putFloat(held[index])
    }
}
