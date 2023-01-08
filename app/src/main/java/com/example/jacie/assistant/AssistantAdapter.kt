package com.example.jacie.assistant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jacie.R
import com.example.jacie.data.Assistant


class AssistantAdapter : RecyclerView.Adapter<AssistantAdapter.ViewHolder>(){

    var data= listOf<Assistant>()
    set(value) {
        field=value
        notifyDataSetChanged()
    }

    class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
        val assistantMessage:TextView=itemView.findViewById(R.id.assistantMsg)
        val humanMessage:TextView=itemView.findViewById(R.id.humanMsg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.assistant_item_layout, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=data[position]
        holder.assistantMessage.text=item.assistant_message
        holder.humanMessage.text=item.human_message
    }

    override fun getItemCount(): Int {
        return data.size
    }
}