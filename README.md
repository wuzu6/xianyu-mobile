# 闲鱼智能客服 · 手机管理端 (XianyuCS Mobile)

对标 [xianyu-auto-reply-fix](https://github.com/GuDong2003/xianyu-auto-reply-fix) 的手机端管理 App。

## 定位

上游项目是 **FastAPI + SQLite + Playwright** 的完整服务，跑在服务器/Docker 上。
本 App 是它的**手机遥控管理端**：在手机上随时查看账号状态、启停账号、
管理关键词回复、看实时日志、改服务器配置，不用开电脑浏览器。

```
┌──────────────┐   HTTP/JSON   ┌─────────────────────────┐
│  本 App      │ ────────────► │  上游 FastAPI 服务      │
│ (手机端 UI)  │ ◄──────────── │  (VPS / Docker)         │
└──────────────┘               └─────────────────────────┘
```

## 功能

| 页面 | 功能 |
|---|---|
| 登录 | 填服务器地址一键测试连接 + 登录 |
| 概览 | 账号总数/在线数/累计回复/今日回复 + 服务状态 |
| 账号 | 账号列表、启停开关、刷新 |
| 关键词 | 查看/新增/删除关键词回复规则 |
| 日志 | 实时日志流（分级着色） |
| 设置 | 服务器地址、连接测试、退出登录 |

## 技术栈

- Kotlin + Jetpack Compose（Material 3）
- OkHttp + kotlinx.serialization
- DataStore Preferences（本地配置持久化）
- MVVM（ViewModel + StateFlow）

## 编译出 APK（无需电脑）

本项目配置了 **GitHub Actions 云端编译**，你只要有 GitHub 账号即可：

1. 把整个 `xianyu-mobile` 目录上传到你自己的 GitHub 仓库
2. Push 后 Actions 自动开始编译
3. 打开仓库 → **Actions** 标签 → 最新运行 → 底部 **Artifacts**
4. 下载 `xianyu-cs-debug-apk.zip`，解压得到 `app-debug.apk`
5. 传到手机安装（需允许"未知来源"）

也可以手动触发：Actions → Build APK → Run workflow。

## 接口兼容说明

上游项目的 API 路径可能随版本变化，本 App 对登录、账号、关键词、日志
等接口做了**多路径候选自动探测**（如 `/api/auth/login`、`/api/login`、`/login`），
会依次尝试直到成功。若上游改版导致某个功能失效，改 `api/ApiClient.kt` 里的路径列表即可。

## 下载地址

默认连接 `http://<你的服务器>:9000`（Docker 部署默认端口）。
本地运行时是 `http://<你的服务器>:8090`。

## 注意

本项目仅为上游开源项目的配套管理客户端，遵循 AGPL-3.0。
