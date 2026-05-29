package com.imcys.bilibilias

import android.app.Application
import android.os.StrictMode
import androidx.appfunctions.service.AppFunctionConfiguration
import com.baidu.mobstat.StatService
import com.imcys.bilibilias.agent.functions.BILIAnalysisAppFunctions
import com.imcys.bilibilias.common.data.CommonBuildConfig
import com.imcys.bilibilias.common.memory.FairMemoryReceiver
import com.imcys.bilibilias.common.utils.baiduAnalyticsSafe
import com.imcys.bilibilias.data.repository.AppSettingsRepository
import com.imcys.bilibilias.datastore.enabledConcurrentMerge
import com.imcys.bilibilias.datastore.maxConcurrentDownloads
import com.imcys.bilibilias.di.androidPlatformKoinModules
import com.imcys.bilibilias.download.FfmpegRuntimeConfig
import com.imcys.bilibilias.download.NewDownloadManager
import com.imcys.bilibilias.shared.download.runtime.SharedDownloadManager
import com.imcys.bilibilias.shared.di.sharedKoinModules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class BILIBILIASApplication : Application(), AppFunctionConfiguration.Provider {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var fairMemoryReceiver: FairMemoryReceiver? = null

    override fun onCreate() {
        super.onCreate()
        // 启动性能检测
        startStrictMode()
        // 配置初始化
        initBuildConfig()
        // 初始化百度统计
        baiduAnalyticsSafe {
            StatService.init(this, BuildConfig.BAIDU_STAT_ID, getString(R.string.app_channel))
        }
        // Koin依赖注入
        startKoin {
            if (BuildConfig.DEBUG) {
                androidLogger()
            }
            androidContext(this@BILIBILIASApplication)
            modules(sharedKoinModules() + androidPlatformKoinModules())
        }
        // 监听公平运行内存调度
        bindFairMemoryReceiver()
        // FFmpeg初始化
        initFFmpegAsync()
        // shizuku监听
        // shizukuStateManager.start()
    }

    private fun bindFairMemoryReceiver() {
        applicationScope.launch {
            fairMemoryReceiver = FairMemoryReceiver(this@BILIBILIASApplication) {
                getKoin().get<SharedDownloadManager>() as NewDownloadManager
            }.also { receiver ->
                receiver.initialize()
            }
        }
    }

    private fun startStrictMode() {
        if (!BuildConfig.DEBUG) return
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectCustomSlowCalls() // 配合 StrictMode.noteSlowCall 使用
                .detectDiskReads() // 检查主线程磁盘读取
                .detectDiskWrites() // 检查主线程磁盘写入
                .detectNetwork() // 检查主线程网络请求
                .penaltyLog() // 在 Logcat 输出违规信息
                .build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectActivityLeaks() // 检查 Activity 泄漏
                .detectLeakedSqlLiteObjects() // 检查数据库对象未关闭
                .detectLeakedClosableObjects() // 检查 Closeable 对象未关闭
                .detectLeakedRegistrationObjects() // 检查注册对象未释放
                .penaltyLog() // 在 Logcat 输出违规信息
                .build()
        )
    }

    private fun initFFmpegAsync() {
        applicationScope.launch {
            val settingsRepository = getKoin().get<AppSettingsRepository>()
            val settings = settingsRepository.appSettingsFlow.first()
            FfmpegRuntimeConfig.apply(
                maxConcurrentDownloads = settings.maxConcurrentDownloads,
                enabledConcurrentMerge = settings.enabledConcurrentMerge
            )
        }
    }

    /**
     * 主动内存释放
     */
    @Suppress("DEPRECATION")
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when {
            level == TRIM_MEMORY_UI_HIDDEN -> {
                fairMemoryReceiver?.trimMemory(clearTemporaryFiles = false)
            }

            level >= TRIM_MEMORY_RUNNING_LOW -> {
                fairMemoryReceiver?.trimMemory(clearTemporaryFiles = true)
            }
        }
    }

    override val appFunctionConfiguration: AppFunctionConfiguration
        get() = AppFunctionConfiguration
            .Builder()
            .addEnclosingClassFactory(BILIAnalysisAppFunctions::class.java) {
                getKoin().get<BILIAnalysisAppFunctions>()
            }
            .build()

    private fun initBuildConfig() {
        CommonBuildConfig.enabledAnalytics = BuildConfig.ENABLED_ANALYTICS
        CommonBuildConfig.gitCommitHash = BuildConfig.GIT_COMMIT_HASH
        CommonBuildConfig.enabledNetworkLogging = BuildConfig.DEBUG
    }
}
