package com.milkcode.xianyucs.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// 配色
val Bg = Color(0xFF0D1117)
val Card = Color(0xFF161B22)
val CardB = Color(0xFF21262D)
val Acc = Color(0xFF4A9EFF)
val Gold = Color(0xFFFFD77A)
val Txt = Color(0xFFE6EDF3)
val Dim = Color(0xFF8B949E)
val Green = Color(0xFF3FB950)
val Red = Color(0xFFF85149)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(vm: MainViewModel = viewModel()) {
    val st by vm.state.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }

    LaunchedEffect(st.toast) {
        st.toast?.let { snack.showSnackbar(it); vm.setToast(null) }
    }

    Scaffold(
        containerColor = Bg,
        snackbarHost = { SnackbarHost(snack) },
        bottomBar = {
            if (st.loggedIn) BottomBar(st, vm)
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            if (!st.loggedIn) LoginScreen(st, vm)
            else when (tabIndex) {
                0 -> HomeScreen(st, vm)
                1 -> AccountScreen(st, vm)
                2 -> KeywordScreen(st, vm)
                3 -> LogScreen(st, vm)
                4 -> SettingScreen(st, vm)
            }
            if (st.loading) {
                Box(Modifier.fillMaxSize().background(Color(0x88000000)), Alignment.Center) {
                    CircularProgressIndicator(color = Acc)
                }
            }
        }
    }
}

// 当前标签（简单全局，避免导航依赖过重）
var tabIndex by mutableStateOf(0)

@Composable
fun BottomBar(st: UiState, vm: MainViewModel) {
    NavigationBar(containerColor = Card, tonalElevation = 0.dp) {
        val tabs = listOf(
            Triple("概览", Icons.Filled.Dashboard, 0),
            Triple("账号", Icons.Filled.Person, 1),
            Triple("关键词", Icons.Filled.Key, 2),
            Triple("日志", Icons.Filled.List, 3),
            Triple("设置", Icons.Filled.Settings, 4),
        )
        tabs.forEach { (label, icon, idx) ->
            NavigationBarItem(
                selected = tabIndex == idx,
                onClick = { tabIndex = idx; if (idx == 0 || idx == 1) vm.refreshAll() },
                icon = { Icon(icon, label, tint = if (tabIndex == idx) Acc else Dim) },
                label = { Text(label, fontSize = 11.sp, color = if (tabIndex == idx) Acc else Dim) },
                colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFF1F2A3A))
            )
        }
    }
}

// ---------------- 登录页 ----------------
@Composable
fun LoginScreen(st: UiState, vm: MainViewModel) {
    var user by remember { mutableStateOf(st.username) }
    var pass by remember { mutableStateOf("") }
    var base by remember { mutableStateOf(st.baseUrl) }

    Column(
        Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.SupportAgent, null, tint = Gold, modifier = Modifier.size(72.dp))
        Spacer(Modifier.height(12.dp))
        Text("闲鱼智能客服", color = Txt, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("手机管理端", color = Dim, fontSize = 13.sp)
        Spacer(Modifier.height(36.dp))

        Field("服务器地址", base, { base = it; vm.updateBase(it) }, "http://1.2.3.4:9000")
        Spacer(Modifier.height(12.dp))
        Field("用户名", user, { user = it })
        Spacer(Modifier.height(12.dp))
        Field("密码", pass, { pass = it }, isPass = true)
        Spacer(Modifier.height(20.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = { vm.updateBase(base); vm.checkHealth() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = if (st.serverOk) Green else Dim)
            ) { Text(if (st.serverOk) "服务器在线" else "测试连接") }
            Button(
                onClick = { vm.updateBase(base); vm.login(user, pass) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Acc)
            ) { Text("登 录", color = Color.White) }
        }
    }
}

@Composable
fun Field(label: String, value: String, onChange: (String) -> Unit, ph: String = "", isPass: Boolean = false) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, color = Dim, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value, onValueChange = onChange,
            placeholder = { Text(ph, color = Color(0xFF484F58)) },
            singleLine = true,
            visualTransformation = if (isPass) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Acc, unfocusedBorderColor = CardB,
                focusedTextColor = Txt, unfocusedTextColor = Txt, cursorColor = Acc
            )
        )
    }
}

// ---------------- 概览 ----------------
@Composable
fun HomeScreen(st: UiState, vm: MainViewModel) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("仪表盘", color = Txt, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("账号总数", "${st.stats?.totalAccounts ?: st.accounts.size}", Acc, Modifier.weight(1f))
                StatCard("在线账号", "${st.stats?.onlineAccounts ?: st.accounts.count { it.enabled == true }}", Green, Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("累计回复", "${st.stats?.totalReplies ?: 0}", Gold, Modifier.weight(1f))
                StatCard("今日回复", "${st.stats?.todayReplies ?: 0}", Color(0xFFBC8CFF), Modifier.weight(1f))
            }
        }
        item {
            InfoCard("服务状态", if (st.serverOk) "已连接" else "未连接", if (st.serverOk) Green else Red)
        }
        item {
            Button(
                onClick = { vm.refreshAll() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Card)
            ) { Icon(Icons.Filled.Refresh, null, tint = Acc); Spacer(Modifier.width(8.dp)); Text("刷新数据", color = Txt) }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color, mod: Modifier) {
    Card(mod, colors = CardDefaults.cardColors(containerColor = Card), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Dim, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            Text(value, color = color, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InfoCard(title: String, value: String, valueColor: Color) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Card), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = Txt, fontSize = 14.sp)
            Text(value, color = valueColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ---------------- 账号 ----------------
@Composable
fun AccountScreen(st: UiState, vm: MainViewModel) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("闲鱼账号", color = Txt, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = { vm.refreshAll() }) { Icon(Icons.Filled.Refresh, null, tint = Acc) }
        }
        Spacer(Modifier.height(10.dp))
        if (st.accounts.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("暂无账号\n请在网页后台添加闲鱼账号", color = Dim, fontSize = 14.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(st.accounts) { acc ->
                    val on = acc.enabled == true
                    Card(colors = CardDefaults.cardColors(containerColor = Card), shape = RoundedCornerShape(12.dp)) {
                        Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(10.dp).background(if (on) Green else Dim, RoundedCornerShape(5.dp)))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(acc.nickname ?: acc.username ?: "账号 ${acc.id}", color = Txt, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                Text(acc.status ?: if (on) "运行中" else "已停止", color = Dim, fontSize = 12.sp)
                            }
                            Switch(
                                checked = on,
                                onCheckedChange = { vm.toggle(acc, it) },
                                colors = SwitchDefaults.colors(checkedTrackColor = Green, uncheckedTrackColor = CardB)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------- 关键词 ----------------
@Composable
fun KeywordScreen(st: UiState, vm: MainViewModel) {
    var k by remember { mutableStateOf("") }
    var r by remember { mutableStateOf("") }
    var showAdd by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("关键词回复", color = Txt, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = { showAdd = !showAdd }) { Icon(Icons.Filled.Add, null, tint = Acc) }
        }
        if (showAdd) {
            Spacer(Modifier.height(10.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Card), shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(14.dp)) {
                    OutlinedTextField(k, { k = it }, label = { Text("关键词") }, modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Acc, unfocusedBorderColor = CardB, focusedTextColor = Txt, unfocusedTextColor = Txt))
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(r, { r = it }, label = { Text("回复内容") }, modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Acc, unfocusedBorderColor = CardB, focusedTextColor = Txt, unfocusedTextColor = Txt))
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = { if (k.isNotBlank()) { vm.addKeyword(k, r); k = ""; r = ""; showAdd = false } },
                        modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Acc)) {
                        Text("添加规则", color = Color.White)
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        if (st.keywords.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { Text("暂无关键词规则", color = Dim) }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(st.keywords) { rule ->
                    Card(colors = CardDefaults.cardColors(containerColor = Card), shape = RoundedCornerShape(12.dp)) {
                        Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(rule.keyword, color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                Text(rule.reply, color = Dim, fontSize = 13.sp, maxLines = 2)
                            }
                            IconButton(onClick = { vm.delKeyword(rule) }) { Icon(Icons.Filled.Delete, null, tint = Red) }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- 日志 ----------------
@Composable
fun LogScreen(st: UiState, vm: MainViewModel) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("实时日志", color = Txt, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = { vm.refreshAll() }) { Icon(Icons.Filled.Refresh, null, tint = Acc) }
        }
        Spacer(Modifier.height(10.dp))
        if (st.logs.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { Text("暂无日志", color = Dim) }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(st.logs) { log ->
                    Card(colors = CardDefaults.cardColors(containerColor = Card), shape = RoundedCornerShape(8.dp)) {
                        Column(Modifier.padding(10.dp)) {
                            Row {
                                Text(log.time ?: "--:--", color = Dim, fontSize = 11.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(log.level ?: "INFO", fontSize = 11.sp,
                                    color = when ((log.level ?: "").uppercase()) {
                                        "ERROR" -> Red; "WARN" -> Gold; else -> Acc })
                            }
                            Text(log.message ?: "", color = Txt, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

// ---------------- 设置 ----------------
@Composable
fun SettingScreen(st: UiState, vm: MainViewModel) {
    var base by remember(st.baseUrl) { mutableStateOf(st.baseUrl) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("设置", color = Txt, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Field("服务器地址", base, { base = it; vm.updateBase(it) }, "http://1.2.3.4:9000")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { vm.saveServer() }, Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Acc)) { Text("保存") }
            OutlinedButton(onClick = { vm.checkHealth() }, Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = if (st.serverOk) Green else Dim)) {
                Text(if (st.serverOk) "在线" else "测试")
            }
        }
        Spacer(Modifier.height(6.dp))
        InfoCard("当前用户", st.username.ifBlank { "-" }, Txt)
        InfoCard("登录状态", if (st.loggedIn) "已登录" else "未登录", if (st.loggedIn) Green else Red)
        Spacer(Modifier.height(10.dp))
        Button(onClick = { vm.logout() }, Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D1418))) {
            Text("退出登录", color = Red)
        }
    }
}
