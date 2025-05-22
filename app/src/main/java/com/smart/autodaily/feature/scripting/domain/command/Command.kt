package com.smart.autodaily.feature.scripting.domain.command

import com.smart.autodaily.data.entity.ScriptActionInfo

interface Command {
    /**
     * Executes the command.
     * @param sai The script action info associated with this command.
     * @param executor The executor for performing ADB or system-level actions.
     * @return Boolean indicating success or a specific outcome if needed (original signature).
     *         Consider if the return type needs to be more expressive (e.g., an ActionResult).
     *         For now, retaining Boolean to minimize changes from the original interface.
     */
    suspend fun exec(sai: ScriptActionInfo, executor: CommandExecutor): Boolean
}
