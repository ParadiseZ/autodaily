package com.smart.autodaily.data

import com.smart.autodaily.data.entity.UserInfo

class UserInfoSingle private constructor() {
    var user: UserInfo? = null
    companion object {
        val instance: UserInfoSingle by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            UserInfoSingle() }
        fun saveUserInfo(user: UserInfo) {
            instance.user = user
        }
    }
}