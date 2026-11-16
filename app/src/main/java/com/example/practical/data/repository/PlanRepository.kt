package com.example.practical.data.repository

import com.example.practical.data.db.PlanDao
import com.example.practical.data.db.PlanEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PlanRepository(private val planDao: PlanDao) {

    val allPlans: Flow<List<PlanEntity>> = planDao.getAllPlans()

    fun getPlansForDate(dateStr: String): Flow<List<PlanEntity>> {
        return planDao.getPlansForDate(dateStr)
    }

    suspend fun insertPlan(plan: PlanEntity): Long {
        return withContext(Dispatchers.IO) {
            planDao.insertPlan(plan)
        }
    }

    suspend fun updatePlan(plan: PlanEntity) {
        withContext(Dispatchers.IO) {
            planDao.updatePlan(plan)
        }
    }

    suspend fun deletePlan(plan: PlanEntity) {
        withContext(Dispatchers.IO) {
            planDao.deletePlan(plan)
        }
    }
}
