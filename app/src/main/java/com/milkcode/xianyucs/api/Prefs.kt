package com.milkcode.xianyucs.api

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "xianyu_cs_prefs")

/** 本地配置：服务器地址 / 用户名 / token */
object Prefs {
    private val K_BASE = stringPreferencesKey("base_url")
    private val K_USER = stringPreferencesKey("username")
    private val K_TOKEN = stringPreferencesKey("token")

    suspend fun load(ctx: Context): Triple<String, String, String> {
        val p = ctx.dataStore.data.first()
        return Triple(
            p[K_BASE] ?: "",
            p[K_USER] ?: "",
            p[K_TOKEN] ?: ""
        )
    }

    suspend fun saveBase(ctx: Context, v: String) {
        ctx.dataStore.edit { it[K_BASE] = v }
    }
    suspend fun saveUser(ctx: Context, v: String) {
        ctx.dataStore.edit { it[K_USER] = v }
    }
    suspend fun saveToken(ctx: Context, v: String) {
        ctx.dataStore.edit { it[K_TOKEN] = v }
    }
    suspend fun clear(ctx: Context) {
        ctx.dataStore.edit { it.clear() }
    }
}
