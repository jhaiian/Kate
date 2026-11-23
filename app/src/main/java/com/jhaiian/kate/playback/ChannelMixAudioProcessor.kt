package com.jhaiian.kate.playback

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import com.jhaiian.kate.data.AudioSettings
import com.jhaiian.kate.data.ChannelParams
import java.nio.ByteBuffer
import kotlin.math.roundToInt

@OptIn(UnstableApi::class)
internal class ChannelMixAudioProcessor : BaseAudioProcessor() {
    private companion object {
        const val RAMP_SECONDS = 0.03
        const val SHORT_SCALE = 32_768.0
        const val SHORT_MAX = 32_767.0
    }

    private var sampleRate = 0
    private var encoding = C.ENCODING_INVALID
    private var applied: ChannelParams? = null
    private var mix = 0.0
    private var left = 1.0
    private var right = 1.0
    private var width = 1.0
    private var mixTarget = 0.0
    private var leftTarget = 1.0
    private var rightTarget = 1.0
    private var widthTarget = 1.0
    private var mixStep = 0.0
    private var leftStep = 0.0
    private var rightStep = 0.0
    private var widthStep = 0.0
    private var rampFrames = 0
    private var outL = 0.0
    private var outR = 0.0

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        if (AudioSettings.pipelineParams.bitPerfect) return AudioProcessor.AudioFormat.NOT_SET
        val supported = inputAudioFormat.encoding == C.ENCODING_PCM_16BIT || inputAudioFormat.encoding == C.ENCODING_PCM_FLOAT
        if (!supported || inputAudioFormat.channelCount != 2 || inputAudioFormat.sampleRate <= 0) {
            return AudioProcessor.AudioFormat.NOT_SET
        }
        return inputAudioFormat
    }

    override fun onFlush(streamMetadata: AudioProcessor.StreamMetadata) {
        sampleRate = inputAudioFormat.sampleRate
        encoding = inputAudioFormat.encoding
        val params = AudioSettings.channelParams
        applied = params
        setTargets(params)
        mix = mixTarget
        left = leftTarget
        right = rightTarget
        width = widthTarget
        rampFrames = 0
    }

    override fun onReset() {
        sampleRate = 0
        encoding = C.ENCODING_INVALID
        applied = null
        mix = 0.0
        left = 1.0
        right = 1.0
        width = 1.0
        rampFrames = 0
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val size = inputBuffer.remaining()
        if (size == 0) return
        val params = AudioSettings.channelParams
        if (params !== applied) {
            applied = params
            retarget(params)
        }
        val output = replaceOutputBuffer(size)
        if (rampFrames == 0 && mix == 0.0 && left == 1.0 && right == 1.0 && width == 1.0) {
            output.put(inputBuffer)
            output.flip()
            return
        }
        if (encoding == C.ENCODING_PCM_16BIT) {
            while (inputBuffer.remaining() >= 4) {
                advance()
                mixFrame(inputBuffer.getShort() / SHORT_SCALE, inputBuffer.getShort() / SHORT_SCALE)
                output.putShort((outL.coerceIn(-1.0, 1.0) * SHORT_MAX).roundToInt().toShort())
                output.putShort((outR.coerceIn(-1.0, 1.0) * SHORT_MAX).roundToInt().toShort())
            }
        } else {
            while (inputBuffer.remaining() >= 8) {
                advance()
                mixFrame(inputBuffer.getFloat().toDouble(), inputBuffer.getFloat().toDouble())
                output.putFloat(outL.toFloat())
                output.putFloat(outR.toFloat())
            }
        }
        while (inputBuffer.hasRemaining()) output.put(inputBuffer.get())
        output.flip()
    }

    private fun mixFrame(l: Double, r: Double) {
        val mid = (l + r) * 0.5
        val side = (l - r) * 0.5 * width
        val wideL = mid + side
        val wideR = mid - side
        outL = (wideL + (mid - wideL) * mix) * left
        outR = (wideR + (mid - wideR) * mix) * right
    }

    private fun setTargets(params: ChannelParams) {
        val balance = params.balance.toDouble().coerceIn(-1.0, 1.0)
        mixTarget = if (params.mono) 1.0 else 0.0
        leftTarget = if (balance > 0.0) 1.0 - balance else 1.0
        rightTarget = if (balance < 0.0) 1.0 + balance else 1.0
        widthTarget = params.width.toDouble().coerceIn(0.0, 2.0)
    }

    private fun retarget(params: ChannelParams) {
        setTargets(params)
        val frames = (sampleRate * RAMP_SECONDS).toInt()
        if (frames <= 0) {
            mix = mixTarget
            left = leftTarget
            right = rightTarget
            width = widthTarget
            rampFrames = 0
            return
        }
        mixStep = (mixTarget - mix) / frames
        leftStep = (leftTarget - left) / frames
        rightStep = (rightTarget - right) / frames
        widthStep = (widthTarget - width) / frames
        rampFrames = frames
    }

    private fun advance() {
        if (rampFrames == 0) return
        mix += mixStep
        left += leftStep
        right += rightStep
        width += widthStep
        rampFrames -= 1
        if (rampFrames == 0) {
            mix = mixTarget
            left = leftTarget
            right = rightTarget
            width = widthTarget
        }
    }
}
