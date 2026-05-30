package com.imcys.bilibilias.data.repository

import androidx.datastore.core.DataStore
import com.imcys.bilibilias.database.entity.LoginPlatform
import com.imcys.bilibilias.database.entity.download.MediaContainer
import com.imcys.bilibilias.datastore.*
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AppSettingsRepository(
    private val dataStore: DataStore<AppSettings>,
) {
    private val TAG: String = "AppSettingsRepository"

    @NativeCoroutines
    val appSettingsFlow: Flow<AppSettings> = dataStore.data

    // 获取当前平台类型
    @NativeCoroutines
    suspend fun getVideoParsePlatform(): AppSettings.VideoParsePlatform =
        appSettingsFlow.first().videoParsePlatform

    // 同意了隐私政策
    suspend fun hasAgreedPrivacyPolicy(): Boolean {
        val currentSettings = dataStore.data.first()
        return currentSettings.agreePrivacyPolicy == AppSettings.AgreePrivacyPolicyState.Agreed
    }

    // 预测返回手势启用
    suspend fun hasEnabledOnBackInvokedCallback(): Boolean {
        val currentSettings = dataStore.data.first()
        return currentSettings.enabledNavOnBackInvokedCallback
    }

    // 添加更新隐私政策同意状态的方法
    suspend fun updatePrivacyPolicyAgreement(agreed: AppSettings.AgreePrivacyPolicyState) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(agree_privacy_policy = agreed)
        }
    }


    // 添加更新隐私政策同意状态的方法
    suspend fun updateKnowAboutApp(knowAboutApp: AppSettings.KnowAboutApp) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(know_about_app = knowAboutApp)
        }
    }

    suspend fun updateRoamEnabledState(enabled: Boolean) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(enabled_roam = enabled)
        }
    }

    suspend fun updateEnabledDynamicColor(enabled: Boolean) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(enabled_dynamic_color = enabled)
        }
    }


    suspend fun updateEnabledOnBackInvokedCallback(enabled: Boolean) {
        dataStore.updateData { currentSettings ->
            if (!currentSettings.enabledNavAnimation) currentSettings
            else currentSettings.copy(enabled_nav_on_back_invoked_callback = enabled)
        }
    }

    suspend fun updateEnabledNavAnimation(enabled: Boolean) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(
                enabled_nav_on_back_invoked_callback = if (!enabled) false else currentSettings.enabled_nav_on_back_invoked_callback,
                enabled_nav_animation = enabled,
            )
        }
    }

    suspend fun updateClipboardAutoHandling(enabled: Boolean) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(enabled_clipboard_auto_handling = enabled)
        }
    }

    suspend fun updateLastSkipUpdateVersionCode(versionCode: Int) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(last_skip_update_version_code = versionCode)
        }
    }

    suspend fun asyncHomeLayoutTypesetList(): List<AppSettings.HomeLayoutItem> {
        val defaultList = createDefaultHomeLayoutItems()
        val existingList = dataStore.data.first().homeLayoutTypesetList.toMutableList()

        return if (existingList.isEmpty()) {
            dataStore.updateData { currentSettings ->
                currentSettings.copy(home_layout_typeset = defaultList)
            }
            defaultList
        } else {
            val existingTypes = existingList.map { it.type }.toSet()
            val missingItems = defaultList.filterNot { it.type in existingTypes }
            existingList.addAll(missingItems)
            existingList
        }
    }

    private fun createDefaultHomeLayoutItems(): List<AppSettings.HomeLayoutItem> {
        val defaultTypes = listOf(
            AppSettings.HomeLayoutType.Banner,
            AppSettings.HomeLayoutType.Announcement,
            AppSettings.HomeLayoutType.UpdateInfo,
            AppSettings.HomeLayoutType.Tools,
            AppSettings.HomeLayoutType.DownloadList
        )

        return defaultTypes.map { type ->
            AppSettings.HomeLayoutItem(type = type, is_hidden = false)
        }
    }

    suspend fun updateHomeLayoutTypesetList(newList: List<AppSettings.HomeLayoutItem>) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(home_layout_typeset = newList)
        }
    }


    suspend fun updateLastBulletinContent(content: String) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(last_bulletin_content = content)
        }
    }

    suspend fun saveDownloadSAFUriString(uriString: String) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(download_uri = uriString)
        }
    }

    suspend fun updateEpisodeListMode(it: AppSettings.EpisodeListMode) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(episode_list_mode = it)
        }
    }

    suspend fun updateVideoNamingRule(rule: String) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(video_naming_rule = rule)
        }
    }

    suspend fun updateBangumiNamingRule(rule: String) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(bangumi_naming_rule = rule)
        }
    }

    suspend fun updateLineHost(lineHost: String) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(bili_line_host = lineHost)
        }
    }

    // 存储使用工具记录
    suspend fun updateUseToolRecord(toolName: String) {
        dataStore.updateData { currentSettings ->
            val historyList = currentSettings.useToolHistoryList.toMutableList()
            if (historyList.size > 10) {
                historyList.removeLastOrNull()
            }
            historyList.add(0, toolName)
            // 去重
            val distinctList = historyList.distinct()
            currentSettings.copy(use_tool_history = distinctList)
        }
    }

    suspend fun updateVideoParsePlatform(platform: AppSettings.VideoParsePlatform) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(video_parse_platform = platform)
        }
    }

    suspend fun updateDownloadSortType(sortType: AppSettings.DownloadSortType) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(download_sort_type = sortType)
        }
    }

    suspend fun updateUseVideoContainer(
        videoContainer: MediaContainer
    ) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(use_video_container = videoContainer.extension)
        }
    }

    suspend fun updateUseAudioContainer(
        audioContainer: MediaContainer
    ) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(use_audio_container = audioContainer.extension)
        }
    }

    fun storeMediaContainerFromExtension(extension: String): MediaContainer {
        return  MediaContainer.entries.first { it.extension == extension }
    }

    suspend fun updateNavBackStack(navBackStackStr: String)  {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(nav_back_stack = navBackStackStr)
        }
    }

    suspend fun updateUnknownAppSignWarningCloseTime(closeTime: Long) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(unknown_app_sign_warning_close_time = closeTime)
        }
    }

    suspend fun updateMaxConcurrentDownloads(maxConcurrentDownloads: Int) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(
                max_concurrent_downloads = maxConcurrentDownloads,
                enabled_concurrent_merge = if (maxConcurrentDownloads <= 1) false else currentSettings.enabled_concurrent_merge,
            )
        }
    }

    suspend fun updateEnabledConcurrentMerge(enabled: Boolean) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(
                enabled_concurrent_merge = enabled && currentSettings.maxConcurrentDownloads > 1
            )
        }
    }

    suspend fun updatePackageSourceWarningSkipKey(key: String) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(package_source_warning_skip_key = key)
        }
    }
}


fun AppSettings.VideoParsePlatform.getDescription(): String = this.name

fun AppSettings.VideoParsePlatform.toDatabaseType(): LoginPlatform = when (this) {
    AppSettings.VideoParsePlatform.Web -> LoginPlatform.WEB
    AppSettings.VideoParsePlatform.TV -> LoginPlatform.TV
    AppSettings.VideoParsePlatform.Mobile -> LoginPlatform.MOBILE
    else -> LoginPlatform.WEB
}

fun LoginPlatform.toDataStoreType() = when (this) {
    LoginPlatform.WEB -> AppSettings.VideoParsePlatform.Web
    LoginPlatform.MOBILE -> AppSettings.VideoParsePlatform.Mobile
    LoginPlatform.TV -> AppSettings.VideoParsePlatform.TV
}


fun AppSettings.HomeLayoutType.getDescription(): String = when (this) {
    AppSettings.HomeLayoutType.Banner -> "轮播图"
    AppSettings.HomeLayoutType.Announcement -> "公告信息"
    AppSettings.HomeLayoutType.UpdateInfo -> "更新信息"
    AppSettings.HomeLayoutType.Tools -> "工具列表"
    AppSettings.HomeLayoutType.DownloadList -> "下载列表"
    else -> this.name
}
