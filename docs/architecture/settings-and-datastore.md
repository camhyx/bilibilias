# 设置与 DataStore 设计

本文档说明 BILIBILIAS 当前 DataStore / protobuf 设置体系的职责边界、字段影响范围，以及修改设置相关能力时需要同步触达的层次。内容以 `core:datastore-proto`、`core:datastore`、`core:data` 和 `:app` 中的实际调用为准。

## 总览

当前仓库并不是“每个页面自己存一点偏好”，而是由三份 protobuf 数据承载主要本地设置状态。当前这套设置存储已经按 Kotlin Multiplatform 组织，使用 `androidx.datastore:datastore-core` / `datastore-core-okio` 负责存储，用 Wire 负责生成跨平台 protobuf Kotlin 类型。

- `AppSettings`
  - 面向全局应用行为、UI 偏好、下载配置、导航恢复和隐私同意。
- `User`
  - 面向当前登录用户标识和少量账号相关网络开关。
- `GooglePlaySettings`
  - 面向 Google Play 渠道流程统计等轻量状态。

proto 定义位于：

- `core/datastore-proto/src/commonMain/proto/com/imcys/bilibilias/datastore/settings.proto`
- `core/datastore-proto/src/commonMain/proto/com/imcys/bilibilias/datastore/user.proto`
- `core/datastore-proto/src/commonMain/proto/com/imcys/bilibilias/datastore/google_play.proto`

## 分层职责

### `core:datastore-proto`

负责：

- Wire protobuf message 定义
- 字段编号和向后兼容约束
- 生成供上层使用的 Kotlin 类型
- 通过 `WireCompat.kt` 提供旧 protobuf-lite 调用到 Wire API 的兼容扩展，例如 `toBuilder()`、`copy {}`、camelCase 访问器等

任何新增设置项，第一步都应从 proto 开始，而不是先在 repository 或 UI 层“临时塞一个字段”。

### `core:datastore`

负责：

- `DataStore<T>` 实例定义
- 基于 Okio 的 `Serializer` 实现
- 读取旧数据时的默认值补齐和兼容修正
- 更贴近存储层的 source
- Android / iOS 各自的数据文件路径实现

其中最关键的是 `AppSettingsSerializer`。它不仅负责序列化，还承担历史字段缺省值修复、非法值回填、默认容器格式兜底、并发下载配置修正等兼容逻辑。

### `core:data`

负责：

- `AppSettingsRepository` 等 repository 封装
- 向 UI 暴露更稳定的读写接口
- 维护跨字段约束，例如“关闭导航动画时，同时关闭预测性返回动画”“最大并发下载数小于等于 1 时，关闭并发合并”

推荐调用方向：

`UI / ViewModel -> Repository -> DataStore`

不要让页面直接操作 proto builder 或 serializer。

## `AppSettings` 负责什么

`AppSettings` 是当前最核心的本地设置来源，主要覆盖以下几类能力。

### 隐私与首次使用

- `agree_privacy_policy`
  - 控制用户是否已同意隐私政策。
  - 实际会影响 `MainActivity` 中百度统计、Firebase 等能力是否真正启用。
- `know_about_app`
  - 控制首次介绍、认知引导等状态。

### 首页与通用 UI 偏好

- `enabled_dynamic_color`
  - 控制动态取色主题。
- `home_layout_typeset`
  - 控制首页各区块顺序和隐藏状态。
- `use_tool_history`
  - 记录最近使用工具。

### 解析与导航

- `video_parse_platform`
  - 决定 Web / TV / Mobile 解析平台。
  - 会影响 `AutoBILIInfoPlugin`、登录平台切换和部分网络头策略。
- `nav_back_stack`
  - 保存 Navigation 3 back stack，用于主页面恢复。
- `enabled_nav_on_back_invoked_callback`
  - 控制预测性返回手势能力。
- `enabled_nav_animation`
  - 控制导航动画总开关。

### 下载与媒体行为

- `download_uri`
  - 持久化 SAF 下载目录。
- `video_naming_rule` / `bangumi_naming_rule`
  - 控制下载命名规则。
- `use_video_container` / `use_audio_container`
  - 控制默认媒体容器。
- `download_sort_type`
  - 控制下载页排序。
- `max_concurrent_downloads`
  - 控制并行下载数。
- `enabled_concurrent_merge`
  - 控制是否按下载并发数启用 FFmpeg 并发合并。

### 杂项行为

- `enabled_roam`
  - 控制漫游能力启用。
- `enabled_clipboard_auto_handling`
  - 控制剪贴板自动识别处理。
- `bili_line_host`
  - 自定义线路配置。
- `last_skip_update_version_code` / `last_skip_update_version`
  - 更新跳过状态。
- `last_bulletin_content`
  - 公告忽略状态。
- `unknown_app_sign_warning_close_time`
  - 签名告警关闭时间。
- `package_source_warning_skip_key`
  - 记录“此版本不再提示”的版本号 + git hash 组合 key。

## `User` 与 `GooglePlaySettings`

### `User`

当前字段很少，但它不等于“用户资料详情缓存”，而是：

- `current_user_id`
  - 当前登录用户标识。
- `not_use_buvid3`
  - 网络层请求时是否过滤 `buvid3`。

它会影响 `AsCookiesStorage` 的行为，因此虽然字段少，仍属于网络链路关键状态。

### `GooglePlaySettings`

当前只承载：

- `usage_process_number`
  - 整个 App 流程使用次数统计。

这类字段不要混进 `AppSettings`，否则会把渠道特定状态和全局设置耦合到一起。

## 默认值与兼容修正

`AppSettingsSerializer` 中的 `appSettingsDefault` 定义了当前仓库的事实默认值，例如：

- 默认视频命名规则：`{p_title}`
- 默认番剧命名规则：`{episode_title}`
- 默认解析平台：`Web`
- 默认视频容器：`mp4`
- 默认音频容器：`m4a`
- 默认最大并发下载数：`1`
- 默认并发合并：`false`
- 默认预测性返回：`true`
- 默认导航动画：`true`

读取旧数据时还会做几类兼容修正：

- 空命名规则回填默认值
- 空工具历史回填默认值
- 老数据缺失 optional 字段时补默认值
- 非法媒体容器值回退到默认值
- 小于等于 0 的并发下载数回退到默认值
- 并发下载数小于等于 1 时自动关闭并发合并

这意味着：新增字段后，如果需要兼容历史数据，通常不只改 proto，还要评估 serializer 是否需要补迁移逻辑。

## 修改设置项的标准路径

新增或修改 `AppSettings` 字段时，建议按这个顺序检查：

1. 修改 `core:datastore-proto` 中的 proto。
2. 评估 `core:datastore` 的 serializer 默认值和兼容修正是否需要同步调整。
3. 在 `core:data` 的 repository 中补读写接口和跨字段约束。
4. 更新 `:app` 中对应 ViewModel / UI / 导航 / 下载逻辑。
5. 如果字段会影响构建、隐私、下载或导航行为，同步更新对应 `docs/` 文档。

## 什么时候不该放进 `AppSettings`

以下场景通常不适合直接塞进 `AppSettings`：

- 仅服务单一渠道或单一平台的临时状态
- 更适合和账号绑定的网络开关
- 需要结构化查询、排序、关联关系的持久化数据

对应建议：

- 账号状态优先考虑 `User` 或数据库用户表
- 渠道状态优先考虑 `GooglePlaySettings`
- 复杂持久化结构优先考虑 Room
