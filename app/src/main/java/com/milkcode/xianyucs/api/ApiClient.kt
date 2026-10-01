package com.milkcode.xianyucs.api

import com.milkcode.xianyucs.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * 闲鱼管理系统 API 客户端
 * 对接上游 FastAPI 后端 (xianyu-auto-reply-fix)
 */
class ApiClient(var baseUrl: String = "") {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }

    private val http = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    var token: String? = null
        private set

    fun setToken(t: String?) { token = t }

    private fun url(path: String): String {
        val b = baseUrl.trimEnd('/')
        val p = if (path.startsWith("/")) path else "/$path"
        return b + p
    }

    private fun reqBuilder(path: String): Request.Builder {
        val rb = Request.Builder().url(url(path))
        token?.let { rb.header("Authorization", "Bearer $it") }
        rb.header("Accept", "application/json")
        return rb
    }

    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

    /** 执行请求，返回 body；非 2xx 抛异常 */
    private suspend fun exec(req: Request): String = withContext(Dispatchers.IO) {
        http.newCall(req).execute().use { resp ->
            val body = resp.body?.string() ?: ""
            if (!resp.isSuccessful) throw ApiException(resp.code, "HTTP ${resp.code}: ${body.take(200)}")
            body
        }
    }

    private suspend fun get(path: String): String = exec(reqBuilder(path).get().build())

    private suspend fun post(path: String, bodyJson: String = "{}"): String =
        exec(reqBuilder(path).post(bodyJson.toRequestBody(JSON_MEDIA)).build())

    private suspend fun delete(path: String): String =
        exec(reqBuilder(path).delete().build())

    // ---------- 登录 ----------
    suspend fun login(username: String, password: String): LoginResponse {
        val paths = listOf("/api/auth/login", "/api/login", "/auth/login", "/login")
        var lastErr: Exception? = null
        for (p in paths) {
            try {
                val body = json.encodeToString(LoginRequest.serializer(), LoginRequest(username, password))
                val r = post(p, body)
                val parsed = json.decodeFromString(LoginResponse.serializer(), r)
                if (parsed.bearer.isNotBlank()) {
                    token = parsed.bearer
                    return parsed
                }
            } catch (e: Exception) { lastErr = e }
        }
        throw lastErr ?: ApiException(0, "所有登录路径均失败")
    }

    // ---------- 账号 ----------
    suspend fun getAccounts(): List<XyAccount> {
        val paths = listOf("/api/accounts", "/api/account/list", "/accounts")
        var lastErr: Exception? = null
        for (p in paths) {
            try {
                return parseAccounts(get(p))
            } catch (e: Exception) { lastErr = e }
        }
        throw lastErr ?: ApiException(0, "获取账号失败")
    }

    private fun parseAccounts(raw: String): List<XyAccount> {
        val t = raw.trim()
        return try {
            if (t.startsWith("[")) {
                json.decodeFromString(kotlinx.serialization.builtins.ListSerializer(XyAccount.serializer()), t)
            } else {
                json.decodeFromString(AccountList.serializer(), t).list
            }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun toggleAccount(id: Any, enable: Boolean): Boolean {
        val paths = listOf(
            "/api/accounts/$id/${if (enable) "enable" else "disable"}",
            "/api/account/$id/${if (enable) "start" else "stop"}",
            "/api/accounts/$id/toggle"
        )
        for (p in paths) {
            try { post(p); return true } catch (_: Exception) { }
        }
        return false
    }

    suspend fun deleteAccount(id: Any): Boolean {
        return try { delete("/api/accounts/$id"); true } catch (_: Exception) { false }
    }

    // ---------- 关键词规则 ----------
    suspend fun getKeywords(): List<KeywordRule> {
        val paths = listOf("/api/keywords", "/api/keyword/list", "/api/reply/keywords")
        for (p in paths) {
            try {
                val t = get(p).trim()
                return if (t.startsWith("[")) json.decodeFromString(
                    kotlinx.serialization.builtins.ListSerializer(KeywordRule.serializer()), t)
                else emptyList()
            } catch (_: Exception) { }
        }
        return emptyList()
    }

    suspend fun addKeyword(rule: KeywordRule): Boolean {
        return try {
            val body = json.encodeToString(KeywordRule.serializer(), rule)
            post("/api/keywords", body)
            true
        } catch (_: Exception) { false }
    }

    suspend fun deleteKeyword(id: Any): Boolean {
        return try { delete("/api/keywords/$id"); true } catch (_: Exception) { false }
    }

    // ---------- 日志 ----------
    suspend fun getLogs(limit: Int = 100): List<LogEntry> {
        val paths = listOf("/api/logs?limit=$limit", "/api/log/recent?limit=$limit", "/api/logs")
        for (p in paths) {
            try {
                val t = get(p).trim()
                return if (t.startsWith("[")) json.decodeFromString(
                    kotlinx.serialization.builtins.ListSerializer(LogEntry.serializer()), t)
                else emptyList()
            } catch (_: Exception) { }
        }
        return emptyList()
    }

    // ---------- 统计 ----------
    suspend fun getStats(): SystemStat? {
        val paths = listOf("/api/stats", "/api/system/stats", "/api/statistics")
        for (p in paths) {
            try {
                return json.decodeFromString(SystemStat.serializer(), get(p))
            } catch (_: Exception) { }
        }
        return null
    }

    // ---------- 健康检查 ----------
    suspend fun healthCheck(): Boolean = withContext(Dispatchers.IO) {
        try {
            val b = baseUrl.trimEnd('/')
            http.newCall(Request.Builder().url("$b/health").get().build()).execute().use { it.isSuccessful }
        } catch (_: Exception) { false }
    }
}

class ApiException(val code: Int, message: String) : IOException(message)
