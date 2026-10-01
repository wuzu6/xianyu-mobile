package com.milkcode.xianyucs.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 登录请求 */
@Serializable
data class LoginRequest(val username: String, val password: String)

/** 登录响应（兼容多种字段名） */
@Serializable
data class LoginResponse(
    val access_token: String? = null,
    @SerialName("token") val token: String? = null,
    val token_type: String? = "bearer",
    val username: String? = null,
    val expires_in: Long? = null
) {
    val bearer: String get() = access_token ?: token ?: ""
}

/** 闲鱼账号 */
@Serializable
data class XyAccount(
    val id: Int? = null,
    @SerialName("account_id") val accountId: String? = null,
    val nickname: String? = null,
    val username: String? = null,
    @SerialName("enabled") val enabled: Boolean? = false,
    val status: String? = null,
    @SerialName("auto_reply") val autoReply: Boolean? = null,
    @SerialName("cookie_valid") val cookieValid: Boolean? = null
)

/** 通用列表包装 */
@Serializable
data class AccountList(
    val accounts: List<XyAccount>? = null,
    val data: List<XyAccount>? = null,
    val items: List<XyAccount>? = null
) {
    val list: List<XyAccount>
        get() = accounts ?: data ?: items ?: emptyList()
}

/** 关键词回复规则 */
@Serializable
data class KeywordRule(
    val id: Int? = null,
    val keyword: String = "",
    val reply: String = "",
    @SerialName("match_type") val matchType: String = "contains",
    val enabled: Boolean = true,
    @SerialName("is_regex") val isRegex: Boolean = false
)

/** 日志条目 */
@Serializable
data class LogEntry(
    val time: String? = null,
    val level: String? = null,
    val message: String? = null,
    val account: String? = null
)

/** 系统/统计信息 */
@Serializable
data class SystemStat(
    @SerialName("total_accounts") val totalAccounts: Int? = 0,
    @SerialName("online_accounts") val onlineAccounts: Int? = 0,
    @SerialName("total_replies") val totalReplies: Int? = 0,
    @SerialName("today_replies") val todayReplies: Int? = 0
)

/** 通用 API 结果 */
@Serializable
data class ApiResult(
    val success: Boolean? = null,
    val message: String? = null,
    val code: Int? = null
)
