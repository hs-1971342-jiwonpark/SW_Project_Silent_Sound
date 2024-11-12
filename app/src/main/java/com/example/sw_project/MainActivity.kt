package com.example.sw_project

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.ImageViewTarget
import com.example.sw_project.databinding.ActivityMainBinding
import com.example.sw_project.domain.mqtt.MqttClient
import com.example.sw_project.domain.mqtt.PCMPlayer
import com.example.sw_project.ui.base.BaseActivity
import java.net.Inet4Address
import java.net.NetworkInterface

class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

    private var mqttClient: MqttClient? = null
    private var pcmPlayer: PCMPlayer? = null
    private var gifDrawable: GifDrawable? = null
    var mqttIp = "tcp://220.67.231.91:80"
    val mqttTopic = "audio"

    @RequiresApi(Build.VERSION_CODES.S)
    override fun setLayout() {
        pcmPlayer = PCMPlayer()
        initAudio()
        openWifi()
        setClient()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun setClient() {
        mqttIp = "tcp://220.67.231.91:80" //추후에 >> getIPAddress()
        mqttClient = MqttClient(this@MainActivity, mqttIp, mqttTopic) { _, data ->
            pcmPlayer?.play(data)
        }
        mqttClient?.apply {
            publish("request_pcm_format", "")
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun openWifi() {
        binding.activityMainSettingIv.setOnClickListener {
            openNetworkSettings()
        }
        binding.activityMainPlayIv.setOnClickListener {
            when (it.tag) {
                "play" -> pauseAudio()
                "pause" -> playAudio()
            }
        }
    }

    private fun initAudio() {
        binding.activityMainPlayIv.tag = "pause"
        loadGif(binding.activityMainAudioOnIv, R.drawable.ic_audio_on)
        gifDrawable?.stop()  // GIF를 시작하지 않고 멈춘 상태로 둡니다.
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun playAudio() {
        pcmPlayer?.stop()
        binding.activityMainPlayIv.setBackgroundResource(R.drawable.ic_play)
        binding.activityMainPlayIv.tag = "play"
        gifDrawable?.stop()  // GIF 재생 시작
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun pauseAudio() {
        mqttClient = MqttClient(this@MainActivity, mqttIp, mqttTopic) { _, data ->
            pcmPlayer?.play(data)
        }
        mqttClient?.apply {
            publish("request_pcm_format", "")
        }
        binding.activityMainPlayIv.setBackgroundResource(R.drawable.ic_pause)
        binding.activityMainPlayIv.tag = "pause"
        gifDrawable?.start()  // GIF 애니메이션 멈춤
    }


    private fun loadGif(imageView: ImageView, gifResource: Int) {
        Glide.with(this)
            .asGif()
            .load(gifResource)
            .centerCrop()
            .into(object : ImageViewTarget<GifDrawable>(imageView) {
                override fun setResource(resource: GifDrawable?) {
                    gifDrawable = resource // GifDrawable을 변수에 저장
                    gifDrawable?.setLoopCount(GifDrawable.LOOP_FOREVER) // 반복 설정
                    view.setImageDrawable(resource)
                }
            })
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun openNetworkSettings() {
        val intent = Intent(Settings.Panel.ACTION_WIFI)
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onDestroy() {
        super.onDestroy()
        mqttClient?.disconnect()
        pcmPlayer?.stop()
        gifDrawable?.stop() // 액티비티 종료 시 GIF 멈춤
    }

    // IP 주소 가져오는 함수
    private fun getIPAddress() {
        try {
            val connectivityManager =
                this@MainActivity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)

            if (capabilities != null &&
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            ) {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                while (interfaces.hasMoreElements()) {
                    val networkInterface = interfaces.nextElement()
                    val addresses = networkInterface.inetAddresses

                    while (addresses.hasMoreElements()) {
                        val address = addresses.nextElement()
                        if (!address.isLoopbackAddress && address is Inet4Address) {
                            val ip = address.hostAddress ?: ""
                            if (ip.isNotEmpty()) {
                                mqttIp = "tcp://$ip:80"
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
