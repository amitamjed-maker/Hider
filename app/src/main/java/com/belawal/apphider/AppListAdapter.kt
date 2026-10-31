package com.belawal.apphider

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppListAdapter(
    private val apps: MutableList<AppEntry>,
    private val onToggle: (AppEntry, Boolean) -> Unit
) : RecyclerView.Adapter<AppListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val label: TextView = view.findViewById(R.id.appLabel)
        val checkBox: CheckBox = view.findViewById(R.id.appHideCheck)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = apps[position]
        holder.icon.setImageDrawable(entry.icon)
        holder.label.text = entry.label
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = entry.hidden
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            entry.hidden = isChecked
            onToggle(entry, isChecked)
        }
        holder.itemView.setOnClickListener {
            holder.checkBox.isChecked = !holder.checkBox.isChecked
        }
    }

    override fun getItemCount(): Int = apps.size
}
