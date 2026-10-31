package com.belawal.apphider

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * হাইড করা কোনো অ্যাপ ফোরগ্রাউন্ডে আসার চেষ্টা করলেই সাথে সাথে
 * হোম স্ক্রিনে ফিরিয়ে দেয় — অ্যাপটা আসলে রানই করতে পারে না,
 * তাই তার ব্যাটারি/নেট/জিপিএস ইউজও হয় না।
 *
 * এই সার্ভিসটা ফোনের Settings > Accessibility থেকে ম্যানুয়ালি
 * অন করে দিতে হবে (Android নিরাপত্তার কারণে এটা কোড দিয়ে অটো-অন করা যায় না)।
 */
class HiderAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return

        val hidden = HiddenAppsStore.getHidden(this)
        if (pkg in hidden) {
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    override fun onInterrupt() {
        // কিছু করার দরকার নেই
    }
}
