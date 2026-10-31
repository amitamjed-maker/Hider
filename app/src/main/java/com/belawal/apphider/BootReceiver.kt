package com.belawal.apphider

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.VpnService

/**
 * ফোন রিস্টার্টের পর, যদি আগে VPN পারমিশন দেওয়া থাকে, তাহলে
 * হাইডিং প্রোটেকশন (নেট ব্লক) আবার চালু করে দেয়।
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        if (HiddenAppsStore.getHidden(context).isEmpty()) return
        if (VpnService.prepare(context) == null) {
            // পারমিশন আগে থেকেই দেওয়া আছে, তাই সরাসরি সার্ভিস চালু করা যাবে
            context.startService(Intent(context, AppVpnService::class.java))
        }
    }
}
