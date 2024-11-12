package com.example.sw_project.domain.mqtt

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.S)
class PCMPlayer(private val sampleRate: Int = 22050, private val channels: Int = 1) {
    private val audioTrack: AudioTrack

    init {
        val channelConfig =
            if (channels == 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO
        // AudioTrack.Builder를 사용하여 AudioTrack을 생성
        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)  // 스트림 타입을 대신하는 역할
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)                // 샘플 레이트 설정
                    .setEncoding(AudioFormat.ENCODING_PCM_32BIT)  // PCM 인코딩 설정
                    .setChannelMask(channelConfig)             // 채널 설정
                    .build()
            )
            .setBufferSizeInBytes(
                AudioTrack.getMinBufferSize(
                    sampleRate,
                    channelConfig,
                    AudioFormat.ENCODING_PCM_32BIT
                )
            )
            .setTransferMode(AudioTrack.MODE_STREAM)  // 스트리밍 모드 설정
            .build()
        audioTrack.play()
    }

    fun play(pcmData: ByteArray) {
        audioTrack.write(pcmData, 0, pcmData.size)
    }

    fun stop() {
        audioTrack.stop()
        audioTrack.release()
    }
}
