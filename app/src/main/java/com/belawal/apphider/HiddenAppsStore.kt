package com.belawal.apphider

import android.content.Context

/**
 * কোন কোন অ্যাপ হাইড করা আছে তার তালিকা সেভ/লোড করার জন্য।
 */
object HiddenAppsStore {
    private const val PREFS = "app_hider_prefs"
    private const val KEY_HIDDEN = "hidden_packages"

    fun getHidden(context: Context): MutableSet<String> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return HashSet(prefs.getStringSet(KEY_HIDDEN, emptySet()) ?: emptySet())
    }

    fun setHidden(context: Context, packages: Set<String>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_HIDDEN, packages).apply()
    }

    fun toggle(context: Context, packageName: String, hide: Boolean) {
        val current = getHidden(context)
        if (hide) current.add(packageName) else current.remove(packageName)
        setHidden(context, current)
    }
}
