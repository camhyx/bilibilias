package com.imcys.bilibilias.shared.app


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import bilibilias.shared.generated.resources.Res
import bilibilias.shared.generated.resources.account_all_invalid
import bilibilias.shared.generated.resources.account_check_invalid
import bilibilias.shared.generated.resources.app_privacy_policy_tip
import bilibilias.shared.generated.resources.cd_back
import bilibilias.shared.generated.resources.cd_go_to_bilibili
import bilibilias.shared.generated.resources.common_agree
import bilibilias.shared.generated.resources.common_i_know
import bilibilias.shared.generated.resources.common_privacy_policy
import bilibilias.shared.generated.resources.common_refuse
import bilibilias.shared.generated.resources.instructions_go_to_bilibili
import bilibilias.shared.generated.resources.instructions_i_agree
import bilibilias.shared.generated.resources.instructions_no_authorization
import bilibilias.shared.generated.resources.instructions_no_redistribution
import bilibilias.shared.generated.resources.instructions_not_bilibili
import bilibilias.shared.generated.resources.instructions_purpose
import bilibilias.shared.generated.resources.instructions_self_responsibility
import bilibilias.shared.generated.resources.instructions_title
import bilibilias.shared.generated.resources.privacy_refuse_content
import com.imcys.bilibilias.common.event.ToastEvent
import com.imcys.bilibilias.common.event.loginErrorChannel
import com.imcys.bilibilias.common.event.toastEventFlow
import com.imcys.bilibilias.common.event.sendToastEventOnBlocking
import com.imcys.bilibilias.datastore.*
import com.imcys.bilibilias.datastore.AppSettings.AgreePrivacyPolicyState.Agreed
import com.imcys.bilibilias.datastore.AppSettings.AgreePrivacyPolicyState.Refuse
import com.imcys.bilibilias.shared.navigation.BILIBILAISNavDisplay
import com.imcys.bilibilias.shared.platform.component.ASHtmlText
import com.imcys.bilibilias.shared.platform.runtime.openLink
import com.imcys.bilibilias.shared.platform.permission.rememberLegacyStoragePermissionController
import com.imcys.bilibilias.ui.utils.DialogSortBuilder
import com.imcys.bilibilias.ui.utils.DialogSortHost
import com.imcys.bilibilias.ui.component.ASAlertDialog
import com.imcys.bilibilias.ui.component.ASIconButton
import com.imcys.bilibilias.ui.component.ASTextButton
import com.imcys.bilibilias.ui.component.ASTopAppBar
import com.imcys.bilibilias.ui.component.BILIBILIASTopAppBarStyle
import com.imcys.bilibilias.shared.ui.component.Konfetti
import com.imcys.bilibilias.shared.ui.component.rememberKonfettiState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun BILIBILIASAppScreen() {
    MainScaffold()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold() {
    val konfettiState = rememberKonfettiState(false)
    val vm = koinViewModel<BILIBILIASAppViewModel>()
    val appSettings by vm.appSettings.collectAsStateWithLifecycle()
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    var showPrivacyPolicyRefuseTip by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val bottomNavHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    // 监听注册区域
    LaunchedEffect(Unit) {
        loginErrorChannel.collect {
            vm.accountLoginStateError()
        }
    }
    // 监听Toast事件
    handleToastEvent(snackbarHostState)

    // 页面注册区域
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        AnimatedContent(
            modifier = Modifier.fillMaxSize(),
            targetState = uiState,
            transitionSpec = {
                // 进入动画：淡入
                fadeIn(
                    animationSpec = tween(durationMillis = 300)
                ) togetherWith fadeOut(
                    animationSpec = tween(durationMillis = 300)
                )
            },
        ) { targetUiState ->
            when (targetUiState) {
                UIState.Default -> {
                    Surface(
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                    ) {
                        BILIBILAISNavDisplay()
                    }
                    HomeDialogHost(
                        appSettings = appSettings,
                        showPrivacyPolicyRefuseTip = showPrivacyPolicyRefuseTip,
                        onAgreePrivacyPolicy = {
                            konfettiState.value = true
                            vm.updatePrivacyPolicyAgreement(Agreed)
                        },
                        onRefusePrivacyPolicy = {
                            showPrivacyPolicyRefuseTip = true
                            vm.updatePrivacyPolicyAgreement(Refuse)
                        },
                        onDismissPrivacyPolicyRefuseTip = {
                            showPrivacyPolicyRefuseTip = false
                        }
                    )
                }

                is UIState.AccountCheck -> {
                    AccountCheckPage(targetUiState)
                }

                is UIState.KnowAboutApp -> {
                    InstructionsPage(onClickKnowAbout = vm::onKnowAboutApp)
                }
            }
        }
        Konfetti(konfettiState)

        // Snackbar 提示
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomNavHeight)
        )
    }
}

@Composable
private fun HomeDialogHost(
    appSettings: com.imcys.bilibilias.datastore.AppSettings,
    showPrivacyPolicyRefuseTip: Boolean,
    onAgreePrivacyPolicy: () -> Unit,
    onRefusePrivacyPolicy: () -> Unit,
    onDismissPrivacyPolicyRefuseTip: () -> Unit
) {
    var showLegacyStoragePermissionTip by rememberSaveable { mutableStateOf(true) }
    val legacyStoragePermissionLauncher = rememberLegacyStoragePermissionController {
        sendToastEventOnBlocking("权限未被授予")
    }
    DialogSortHost {

        dialog(
            visible = appSettings.agreePrivacyPolicyValue <= 1 &&
                    appSettings.knowAboutAppValue == 1
        ) {
            PrivacyPolicyDialog(
                onClickConfirm = onAgreePrivacyPolicy,
                onClickDismiss = onRefusePrivacyPolicy
            )
        }

        dialog(
            visible = showPrivacyPolicyRefuseTip
        ) {
            PrivacyPolicyRefuseDialog(
                onClickConfirm = onDismissPrivacyPolicyRefuseTip
            )
        }

        dialog(
            visible = showLegacyStoragePermissionTip && legacyStoragePermissionLauncher.shouldRequest
        ) {
            ASAlertDialog(
                title = { Text("权限申请") },
                text = {
                    Text("在您的设备保存下载内容需要存储权限，是否现在授权？")
                },
                confirmButton = {
                    ASTextButton(onClick = {
                        showLegacyStoragePermissionTip = false
                        legacyStoragePermissionLauncher.request()
                    }) {
                        Text(text = "确认")
                    }
                },
                dismissButton = {
                    ASTextButton(onClick = {
                        showLegacyStoragePermissionTip = false
                    }) {
                        Text(text = "暂不授权")
                    }
                }
            )
        }
    }
}

/**
 * 处理Toast事件
 */
@Composable
private fun handleToastEvent(
    snackbarHostState: SnackbarHostState,
) {
    LaunchedEffect(Unit) {
        toastEventFlow.collect { event ->
            val result = when (event) {
                is ToastEvent.ActionToastEvent -> snackbarHostState
                    .showSnackbar(
                        message = event.message,
                        actionLabel = event.actionLabel,
                        duration = event.duration
                    )

                is ToastEvent.NormalToastEvent -> snackbarHostState
                    .showSnackbar(
                        message = event.message,
                        duration = event.duration
                    )
            }
            event.onResult.invoke(result)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun InstructionsPage(onClickKnowAbout: () -> Unit = {}) {
    Scaffold(
        topBar = {
            ASTopAppBar(
                style = BILIBILIASTopAppBarStyle.Small,
                title = {
                    Text(stringResource(Res.string.instructions_title))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                navigationIcon = {
                    ASIconButton(onClick = {}) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = stringResource(Res.string.cd_back)
                        )
                    }
                },
                actions = {}
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = CardDefaults.shape
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        item {
                            Text(
                                stringResource(Res.string.instructions_not_bilibili),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        item {
                            Text(
                                stringResource(Res.string.instructions_no_authorization),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        item {
                            Text(
                                stringResource(Res.string.instructions_purpose),
                            )
                        }

                        item {
                            Text(
                                stringResource(Res.string.instructions_no_redistribution),

                                )
                        }
                        item {
                            Text(
                                stringResource(Res.string.instructions_self_responsibility),
                            )
                        }
                        item {
                            Text(
                                """
                                    注意：该程序由非开源仓库开发者打包而来，这是因为本项目已经停止分发，正常情况下，你不可能在任何地方获得本项目开发者打包的安装包，除非你是学习本项目的开发者，否则你目前使用的APP有可能遭遇篡改。
                                """.trimIndent(),
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Button(
                onClick = {
                    openLink("https://bilibili.com")
                },
                Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            ) {

                Text(stringResource(Res.string.instructions_go_to_bilibili))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Icon(
                    Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = stringResource(Res.string.cd_go_to_bilibili),
                )

            }
            Button(onClick = onClickKnowAbout, Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.instructions_i_agree))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AccountCheckPage(targetUiState: UIState.AccountCheck) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (targetUiState.isCheckLoading) {
            ContainedLoadingIndicator()
            Spacer(Modifier.height(5.dp))
            Text(stringResource(Res.string.account_check_invalid))
        } else {
            if (targetUiState.newCurrentUser == null) {
                Text(stringResource(Res.string.account_all_invalid))
            }
        }
    }
}

/**
 * 隐私政策对话框
 */
@Composable
fun PrivacyPolicyDialog(
    showState: Boolean,
    onClickConfirm: () -> Unit,
    onClickDismiss: () -> Unit,
) {
    ASAlertDialog(
        showState = showState,
        title = { Text(text = stringResource(Res.string.common_privacy_policy)) },
        icon = {
            Icon(
                imageVector = Icons.Outlined.PrivacyTip,
                contentDescription = stringResource(Res.string.common_privacy_policy)
            )
        },
        text = {
            ASHtmlText(
                stringResource(Res.string.app_privacy_policy_tip).trimIndent(),
                onLinkClick = { openLink(it) })
        },
        confirmButton = {
            ASTextButton(onClick = onClickConfirm) {
                Text(text = stringResource(Res.string.common_agree))
            }
        },
        dismissButton = {
            ASTextButton(onClick = onClickDismiss) {
                Text(text = stringResource(Res.string.common_refuse))
            }
        }
    )
}

context(_: DialogSortBuilder)
@Composable
fun PrivacyPolicyDialog(
    onClickConfirm: () -> Unit,
    onClickDismiss: () -> Unit,
) {
    PrivacyPolicyDialog(true, onClickConfirm, onClickDismiss)
}


/**
 * 隐私政策拒绝后提示弹窗
 */
@Composable
fun PrivacyPolicyRefuseDialog(
    showState: Boolean,
    onClickConfirm: () -> Unit,
) {
    ASAlertDialog(
        showState = showState,
        title = { Text(text = stringResource(Res.string.common_privacy_policy)) },
        icon = {
            Icon(
                imageVector = Icons.Outlined.PrivacyTip,
                contentDescription = stringResource(Res.string.common_privacy_policy)
            )
        },
        text = {
            ASHtmlText(
                stringResource(Res.string.privacy_refuse_content).trimIndent(),
                onLinkClick = { openLink(it) })
        },
        confirmButton = {
            ASTextButton(onClick = onClickConfirm) {
                Text(text = stringResource(Res.string.common_i_know))
            }
        },
    )
}

/**
 * 隐私政策拒绝后提示弹窗
 */
context(_: DialogSortBuilder)
@Composable
fun PrivacyPolicyRefuseDialog(
    onClickConfirm: () -> Unit,
) {
    PrivacyPolicyRefuseDialog(true, onClickConfirm)
}