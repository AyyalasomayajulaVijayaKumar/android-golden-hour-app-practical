package com.example.practical.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photoshoot_plans")
data class PlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateStr: String,
    val placeName: String,
    val note: String,
    val createdAt: Long = System.currentTimeMillis()
)
