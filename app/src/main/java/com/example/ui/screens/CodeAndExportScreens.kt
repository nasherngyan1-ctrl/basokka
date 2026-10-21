package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GitHubAccountState
import com.example.data.GitHubDeployResult
import com.example.data.GitHubDeployService
import com.example.data.PlatformType
import com.example.data.ProjectFile
import com.example.ui.theme.BasoBgDark
import com.example.ui.theme.BasoBorderCyan
import com.example.ui.theme.BasoCardElevated
import com.example.ui.theme.BasoCardSurface
import com.example.ui.theme.BasoCyan
import com.example.ui.theme.BasoDarkOnAccent
import com.example.ui.theme.BasoGold
import com.example.ui.theme.BasoSuccessGreen
import com.example.ui.theme.BasoTextMuted
import com.example.ui.theme.BasoTextWhite
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun CodeExplorerSection(
    isKurdish: Boolean,
    codeFilterTab: String,
    projectFiles: List<ProjectFile>,
    selectedFile: ProjectFile?,
    onSelectCodeFilterTab: (String) -> Unit,
    onSelectFile: (ProjectFile) -> Unit,
    onOpenZipBuildScreen: () -> Unit
) {
    val context = LocalContext.current
    val activeFile = selectedFile ?: projectFiles.firstOrNull()

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Code Category Tabs matching Screen 6: Android | Web | Files
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Android", "Web", "Files").forEach { tab ->
                val isSelected = codeFilterTab == tab
                Surface(
                    color = if (isSelected) BasoCardElevated else BasoCardSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) BasoCyan else BasoBorderCyan
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onSelectCodeFilterTab(tab)
                            val matching = projectFiles.firstOrNull { it.platformCategory == tab }
                                ?: projectFiles.firstOrNull()
                            if (matching != null) onSelectFile(matching)
                        }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = tab,
                            color = if (isSelected) BasoTextWhite else BasoTextMuted,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Split File Tree (Left) + Syntax-Highlighted Code Viewer (Right) matching Screen 6
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF07131E)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BasoBorderCyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Pane: Folder & File Tree (web-app -> src -> components / pages / assets)
                    Column(
                        modifier = Modifier
                            .width(138.dp)
                            .fillMaxHeight()
                            .background(Color(0xFF091724))
                            .padding(vertical = 10.dp, horizontal = 8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        FolderTreeHeader("web-app", indent = 0)
                        FolderTreeHeader("src", indent = 1)
                        FolderTreeHeader("components", indent = 2)

                        projectFiles.filter { it.folder == "src/components" }.forEach { f ->
                            FileTreeItemRow(
                                file = f,
                                isSelected = activeFile?.path == f.path,
                                indent = 3,
                                onClick = { onSelectFile(f) }
                            )
                        }

                        FolderTreeHeader("pages", indent = 2)
                        projectFiles.filter { it.folder == "src/pages" }.forEach { f ->
                            FileTreeItemRow(
                                file = f,
                                isSelected = activeFile?.path == f.path,
                                indent = 3,
                                onClick = { onSelectFile(f) }
                            )
                        }

                        FolderTreeHeader("assets", indent = 1)
                        projectFiles.filter { it.folder == "assets" }.forEach { f ->
                            FileTreeItemRow(
                                file = f,
                                isSelected = activeFile?.path == f.path,
                                indent = 2,
                                onClick = { onSelectFile(f) }
                            )
                        }

                        projectFiles.filter { it.folder == "web-app" || it.folder == "android/ui" }.forEach { f ->
                            FileTreeItemRow(
                                file = f,
                                isSelected = activeFile?.path == f.path,
                                indent = 1,
                                onClick = { onSelectFile(f) }
                            )
                        }
                    }

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(BasoBorderCyan)
                    )

                    // Right Pane: Line-numbered Syntax Highlighted Code
                    val codeLines = (activeFile?.content ?: "").lines()
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState())
                    ) {
                        codeLines.forEachIndexed { idx, line ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.padding(vertical = 1.5.dp)
                            ) {
                                Text(
                                    text = "${idx + 1}".padStart(2, ' '),
                                    color = BasoTextMuted.copy(alpha = 0.6f),
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.sp,
                                    modifier = Modifier.width(24.dp)
                                )
                                Text(
                                    text = highlightSyntaxLine(line),
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom Action Buttons: کۆپی کۆد | دابەزاندنی APK & کۆنێکتی GitHub
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(
                        ClipData.newPlainText("BASO Code", activeFile?.content ?: "")
                    )
                    Toast.makeText(
                        context,
                        if (isKurdish) "کۆدەکە کۆپی کرا!" else "Code copied to clipboard!",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                border = BorderStroke(1.dp, BasoCyan.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = BasoCardSurface,
                    contentColor = BasoTextWhite
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("copy_code_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    tint = BasoCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isKurdish) "کۆپی کۆد" else "Copy Code",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onOpenZipBuildScreen,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BasoGold,
                    contentColor = BasoDarkOnAccent
                ),
                modifier = Modifier
                    .weight(1.25f)
                    .height(48.dp)
                    .testTag("export_zip_from_code_button")
            ) {
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isKurdish) "APK + GitHub + ZIP" else "APK + GitHub + ZIP",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun FolderTreeHeader(name: String, indent: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = (indent * 8).dp, top = 4.dp, bottom = 3.dp)
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = BasoTextMuted,
            modifier = Modifier.size(13.dp)
        )
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            tint = BasoGold,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = name,
            color = BasoTextWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FileTreeItemRow(
    file: ProjectFile,
    isSelected: Boolean,
    indent: Int,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) BasoCardElevated else Color.Transparent)
            .clickable { onClick() }
            .padding(start = (indent * 8).dp, top = 4.dp, bottom = 4.dp, end = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = null,
            tint = if (isSelected) BasoCyan else BasoTextMuted,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = file.name,
            color = if (isSelected) BasoCyan else BasoTextWhite.copy(alpha = 0.85f),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun highlightSyntaxLine(line: String) = buildAnnotatedString {
    val trimmed = line.trim()
    when {
        trimmed.startsWith("<!DOCTYPE") || trimmed.startsWith("<html") || trimmed.startsWith("</html") ||
            trimmed.startsWith("<head") || trimmed.startsWith("</head") ||
            trimmed.startsWith("<body") || trimmed.startsWith("</body") -> {
            withStyle(SpanStyle(color = Color(0xFF00E5CC), fontWeight = FontWeight.Bold)) {
                append(line)
            }
        }
        trimmed.startsWith("<meta") || trimmed.startsWith("<title") ||
            trimmed.startsWith("<link") || trimmed.startsWith("<div") ||
            trimmed.startsWith("<script") -> {
            withStyle(SpanStyle(color = Color(0xFF60A5FA))) {
                append(line)
            }
        }
        trimmed.startsWith("import ") || trimmed.startsWith("export ") ||
            trimmed.startsWith("package ") || trimmed.startsWith("class ") ||
            trimmed.startsWith("fun ") -> {
            withStyle(SpanStyle(color = Color(0xFFF5C518), fontWeight = FontWeight.SemiBold)) {
                append(line)
            }
        }
        else -> {
            withStyle(SpanStyle(color = Color(0xFFE2E8F0))) {
                append(line)
            }
        }
    }
}

@Composable
fun ProjectFilesOverviewSection(
    isKurdish: Boolean,
    projectFiles: List<ProjectFile>,
    onSelectFileToInspect: (ProjectFile) -> Unit,
    onOpenZipBuildScreen: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(projectFiles) { file ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = BasoCardSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BasoBorderCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectFileToInspect(file) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = BasoCyan
                            )
                            Column {
                                Text(
                                    text = file.name,
                                    color = BasoTextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = file.path,
                                    color = BasoTextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Text(
                            text = file.platformCategory,
                            color = BasoGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onOpenZipBuildScreen,
            colors = ButtonDefaults.buttonColors(
                containerColor = BasoGold,
                contentColor = BasoDarkOnAccent
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("open_apk_github_zip_center_button")
        ) {
            Icon(imageVector = Icons.Default.RocketLaunch, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isKurdish) {
                    "📲 دابەزاندنی APK + کۆنێکتی ڕاستەوخۆی GitHub / وێبسایت"
                } else {
                    "📲 Download APK + Direct GitHub Website Deploy"
                },
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun BuildSuccessZipScreen(
    isKurdish: Boolean,
    zipFileName: String,
    zipSizeLabel: String,
    selectedPlatform: PlatformType = PlatformType.ANDROID,
    projectTitle: String = "My Project",
    gitHubAccountState: GitHubAccountState = GitHubAccountState(),
    gitHubDeployResult: GitHubDeployResult? = null,
    isDeployingToGitHub: Boolean = false,
    gitHubDeployProgressText: String? = null,
    onConnectGitHubToken: (String) -> Unit = {},
    onDisconnectGitHub: () -> Unit = {},
    onDeployToGitHub: () -> Unit = {},
    onDownloadLatestApk: () -> Unit = {},
    onDownloadStandaloneApp: () -> Unit = {},
    onBack: () -> Unit,
    onDownloadZip: () -> Unit,
    onViewFiles: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var tokenInput by remember { mutableStateOf("") }
    var showTokenInputBox by remember { mutableStateOf(!gitHubAccountState.isConnected) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BasoBgDark)
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("build_success_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = BasoTextWhite
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isKurdish) "ناوەندی APK و کۆنێکتی ڕاستەوخۆی GitHub" else "APK & Direct GitHub Deploy",
                    color = BasoTextWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "$projectTitle • ${selectedPlatform.labelEn}",
                    color = BasoCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.width(42.dp))
        }

        // 3 Completed Steps with Green Checkmarks
        val steps = if (isKurdish) {
            listOf(
                "پڕۆژەی ئەپ و وێبسایتەکەت بە تەواوی ئامادەیە",
                "فایلی دامەزراندنی APK و GitHub Actions ئامادە کرا",
                "کۆنێکتی ڕاستەوخۆی وێبسایت (GitHub Pages) ئامادەیە"
            )
        } else {
            listOf(
                "App & Website Project Generated",
                "APK Builder & GitHub Actions Configured",
                "Direct GitHub Pages Live Website Ready"
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = BasoCardSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BasoBorderCyan),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                steps.forEach { stepText ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = BasoSuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stepText,
                            color = BasoTextWhite,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // =========================================================================
        // CARD 1: DIRECT GITHUB ACCOUNT CONNECTION (Personal Access Token)
        // =========================================================================
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF081726)),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(
                1.5.dp,
                if (gitHubAccountState.isConnected) BasoSuccessGreen else BasoGold
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (gitHubAccountState.isConnected) {
                                        BasoSuccessGreen.copy(alpha = 0.16f)
                                    } else {
                                        BasoGold.copy(alpha = 0.16f)
                                    }
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = if (gitHubAccountState.isConnected) BasoSuccessGreen else BasoGold
                            )
                        }
                        Column {
                            Text(
                                text = if (isKurdish) "کۆنێکتی ڕاستەوخۆ بە هەژماری GitHub" else "Direct GitHub Account Connect",
                                color = BasoTextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (gitHubAccountState.isConnected) {
                                    "@${gitHubAccountState.username} (${gitHubAccountState.tokenMasked})"
                                } else {
                                    if (isKurdish) "بۆ بڵاوکردنەوەی وێبسایت و دروستکردنی APK لە کڵاود" else "For Live GitHub Pages & Cloud APK Build"
                                },
                                color = if (gitHubAccountState.isConnected) BasoSuccessGreen else BasoTextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Surface(
                        color = BasoCardElevated,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BasoBorderCyan),
                        modifier = Modifier.clickable {
                            showTokenInputBox = !showTokenInputBox
                        }
                    ) {
                        Text(
                            text = if (gitHubAccountState.isConnected) {
                                if (isKurdish) "گۆڕینی تۆکن" else "Change Token"
                            } else {
                                if (isKurdish) "دانانی تۆکن" else "Add Token"
                            },
                            color = BasoCyan,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                if (showTokenInputBox || !gitHubAccountState.isConnected) {
                    Text(
                        text = if (isKurdish) {
                            "تۆکنی GitHub (Personal Access Token — ghp_... یان github_pat_...) لێرە دابنێ تا BASO Studio ڕاستەوخۆ ڕیپۆزیتۆریت بۆ دروست بکات، وێبسایتەکەت بڵاوبکاتەوە و APK دروست بکات:"
                        } else {
                            "Paste your GitHub Personal Access Token (ghp_... or github_pat_...) with 'repo' & 'workflow' scopes:"
                        },
                        color = BasoTextMuted,
                        fontSize = 12.sp
                    )

                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        OutlinedTextField(
                            value = tokenInput,
                            onValueChange = { tokenInput = it },
                            placeholder = {
                                Text(
                                    text = "ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
                                    color = BasoTextMuted.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BasoCyan,
                                unfocusedBorderColor = BasoBorderCyan,
                                focusedTextColor = BasoTextWhite,
                                unfocusedTextColor = BasoTextWhite,
                                focusedContainerColor = BasoCardSurface,
                                unfocusedContainerColor = BasoCardSurface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("github_token_input")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (tokenInput.isNotBlank()) {
                                    onConnectGitHubToken(tokenInput)
                                    tokenInput = ""
                                    showTokenInputBox = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BasoCyan,
                                contentColor = BasoDarkOnAccent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("connect_github_token_button")
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isKurdish) "کۆنێکتکردنی GitHub" else "Connect GitHub",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                GitHubDeployService.openUrlInBrowser(
                                    context,
                                    "https://github.com/settings/tokens/new?scopes=repo,workflow&description=BASO_AI_Studio"
                                )
                            },
                            border = BorderStroke(1.dp, BasoGold),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = BasoCardSurface,
                                contentColor = BasoGold
                            ),
                            modifier = Modifier.height(46.dp)
                        ) {
                            Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isKurdish) "وەرگرتنی تۆکن" else "Get Token",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (gitHubAccountState.isConnected) {
                        OutlinedButton(
                            onClick = onDisconnectGitHub,
                            border = BorderStroke(1.dp, Color(0xFFEF4444)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isKurdish) "پچڕاندنی پەیوەندی GitHub" else "Disconnect GitHub",
                                color = Color(0xFFEF4444),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Progress / Status Banner
        if (!gitHubDeployProgressText.isNullOrBlank()) {
            Surface(
                color = Color(0xFF0D2235),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, BasoCyan),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isDeployingToGitHub) {
                        CircularProgressIndicator(
                            color = BasoCyan,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = gitHubDeployProgressText,
                        color = BasoTextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // =========================================================================
        // CARD 2: ANDROID APK BUILD & DIRECT DOWNLOAD (کاتێ ئەپ دروست دەکەیت وەک APK دایبەزێنە)
        // =========================================================================
        Card(
            colors = CardDefaults.cardColors(containerColor = BasoCardSurface),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, BasoGold),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(BasoGold.copy(alpha = 0.16f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Android,
                            contentDescription = null,
                            tint = BasoGold,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isKurdish) {
                                "📲 دروستکردن و دابەزاندنی ئەپ وەک فایلی APK"
                            } else {
                                "📲 Build & Download Android APK"
                            },
                            color = BasoTextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (isKurdish) {
                                "کۆمپایلکردنی ڕاستەوخۆی ئەپەکەت بۆ فایلی دامەزراندنی .apk لە ڕێگەی GitHub Actions و دابەزاندنی بۆ مۆبایلەکەت"
                            } else {
                                "Compile your generated app into an installable Android .apk via GitHub Actions Release & instant standalone package"
                            },
                            color = BasoTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                // 1-Click Cloud APK Build & Download via GitHub
                Button(
                    onClick = {
                        if (gitHubDeployResult?.success == true) {
                            onDownloadLatestApk()
                        } else {
                            onDeployToGitHub()
                        }
                    },
                    enabled = !isDeployingToGitHub,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BasoGold,
                        contentColor = BasoDarkOnAccent
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("build_and_download_apk_button")
                ) {
                    Icon(imageVector = Icons.Default.Android, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (gitHubDeployResult?.success == true) {
                            if (isKurdish) "⬇️ دابەزاندنی ڕاستەوخۆی فایلی APK (Download APK)" else "⬇️ Download Compiled APK"
                        } else {
                            if (isKurdish) "🚀 دروستکردنی APK لە ڕێگەی GitHub Actions و دابەزاندن" else "🚀 Build APK on GitHub & Download"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Instant Local Standalone App Package Export (No waiting for cloud compile)
                OutlinedButton(
                    onClick = onDownloadStandaloneApp,
                    border = BorderStroke(1.2.dp, BasoGold.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = BasoCardElevated,
                        contentColor = BasoTextWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("download_standalone_app_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = BasoGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isKurdish) {
                            "📱 دابەزاندنی ئەپی سەربەخۆی خێرا (ڕاستەوخۆ لەسەر مۆبایل بیکەرەوە)"
                        } else {
                            "📱 Instant Standalone Mobile App File (Open Immediately)"
                        },
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Interactive GitHub Actions Workflow (.github/workflows/build-apk.yml) Inspector & 1-Click Copy
                var showWorkflowYamlPreview by remember { mutableStateOf(false) }
                val mainWorkflowYaml = remember { GitHubDeployService.buildMainBasoStudioWorkflowYaml() }

                Surface(
                    color = Color(0xFF071522),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BasoCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isKurdish) {
                                        "⚙️ فایلی تەواوی GitHub Actions (.github/workflows/build-apk.yml)"
                                    } else {
                                        "⚙️ Complete GitHub Actions APK Workflow (build-apk.yml)"
                                    },
                                    color = BasoCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = if (isKurdish) {
                                        "ئامادەکراوە بە JDK 21 + Gradle 9.3.1 + Android SDK 36.1 بۆ دروستکردنی ڕاستەوخۆی APK لە بەشی Actions و Releases"
                                    } else {
                                        "Pre-configured with JDK 21 + Gradle 9.3.1 + SDK 36.1 for automatic Actions APK build"
                                    },
                                    color = BasoTextMuted,
                                    fontSize = 10.5.sp
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showWorkflowYamlPreview = !showWorkflowYamlPreview },
                                border = BorderStroke(1.dp, BasoCyan),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = null,
                                    tint = BasoCyan,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (showWorkflowYamlPreview) {
                                        if (isKurdish) "شاردنەوەی کۆدی Workflow" else "Hide Workflow YAML"
                                    } else {
                                        if (isKurdish) "بینینی فایلی build-apk.yml" else "View build-apk.yml"
                                    },
                                    color = BasoCyan,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(
                                        ClipData.newPlainText("build-apk.yml", mainWorkflowYaml)
                                    )
                                    Toast.makeText(
                                        context,
                                        if (isKurdish) "فایلی build-apk.yml کۆپی کرا!" else "build-apk.yml copied!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                border = BorderStroke(1.dp, BasoGold),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    tint = BasoGold,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isKurdish) "کۆپیکردنی Workflow" else "Copy Workflow YAML",
                                    color = BasoGold,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (showWorkflowYamlPreview) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Surface(
                                    color = Color(0xFF040C14),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, BasoBorderCyan),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = mainWorkflowYaml,
                                        color = Color(0xFFE2E8F0),
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.5.sp,
                                        lineHeight = 15.sp,
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .horizontalScroll(rememberScrollState())
                                    )
                                }
                            }
                        }
                    }
                }

                if (gitHubDeployResult?.success == true) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                GitHubDeployService.openUrlInBrowser(context, gitHubDeployResult.actionsBuildUrl)
                            },
                            border = BorderStroke(1.dp, BasoCyan),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, tint = BasoCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isKurdish) "پەڕەی APK Actions" else "APK Actions Status",
                                color = BasoCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                GitHubDeployService.openUrlInBrowser(context, gitHubDeployResult.directApkReleaseUrl)
                            },
                            border = BorderStroke(1.dp, BasoGold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = BasoGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isKurdish) "لینکی ڕاستەوخۆی .APK" else "Direct .APK Link",
                                color = BasoGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // =========================================================================
        // CARD 3: WEBSITE DIRECT GITHUB CONNECT & LIVE GITHUB PAGES DEPLOYMENT
        // =========================================================================
        Card(
            colors = CardDefaults.cardColors(containerColor = BasoCardSurface),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, BasoCyan),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(BasoCyan.copy(alpha = 0.16f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = BasoCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isKurdish) {
                                "🌐 کۆنێکتی ڕاستەوخۆی وێبسایت بە GitHub (Live Pages)"
                            } else {
                                "🌐 Direct Website Connect to GitHub & Live Pages"
                            },
                            color = BasoTextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (isKurdish) {
                                "بە یەک کلیک وێبسایتەکەت دەنێردرێت بۆ GitHub و لەسەر لینکی فەرمی github.io دەستبەجێ ئامادە دەبێت"
                            } else {
                                "1-Click push to a new GitHub repository with automatic GitHub Pages live website activation"
                            },
                            color = BasoTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Button(
                    onClick = onDeployToGitHub,
                    enabled = !isDeployingToGitHub,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BasoCyan,
                        contentColor = BasoDarkOnAccent
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("deploy_website_github_button")
                ) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isKurdish) {
                            "🌐 کۆنێکتکردن بە GitHub و بڵاوکردنەوەی ڕاستەوخۆی وێبسایت"
                        } else {
                            "🌐 Connect & Publish Website to GitHub Now"
                        },
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Live Deployed URLs Card when deployed
                if (gitHubDeployResult?.success == true) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Surface(
                            color = Color(0xFF071A29),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, BasoSuccessGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "🟢 LIVE WEBSITE URL (GitHub Pages):",
                                    color = BasoSuccessGreen,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = gitHubDeployResult.liveWebsiteUrl,
                                    color = BasoTextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = JetBrainsMonoFontFamily
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            GitHubDeployService.openUrlInBrowser(context, gitHubDeployResult.liveWebsiteUrl)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = BasoSuccessGreen,
                                            contentColor = BasoDarkOnAccent
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isKurdish) "کردنەوەی وێبسایت" else "Open Website",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(
                                                ClipData.newPlainText("Live Website URL", gitHubDeployResult.liveWebsiteUrl)
                                            )
                                            Toast.makeText(
                                                context,
                                                if (isKurdish) "لینکی وێبسایت کۆپی کرا!" else "Website URL copied!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        border = BorderStroke(1.dp, BasoCyan),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = BasoCyan, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isKurdish) "کۆپیکردنی لینک" else "Copy URL",
                                            color = BasoCyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        GitHubDeployService.openUrlInBrowser(context, gitHubDeployResult.repoUrl)
                                    },
                                    border = BorderStroke(1.dp, BasoBorderCyan),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "GitHub Repo: ${gitHubDeployResult.repoUrl}",
                                        color = BasoTextWhite,
                                        fontSize = 11.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // CARD 4: COMPLETE ZIP BUNDLE DOWNLOAD & FILES VIEWER
        // =========================================================================
        Card(
            colors = CardDefaults.cardColors(containerColor = BasoCardSurface),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, BasoBorderCyan),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Surface(
                        color = BasoCardElevated,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BasoBorderCyan),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDownloadZip() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = BasoCyan,
                                    modifier = Modifier.size(28.dp)
                                )
                                Column {
                                    Text(
                                        text = zipFileName,
                                        color = BasoTextWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = zipSizeLabel,
                                        color = BasoTextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Surface(
                                color = BasoCardSurface,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, BasoCyan.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Download ZIP",
                                    tint = BasoCyan,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(20.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDownloadZip,
                        border = BorderStroke(1.2.dp, BasoGold),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = BasoCardElevated,
                            contentColor = BasoGold
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("download_zip_primary_button")
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isKurdish) "دابەزاندنی ZIP" else "Download ZIP",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    OutlinedButton(
                        onClick = onViewFiles,
                        border = BorderStroke(1.2.dp, BasoCyan.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = BasoCardSurface,
                            contentColor = BasoTextWhite
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("view_files_secondary_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = BasoCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isKurdish) "بینینی فایلەکان" else "View Files",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
