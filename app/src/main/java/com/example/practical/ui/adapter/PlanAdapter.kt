package com.example.practical.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.practical.R
import com.example.practical.data.db.PlanEntity

class PlanAdapter(
    private val onEditClick: (PlanEntity) -> Unit,
    private val onDeleteClick: (PlanEntity) -> Unit
) : ListAdapter<PlanEntity, PlanAdapter.PlanViewHolder>(PlanDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photoshoot_plan, parent, false)
        return PlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPlaceName: TextView = itemView.findViewById(R.id.tvPlaceName)
        private val tvPlanDate: TextView = itemView.findViewById(R.id.tvPlanDate)
        private val tvNote: TextView = itemView.findViewById(R.id.tvNote)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditPlan)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeletePlan)

        fun bind(plan: PlanEntity) {
            tvPlaceName.text = plan.placeName
            tvPlanDate.text = plan.dateStr
            tvNote.text = if (plan.note.isNotBlank()) plan.note else "No notes provided"

            btnEdit.setOnClickListener { onEditClick(plan) }
            btnDelete.setOnClickListener { onDeleteClick(plan) }
        }
    }

    class PlanDiffCallback : DiffUtil.ItemCallback<PlanEntity>() {
        override fun areItemsTheSame(oldItem: PlanEntity, newItem: PlanEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PlanEntity, newItem: PlanEntity): Boolean {
            return oldItem == newItem
        }
    }
}
