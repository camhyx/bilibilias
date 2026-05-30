package com.imcys.bilibilias.datastore

fun AppSettings.Companion.getDefaultInstance(): AppSettings = AppSettings()

fun User.Companion.getDefaultInstance(): User = User()

fun GooglePlaySettings.Companion.getDefaultInstance(): GooglePlaySettings = GooglePlaySettings()

fun AppSettings.parseFrom(bytes: ByteArray): AppSettings = AppSettings.ADAPTER.decode(bytes)

fun User.parseFrom(bytes: ByteArray): User = User.ADAPTER.decode(bytes)

fun GooglePlaySettings.parseFrom(bytes: ByteArray): GooglePlaySettings =
    GooglePlaySettings.ADAPTER.decode(bytes)

val AppSettings.agreePrivacyPolicy: AppSettings.AgreePrivacyPolicyState
    get() = agree_privacy_policy

val AppSettings.knowAboutApp: AppSettings.KnowAboutApp
    get() = know_about_app

val AppSettings.agreePrivacyPolicyValue: Int
    get() = agree_privacy_policy.value

val AppSettings.knowAboutAppValue: Int
    get() = know_about_app.value

val AppSettings.enabledRoam: Boolean
    get() = enabled_roam

val AppSettings.enabledDynamicColor: Boolean
    get() = enabled_dynamic_color

val AppSettings.homeLayoutTypesetList: List<AppSettings.HomeLayoutItem>
    get() = home_layout_typeset

val AppSettings.lastSkipUpdateVersionCode: Int
    get() = last_skip_update_version_code

val AppSettings.lastSkipUpdateVersion: String
    get() = last_skip_update_version

val AppSettings.lastBulletinContent: String
    get() = last_bulletin_content

val AppSettings.downloadUri: String
    get() = download_uri

val AppSettings.episodeListMode: AppSettings.EpisodeListMode
    get() = episode_list_mode

val AppSettings.videoNamingRule: String
    get() = video_naming_rule

val AppSettings.bangumiNamingRule: String
    get() = bangumi_naming_rule

val AppSettings.biliLineHost: String
    get() = bili_line_host

val AppSettings.useToolHistoryList: List<String>
    get() = use_tool_history

val AppSettings.enabledClipboardAutoHandling: Boolean
    get() = enabled_clipboard_auto_handling ?: false

val AppSettings.videoParsePlatform: AppSettings.VideoParsePlatform
    get() = video_parse_platform ?: AppSettings.VideoParsePlatform.Web

val AppSettings.downloadSortType: AppSettings.DownloadSortType
    get() = download_sort_type

val AppSettings.useVideoContainer: String
    get() = use_video_container

val AppSettings.useAudioContainer: String
    get() = use_audio_container

val AppSettings.navBackStack: String
    get() = nav_back_stack

val AppSettings.unknownAppSignWarningCloseTime: Long
    get() = unknown_app_sign_warning_close_time

val AppSettings.maxConcurrentDownloads: Int
    get() = max_concurrent_downloads

val AppSettings.enabledConcurrentMerge: Boolean
    get() = enabled_concurrent_merge

val AppSettings.enabledNavOnBackInvokedCallback: Boolean
    get() = enabled_nav_on_back_invoked_callback ?: false

val AppSettings.enabledNavAnimation: Boolean
    get() = enabled_nav_animation ?: false

val AppSettings.packageSourceWarningSkipKey: String
    get() = package_source_warning_skip_key

fun AppSettings.hasEnabledClipboardAutoHandling(): Boolean = enabled_clipboard_auto_handling != null

fun AppSettings.hasVideoParsePlatform(): Boolean = video_parse_platform != null

fun AppSettings.hasEnabledNavOnBackInvokedCallback(): Boolean =
    enabled_nav_on_back_invoked_callback != null

fun AppSettings.hasEnabledNavAnimation(): Boolean = enabled_nav_animation != null

val AppSettings.HomeLayoutItem.isHidden: Boolean
    get() = is_hidden

val User.currentUserId: Long
    get() = current_user_id

val User.notUseBuvid3: Boolean
    get() = not_use_buvid3

val GooglePlaySettings.usageProcessNumber: Long
    get() = usage_process_number
