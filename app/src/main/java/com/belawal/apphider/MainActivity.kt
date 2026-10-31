package com.belawal.apphider

import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: AppListAdapter
    private lateinit var statusText: TextView
    private val apps = mutableListOf<AppEntry>()

    private val vpnPermissionLauncher =
        registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                startService(Intent(this, AppVpnService::class.java))
                Toast.makeText(this, "নেট ব্লক প্রোটেকশন চালু হয়েছে", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "VPN পারমিশন ছাড়া নেট ব্লক করা যাবে না", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        val recyclerView: RecyclerView = findViewById(R.id.appRecyclerView)
        val enableProtectionBtn: Button = findViewById(R.id.btnEnableProtection)
        val accessibilityBtn: Button = findViewById(R.id.btnAccessibility)

        loadApps()
        adapter = AppListAdapter(apps) { entry, hide ->
            HiddenAppsStore.toggle(this, entry.packageName, hide)
            refreshProtectionIfRunning()
            updateStatus()
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        enableProtectionBtn.setOnClickListener { requestVpnPermission() }
        accessibilityBtn.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Toast.makeText(
                this,
                "তালিকায় গিয়ে \"App Hider\" খুঁজে অন করে দাও",
                Toast.LENGTH_LONG
            ).show()
        }

        updateStatus()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun loadApps() {
        apps.clear()
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        val hidden = HiddenAppsStore.getHidden(this)
        val seen = mutableSetOf<String>()

        for (info in resolveInfos) {
            val pkg = info.activityInfo.packageName
            if (pkg == packageName) continue // নিজেকে তালিকায় দেখানোর দরকার নেই
            if (!seen.add(pkg)) continue
            val label = info.loadLabel(pm).toString()
            val icon = try { info.loadIcon(pm) } catch (e: Exception) { null }
            apps.add(AppEntry(pkg, label, icon, hidden.contains(pkg)))
        }
        apps.sortBy { it.label.lowercase() }
    }

    private fun requestVpnPermission() {
        val hidden = HiddenAppsStore.getHidden(this)
        if (hidden.isEmpty()) {
            Toast.makeText(this, "আগে অন্তত একটা অ্যাপ হাইড করো", Toast.LENGTH_SHORT).show()
            return
        }
        val prepareIntent = VpnService.prepare(this)
        if (prepareIntent != null) {
            vpnPermissionLauncher.launch(prepareIntent)
        } else {
            startService(Intent(this, AppVpnService::class.java))
            Toast.makeText(this, "নেট ব্লক প্রোটেকশন চালু হয়েছে", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshProtectionIfRunning() {
        // হাইড লিস্ট পাল্টালে VPN রিস্টার্ট করে নতুন তালিকা লোড করায়
        val hidden = HiddenAppsStore.getHidden(this)
        if (hidden.isNotEmpty() && VpnService.prepare(this) == null) {
            stopService(Intent(this, AppVpnService::class.java))
            startService(Intent(this, AppVpnService::class.java))
        }
    }

    private fun updateStatus() {
        val hiddenCount = HiddenAppsStore.getHidden(this).size
        statusText.text = "বর্তমানে $hiddenCount টা অ্যাপ হাইড করা আছে।\n" +
            "অ্যাপ খুলতে ডায়াল করো: *#*#3330#*#*"
    }
}
