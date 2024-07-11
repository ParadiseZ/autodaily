package com.smart.autodaily.command

import com.smart.autodaily.data.entity.ScriptActionInfo

interface Command {
    fun exec(sai: ScriptActionInfo): Boolean
}