package com.belawal.apphider

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * ডায়ালার থেকে *#*#3330#*#* চাপলে এই রিসিভার ট্রিগার হয় এবং
 * লুকানো MainActivity কে খুলে দেয়। App Hider-এর কোনো লঞ্চার আইকন
 * নেই, তাই এটাই অ্যাপ খোলার একমাত্র উপায়।
 */
class SecretCodeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SECRET_CODE") {
            val launch = Intent(context, MainActivity::class.java)
            launch.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(launch)
        }
    }
}
