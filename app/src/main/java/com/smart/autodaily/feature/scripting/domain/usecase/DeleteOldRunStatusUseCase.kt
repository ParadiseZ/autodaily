package com.smart.autodaily.feature.scripting.domain.usecase

import com.smart.autodaily.data.dao.ScriptRunStatusDao
import com.smart.autodaily.data.dao.ScriptSetRunStatusDao
import com.smart.autodaily.utils.DateUtils
import com.smart.autodaily.utils.Lom
import java.util.Calendar

class DeleteOldRunStatusUseCase(
    private val scriptRunStatusDao: ScriptRunStatusDao,
    private val scriptSetRunStatusDao: ScriptSetRunStatusDao
) {
    suspend fun execute() {
        try {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -7) // Go back 7 days
            val sevenDaysAgo = DateUtils.formatDate(calendar.time, "yyyy-MM-dd") // Assuming DateUtils.formatDate exists

            val deletedScriptRunStatusCount = scriptRunStatusDao.deleteOlderThan(sevenDaysAgo)
            val deletedScriptSetRunStatusCount = scriptSetRunStatusDao.deleteOlderThan(sevenDaysAgo)

            Lom.i(
                "DeleteOldRunStatusUseCase",
                "Deleted $deletedScriptRunStatusCount old ScriptRunStatus records and $deletedScriptSetRunStatusCount old ScriptSetRunStatus records older than $sevenDaysAgo."
            )
        } catch (e: Exception) {
            Lom.e("DeleteOldRunStatusUseCase", "Error deleting old run statuses: ${e.message}", e)
            // Handle error as appropriate for the application
        }
    }
}
