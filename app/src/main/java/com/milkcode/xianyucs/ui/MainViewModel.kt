package com.milkcode.xianyucs.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.milkcode.xianyucs.api.ApiClient
import com.milkcode.xianyucs.api.Prefs
import com.milkcode.xianyucs.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UiState(
    val baseUrl: String = "",
    val username: String = "",
    val loggedIn: Boolean = false,
    val loading: Boolean = false,
    val toast: String? = null,
    val accounts: List<XyAccount> = emptyList(),
    val keywords: List<KeywordRule> = emptyList(),
    val logs: List<LogEntry> = emptyList(),
    val stats: SystemStat? = null,
    val serverOk: Boolean = false
)

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val api = ApiClient()
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val (base, user, tok) = Prefs.load(getApplication())
            api.baseUrl = base
            api.setToken(tok.ifBlank { null })
            _state.value = _state.value.copy(
                baseUrl = base, username = user,
                loggedIn = tok.isNotBlank()
            )
            if (tok.isNotBlank()) refreshAll()
        }
    }

    fun setToast(m: String?) { _state.value = _state.value.copy(toast = m) }

    fun updateBase(url: String) { _state.value = _state.value.copy(baseUrl = url) }
    fun updateUser(u: String) { _state.value = _state.value.copy(username = u) }

    fun saveServer() {
        viewModelScope.launch {
            Prefs.saveBase(getApplication(), _state.value.baseUrl)
            api.baseUrl = _state.value.baseUrl
            val ok = api.healthCheck()
            _state.value = _state.value.copy(serverOk = ok, toast = if (ok) "服务器连接正常" else "无法连接服务器")
        }
    }

    fun checkHealth() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val ok = api.healthCheck()
            _state.value = _state.value.copy(loading = false, serverOk = ok,
                toast = if (ok) "服务器在线" else "服务器离线")
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            try {
                api.baseUrl = _state.value.baseUrl
                val r = api.login(username, password)
                Prefs.saveBase(getApplication(), _state.value.baseUrl)
                Prefs.saveUser(getApplication(), username)
                Prefs.saveToken(getApplication(), r.bearer)
                _state.value = _state.value.copy(loggedIn = true, username = username,
                    loading = false, toast = "登录成功")
                refreshAll()
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, toast = "登录失败: ${e.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            Prefs.clear(getApplication())
            api.setToken(null)
            _state.value = UiState(baseUrl = _state.value.baseUrl)
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val acc = runCatching { api.getAccounts() }.getOrDefault(emptyList())
            val kw = runCatching { api.getKeywords() }.getOrDefault(emptyList())
            val lg = runCatching { api.getLogs(80) }.getOrDefault(emptyList())
            val st = runCatching { api.getStats() }.getOrNull()
            _state.value = _state.value.copy(
                loading = false, accounts = acc, keywords = kw, logs = lg, stats = st
            )
        }
    }

    fun toggle(acc: XyAccount, enable: Boolean) {
        viewModelScope.launch {
            val id = acc.id ?: acc.accountId ?: return@launch
            val ok = api.toggleAccount(id, enable)
            _state.value = _state.value.copy(toast = if (ok) "操作成功" else "操作失败")
            refreshAll()
        }
    }

    fun delAccount(acc: XyAccount) {
        viewModelScope.launch {
            val id = acc.id ?: acc.accountId ?: return@launch
            api.deleteAccount(id); refreshAll()
        }
    }

    fun addKeyword(k: String, r: String) {
        viewModelScope.launch {
            api.addKeyword(KeywordRule(keyword = k, reply = r))
            refreshAll()
        }
    }

    fun delKeyword(rule: KeywordRule) {
        viewModelScope.launch {
            rule.id?.let { api.deleteKeyword(it) }; refreshAll()
        }
    }
}
