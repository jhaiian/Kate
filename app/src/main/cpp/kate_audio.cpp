#include <jni.h>
#include <oboe/Oboe.h>

#include <algorithm>
#include <atomic>
#include <cmath>
#include <cstdint>
#include <cstring>
#include <ctime>
#include <memory>
#include <vector>

namespace {

constexpr int kFormatI16 = 1;
constexpr int kFormatFloat = 2;
constexpr int kFormatI24 = 3;
constexpr int kFormatI32 = 4;
constexpr int64_t kNanosPerSecond = 1000000000LL;
constexpr int64_t kCheckNanos = 250000000LL;
constexpr int64_t kStallNanos = 1000000000LL;

int bytesPerSample(int format) {
    switch (format) {
        case kFormatI16:
            return 2;
        case kFormatI24:
            return 3;
        default:
            return 4;
    }
}

oboe::AudioFormat toOboeFormat(int format) {
    switch (format) {
        case kFormatI16:
            return oboe::AudioFormat::I16;
        case kFormatI24:
            return oboe::AudioFormat::I24;
        case kFormatI32:
            return oboe::AudioFormat::I32;
        default:
            return oboe::AudioFormat::Float;
    }
}

int64_t monotonicNanos() {
    timespec now{};
    clock_gettime(CLOCK_MONOTONIC, &now);
    return static_cast<int64_t>(now.tv_sec) * kNanosPerSecond + now.tv_nsec;
}

class Engine : public oboe::AudioStreamDataCallback,
               public oboe::AudioStreamErrorCallback,
               public std::enable_shared_from_this<Engine> {
public:
    bool open(int sampleRate, int channels, int format, int capacityFrames) {
        format_ = format;
        sampleRate_ = sampleRate;
        sampleBytes_ = bytesPerSample(format);
        frameBytes_ = channels * sampleBytes_;
        capacity_ = static_cast<int64_t>(capacityFrames) * frameBytes_;
        ring_.assign(static_cast<size_t>(capacity_), 0);
        oboe::AudioStreamBuilder builder;
        builder.setDirection(oboe::Direction::Output)
            ->setSharingMode(oboe::SharingMode::Shared)
            ->setPerformanceMode(oboe::PerformanceMode::None)
            ->setUsage(oboe::Usage::Media)
            ->setContentType(oboe::ContentType::Music)
            ->setFormat(toOboeFormat(format))
            ->setChannelCount(channels)
            ->setSampleRate(sampleRate)
            ->setFormatConversionAllowed(false)
            ->setChannelConversionAllowed(false)
            ->setSampleRateConversionQuality(oboe::SampleRateConversionQuality::None)
            ->setDataCallback(shared_from_this())
            ->setErrorCallback(shared_from_this());
        if (builder.openStream(stream_) != oboe::Result::OK || !stream_) {
            stream_.reset();
            return false;
        }
        if (stream_->getFormat() != toOboeFormat(format) ||
            stream_->getChannelCount() != channels ||
            stream_->getSampleRate() != sampleRate) {
            close();
            return false;
        }
        lastLatency_ = stream_->getBufferSizeInFrames();
        return true;
    }

    bool start() {
        if (!stream_ || disconnected_.load()) return false;
        playing_.store(true);
        const int64_t now = monotonicNanos();
        lastCheck_.store(now);
        lastCallback_.store(now);
        return stream_->requestStart() == oboe::Result::OK;
    }

    void ensureRunning() {
        if (!stream_ || disconnected_.load() || !playing_.load()) return;
        const int64_t now = monotonicNanos();
        if (now - lastCheck_.load() < kCheckNanos) return;
        lastCheck_.store(now);
        const oboe::StreamState state = stream_->getState();
        const bool active = state == oboe::StreamState::Started || state == oboe::StreamState::Starting;
        const bool stalled = now - lastCallback_.load() > kStallNanos;
        if (active && !stalled) return;
        if (active) stream_->requestPause();
        lastCallback_.store(now);
        stream_->requestStart();
    }

    void pause() {
        playing_.store(false);
        if (stream_ && !disconnected_.load()) stream_->requestPause();
    }

    void reset() {
        playing_.store(false);
        if (stream_ && !disconnected_.load()) {
            stream_->pause();
            stream_->flush();
        }
        readPos_.store(0, std::memory_order_release);
        writePos_.store(0, std::memory_order_release);
        lastPresented_ = 0;
        lastLatency_ = stream_ ? stream_->getBufferSizeInFrames() : 0;
    }

    void close() {
        playing_.store(false);
        if (stream_) {
            stream_->stop();
            stream_->close();
            stream_.reset();
        }
    }

    int write(const uint8_t *source, int length) {
        if (length <= 0 || frameBytes_ <= 0) return 0;
        const int64_t write = writePos_.load(std::memory_order_relaxed);
        const int64_t read = readPos_.load(std::memory_order_acquire);
        int64_t count = std::min<int64_t>(length, capacity_ - (write - read));
        count -= count % frameBytes_;
        if (count <= 0) return 0;
        const int64_t start = write % capacity_;
        const int64_t first = std::min<int64_t>(count, capacity_ - start);
        std::memcpy(ring_.data() + start, source, static_cast<size_t>(first));
        if (count > first) {
            std::memcpy(ring_.data(), source + first, static_cast<size_t>(count - first));
        }
        writePos_.store(write + count, std::memory_order_release);
        return static_cast<int>(count);
    }

    int64_t presentedFrames() {
        if (frameBytes_ <= 0) return 0;
        const int64_t consumed = readPos_.load(std::memory_order_acquire) / frameBytes_;
        if (consumed <= 0 || !stream_) return 0;
        int64_t latency = lastLatency_;
        if (!disconnected_.load()) {
            auto timestamp = stream_->getTimestamp(CLOCK_MONOTONIC);
            if (timestamp) {
                const int64_t written = stream_->getFramesWritten();
                int64_t position = timestamp.value().position;
                if (playing_.load()) {
                    const int64_t elapsed = std::max<int64_t>(0, monotonicNanos() - timestamp.value().timestamp);
                    position += elapsed * sampleRate_ / kNanosPerSecond;
                }
                position = std::min(position, written);
                latency = std::max<int64_t>(0, written - position);
                lastLatency_ = latency;
            }
        }
        int64_t presented = consumed - std::min(latency, consumed);
        presented = std::max(presented, lastPresented_);
        lastPresented_ = presented;
        return presented;
    }

    void setVolume(float volume) { volume_.store(std::clamp(volume, 0.0f, 1.0f)); }

    bool isDisconnected() const { return disconnected_.load(); }

    int underruns() const { return underruns_.load(); }

    int bufferFrames() const {
        const int device = stream_ ? stream_->getBufferSizeInFrames() : 0;
        return static_cast<int>(capacity_ / std::max(1, frameBytes_)) + std::max(0, device);
    }

    oboe::DataCallbackResult onAudioReady(oboe::AudioStream *, void *audioData, int32_t numFrames) override {
        lastCallback_.store(monotonicNanos(), std::memory_order_relaxed);
        auto *output = static_cast<uint8_t *>(audioData);
        const int64_t needed = static_cast<int64_t>(numFrames) * frameBytes_;
        const int64_t write = writePos_.load(std::memory_order_acquire);
        const int64_t read = readPos_.load(std::memory_order_relaxed);
        int64_t count = std::min(write - read, needed);
        count -= count % frameBytes_;
        if (count > 0) {
            const int64_t start = read % capacity_;
            const int64_t first = std::min<int64_t>(count, capacity_ - start);
            std::memcpy(output, ring_.data() + start, static_cast<size_t>(first));
            if (count > first) {
                std::memcpy(output + first, ring_.data(), static_cast<size_t>(count - first));
            }
            readPos_.store(read + count, std::memory_order_release);
            const float volume = volume_.load(std::memory_order_relaxed);
            if (volume != 1.0f || appliedVolume_ != 1.0f) applyVolume(output, count / frameBytes_, appliedVolume_, volume);
            appliedVolume_ = volume;
        } else {
            count = 0;
        }
        if (count < needed) {
            std::memset(output + count, 0, static_cast<size_t>(needed - count));
            if (playing_.load(std::memory_order_relaxed)) underruns_.fetch_add(1);
        }
        return oboe::DataCallbackResult::Continue;
    }

    void onErrorAfterClose(oboe::AudioStream *, oboe::Result) override {
        disconnected_.store(true);
        playing_.store(false);
    }

private:
    void applyVolume(uint8_t *data, int64_t frames, float from, float to) {
        if (frames <= 0 || sampleBytes_ <= 0) return;
        const int channels = frameBytes_ / sampleBytes_;
        const double step = (static_cast<double>(to) - static_cast<double>(from)) / static_cast<double>(frames);
        double gain = from;
        switch (format_) {
            case kFormatI16: {
                auto *values = reinterpret_cast<int16_t *>(data);
                for (int64_t frame = 0; frame < frames; ++frame) {
                    gain += step;
                    for (int channel = 0; channel < channels; ++channel, ++values) {
                        const long scaled = std::lrint(*values * gain);
                        *values = static_cast<int16_t>(std::clamp<long>(scaled, -32768, 32767));
                    }
                }
                break;
            }
            case kFormatI24: {
                uint8_t *sample = data;
                for (int64_t frame = 0; frame < frames; ++frame) {
                    gain += step;
                    for (int channel = 0; channel < channels; ++channel, sample += 3) {
                        int32_t value = sample[0] | (sample[1] << 8) | (sample[2] << 16);
                        if (value & 0x800000) value |= ~0xFFFFFF;
                        const long scaled = std::clamp<long>(std::lrint(value * gain), -8388608, 8388607);
                        sample[0] = static_cast<uint8_t>(scaled & 0xFF);
                        sample[1] = static_cast<uint8_t>((scaled >> 8) & 0xFF);
                        sample[2] = static_cast<uint8_t>((scaled >> 16) & 0xFF);
                    }
                }
                break;
            }
            case kFormatI32: {
                auto *values = reinterpret_cast<int32_t *>(data);
                for (int64_t frame = 0; frame < frames; ++frame) {
                    gain += step;
                    for (int channel = 0; channel < channels; ++channel, ++values) {
                        *values = static_cast<int32_t>(std::clamp(*values * gain, -2147483648.0, 2147483647.0));
                    }
                }
                break;
            }
            default: {
                auto *values = reinterpret_cast<float *>(data);
                for (int64_t frame = 0; frame < frames; ++frame) {
                    gain += step;
                    const float scale = static_cast<float>(gain);
                    for (int channel = 0; channel < channels; ++channel, ++values) *values *= scale;
                }
                break;
            }
        }
    }

    std::shared_ptr<oboe::AudioStream> stream_;
    std::vector<uint8_t> ring_;
    int64_t capacity_ = 0;
    int format_ = kFormatFloat;
    int sampleRate_ = 0;
    int sampleBytes_ = 4;
    int frameBytes_ = 0;
    std::atomic<int64_t> readPos_{0};
    std::atomic<int64_t> writePos_{0};
    std::atomic<float> volume_{1.0f};
    float appliedVolume_ = 1.0f;
    std::atomic<bool> playing_{false};
    std::atomic<bool> disconnected_{false};
    std::atomic<int> underruns_{0};
    std::atomic<int64_t> lastCheck_{0};
    std::atomic<int64_t> lastCallback_{0};
    int64_t lastPresented_ = 0;
    int64_t lastLatency_ = 0;
};

struct Holder {
    std::shared_ptr<Engine> engine;
};

Engine *engineOf(jlong handle) {
    if (handle == 0) return nullptr;
    return reinterpret_cast<Holder *>(handle)->engine.get();
}

}

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_jhaiian_kate_playback_NativeAudio_open(JNIEnv *, jclass, jint sampleRate, jint channels, jint format, jint capacityFrames) {
    if (sampleRate <= 0 || channels <= 0 || capacityFrames <= 0) return 0;
    auto holder = new Holder{std::make_shared<Engine>()};
    if (!holder->engine->open(sampleRate, channels, format, capacityFrames)) {
        delete holder;
        return 0;
    }
    return reinterpret_cast<jlong>(holder);
}

JNIEXPORT jboolean JNICALL
Java_com_jhaiian_kate_playback_NativeAudio_start(JNIEnv *, jclass, jlong handle) {
    Engine *engine = engineOf(handle);
    return engine != nullptr && engine->start() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_jhaiian_kate_playback_NativeAudio_ensureRunning(JNIEnv *, jclass, jlong handle) {
    if (Engine *engine = engineOf(handle)) engine->ensureRunning();
}

JNIEXPORT void JNICALL
Java_com_jhaiian_kate_playback_NativeAudio_pause(JNIEnv *, jclass, jlong handle) {
    if (Engine *engine = engineOf(handle)) engine->pause();
}

JNIEXPORT void JNICALL
Java_com_jhaiian_kate_playback_NativeAudio_reset(JNIEnv *, jclass, jlong handle) {
    if (Engine *engine = engineOf(handle)) engine->reset();
}

JNIEXPORT void JNICALL
Java_com_jhaiian_kate_playback_NativeAudio_close(JNIEnv *, jclass, jlong handle) {
    if (handle == 0) return;
    auto *holder = reinterpret_cast<Holder *>(handle);
    holder->engine->close();
    delete holder;
}

JNIEXPORT jint JNICALL
Java_com_jhaiian_kate_playback_NativeAudio_writeDirect(JNIEnv *env, jclass, jlong handle, jobject buffer, jint offset, jint length) {
    Engine *engine = engineOf(handle);
    if (engine == nullptr) return 0;
    auto *address = static_cast<uint8_t *>(env->GetDirectBufferAddress(buffer));
    if (address == nullptr) return 0;
    return engine->write(address + offset, length);
}

JNIEXPORT jint JNICALL
Java_com_jhaiian_kate_playback_NativeAudio_writeArray(JNIEnv *env, jclass, jlong handle, jbyteArray data, jint offset, jint length) {
    Engine *engine = engineOf(handle);
    if (engine == nullptr || length <= 0) return 0;
    jbyte *bytes = env->GetByteArrayElements(data, nullptr);
    if (bytes == nullptr) return 0;
    const int written = engine->write(reinterpret_cast<uint8_t *>(bytes) + offset, length);
    env->ReleaseByteArrayElements(data, bytes, JNI_ABORT);
    return written;
}

JNIEXPORT jlong JNICALL
Java_com_jhaiian_kate_playback_NativeAudio_presentedFrames(JNIEnv *, jclass, jlong handle) {
    Engine *engine = engineOf(handle);
    return engine != nullptr ? static_cast<jlong>(engine->presentedFrames()) : 0;
}

JNIEXPORT void JNICALL
Java_com_jhaiian_kate_playback_NativeAudio_setVolume(JNIEnv *, jclass, jlong handle, jfloat volume) {
    if (Engine *engine = engineOf(handle)) engine->setVolume(volume);
}

JNIEXPORT jboolean JNICALL
Java_com_jhaiian_kate_playback_NativeAudio_isDisconnected(JNIEnv *, jclass, jlong handle) {
    Engine *engine = engineOf(handle);
    return engine == nullptr || engine->isDisconnected() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jint JNICALL
Java_com_jhaiian_kate_playback_NativeAudio_underrunCount(JNIEnv *, jclass, jlong handle) {
    Engine *engine = engineOf(handle);
    return engine != nullptr ? engine->underruns() : 0;
}

JNIEXPORT jint JNICALL
Java_com_jhaiian_kate_playback_NativeAudio_bufferFrames(JNIEnv *, jclass, jlong handle) {
    Engine *engine = engineOf(handle);
    return engine != nullptr ? engine->bufferFrames() : 0;
}

}
