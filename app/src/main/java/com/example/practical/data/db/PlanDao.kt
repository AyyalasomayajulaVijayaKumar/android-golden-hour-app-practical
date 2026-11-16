package com.example.practical.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {

    @Query("SELECT * FROM photoshoot_plans ORDER BY dateStr ASC, createdAt DESC")
    fun getAllPlans(): Flow<List<PlanEntity>>

    @Query("SELECT * FROM photoshoot_plans WHERE dateStr = :dateStr ORDER BY createdAt DESC")
    fun getPlansForDate(dateStr: String): Flow<List<PlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlan(plan: PlanEntity): Long

    @Update
    fun updatePlan(plan: PlanEntity): Int

    @Delete
    fun deletePlan(plan: PlanEntity): Int

    @Query("DELETE FROM photoshoot_plans WHERE id = :id")
    fun deletePlanById(id: Long): Int
}
