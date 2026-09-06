package com.heaton.funnyvote.ui.common

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.analytics.FirebaseAnalytics
import com.heaton.funnyvote.R
import com.heaton.funnyvote.ui.theme.FunnyVoteBlue
import com.heaton.funnyvote.ui.theme.TextPrimary
import com.heaton.funnyvote.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(
    title: String,
    voteUrl: String,
    isShareApp: Boolean = false,
    voteCode: String = "",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val modalBottomSheetState = rememberModalBottomSheetState()
    val analytics = remember { FirebaseAnalytics.getInstance(context) }

    fun logShareClick(platform: String) {
        val bundle = android.os.Bundle().apply {
            putString("platform", platform)
            putString("vote_code", voteCode)
            putBoolean("is_share_app", isShareApp)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalBottomSheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher),
                contentDescription = null,
                modifier = Modifier.size(54.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isShareApp) "分享 FunnyVote 給好友" else "分享投票透過",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = TextPrimary
            )

            Text(
                text = if (isShareApp) "邀請更多朋友一起參與社群投票！" else title,
                fontSize = 13.sp,
                color = TextSecondary,
                maxLines = 2,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            val shareText = if (isShareApp) {
                "最有趣的投票社群 FunnyVote！快來下載體驗：$voteUrl"
            } else {
                "【FunnyVote 投票】$title\n快來發表你的意見：$voteUrl"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 1. LINE 分享
                ShareItem(
                    label = "LINE",
                    icon = Icons.Default.Share,
                    iconBg = Color(0xFF06C755).copy(alpha = 0.15f),
                    iconTint = Color(0xFF06C755),
                    onClick = {
                        logShareClick("LINE")
                        try {
                            val lineIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                setPackage("jp.naver.line.android")
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(lineIntent)
                        } catch (e: Exception) {
                            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://line.me/R/msg/text/?${android.net.Uri.encode(shareText)}"))
                            context.startActivity(intent)
                        }
                        onDismiss()
                    }
                )

                // 2. Facebook 分享
                ShareItem(
                    label = "Facebook",
                    icon = Icons.Default.Share,
                    iconBg = Color(0xFF1877F2).copy(alpha = 0.15f),
                    iconTint = Color(0xFF1877F2),
                    onClick = {
                        logShareClick("Facebook")
                        try {
                            val fbIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                setPackage("com.facebook.katana")
                                putExtra(Intent.EXTRA_TEXT, voteUrl)
                            }
                            context.startActivity(fbIntent)
                        } catch (e: Exception) {
                            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.facebook.com/sharer/sharer.php?u=${android.net.Uri.encode(voteUrl)}"))
                            context.startActivity(intent)
                        }
                        onDismiss()
                    }
                )

                // 3. 複製連結
                ShareItem(
                    label = "複製連結",
                    icon = Icons.Default.ContentCopy,
                    iconBg = FunnyVoteBlue.copy(alpha = 0.1f),
                    iconTint = FunnyVoteBlue,
                    onClick = {
                        logShareClick("CopyLink")
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("FunnyVote Link", voteUrl)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "已複製連結至剪貼簿", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                // 4. 更多選項 (系統分享)
                ShareItem(
                    label = "更多",
                    icon = Icons.Default.MoreHoriz,
                    iconBg = Color(0xFFFF9800).copy(alpha = 0.15f),
                    iconTint = Color(0xFFE65100),
                    onClick = {
                        logShareClick("SystemMore")
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, if (isShareApp) "FunnyVote" else title)
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "分享投票"))
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun ShareItem(
    label: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = iconBg,
            modifier = Modifier.size(52.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(26.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}
