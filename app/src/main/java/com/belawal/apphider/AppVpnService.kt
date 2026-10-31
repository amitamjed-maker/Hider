package com.belawal.apphider

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.FileInputStream
import java.io.IOException

/**
 * শুধু হাইড করা অ্যাপগুলোর ইন্টারনেট ট্র্যাফিক এই VPN টানেলে ঢোকে
 * (addAllowedApplication দিয়ে), আর টানেল কিছুই ফরোয়ার্ড করে না —
 * ফলে ঐ অ্যাপগুলোর নেট কার্যত বন্ধ হয়ে যায়। অন্য সব অ্যাপের নেট
 * স্বাভাবিকভাবে চলতে থাকে, ভিপিএনকে পাশ কাটিয়ে।
 */
class AppVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    @Volatile private var running = false
    private var worker: Thread? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopVpn()
            return START_NOT_STICKY
        }
        startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        if (running) return
        val hidden = HiddenAppsStore.getHidden(this)
        if (hidden.isEmpty()) {
            stopSelf()
            return
        }

        val builder = Builder()
            .setSession("AppHider")
            .addAddress("10.10.0.2", 32)
            .addRoute("0.0.0.0", 0)
            .setBlocking(true)

        var addedAny = false
        for (pkg in hidden) {
            try {
                builder.addAllowedApplication(pkg)
                addedAny = true
            } catch (e: Exception) {
                Log.w("AppVpnService", "package not found: $pkg")
            }
        }
        if (!addedAny) {
            stopSelf()
            return
        }

        vpnInterface = builder.establish()
        running = true

        worker = Thread {
            try {
                val input = FileInputStream(vpnInterface!!.fileDescriptor)
                val buffer = ByteArray(32767)
                while (running) {
                    // ব্ল্যাকহোল: প্যাকেট পড়ে ফেলে দেয়, কোথাও ফরোয়ার্ড করে না
                    val len = input.read(buffer)
                    if (len < 0) break
                }
            } catch (e: IOException) {
                // টানেল বন্ধ হয়ে গেলে এখানে আসবে, স্বাভাবিক
            }
        }
        worker?.start()
    }

    private fun stopVpn() {
        running = false
        try {
            vpnInterface?.close()
        } catch (e: IOException) {
            // ignore
        }
        vpnInterface = null
        stopSelf()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    companion object {
        const val ACTION_STOP = "com.belawal.apphider.STOP_VPN"
    }
}
