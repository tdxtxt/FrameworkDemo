package com.tdxtxt.mdbinder.repository

import com.tdxtxt.mdbinder.bean.UserInfo


/**
 * <pre>
 *     author : tangdexiang
 *     time   : 2023-10-27
 *     desc   :
 * </pre>
 */
object DBRepository {
    private val mData by lazy {
        mutableListOf(
            UserInfo("1001", "张山"), UserInfo("1002", "历史"), UserInfo("1003", "王麻子"),
            UserInfo("1004", "狗蛋"), UserInfo("1005", "正如"), UserInfo("1006", "八嘎"),
            UserInfo("1007", "让人"), UserInfo("1008", "信息"), UserInfo("1009", "嗯嗯")
        )
    }

    fun queryUserInfo(userId: String?): UserInfo? {
        return mData.filter { it.id == userId }.firstOrNull()
    }
}