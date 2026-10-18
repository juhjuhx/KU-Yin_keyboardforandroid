package com.example.ime.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import com.example.R
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class OpenSourceFoundation(
    val id: String,
    val name: String,
    val enName: String,
    val description: String,
    val donateUrl: String,
    @DrawableRes val qrDrawableRes: Int,
    val badgeColor: Color,
    val impactTag: String
)

val OPEN_SOURCE_FOUNDATIONS = listOf(
    OpenSourceFoundation(
        id = "fsf",
        name = "自由軟體基金會",
        enName = "Free Software Foundation (FSF)",
        description = "捍衛全球電腦使用者的四大軟體自由，維護 GNU 與自由開源體系發展。",
        donateUrl = "https://my.fsf.org/donate",
        qrDrawableRes = R.drawable.foundation_qr_fsf,
        badgeColor = Color(0xFF15803D),
        impactTag = "自由軟體核心維護"
    ),
    OpenSourceFoundation(
        id = "asf",
        name = "阿帕契軟體基金會",
        enName = "Apache Software Foundation (ASF)",
        description = "以「公益軟體」理念孵化維護全球逾 350 個頂級開源專案與基礎設施。",
        donateUrl = "https://donate.apache.org/",
        qrDrawableRes = R.drawable.foundation_qr_asf,
        badgeColor = Color(0xFFC2410C),
        impactTag = "全球最大公共開源生態"
    ),
    OpenSourceFoundation(
        id = "osi",
        name = "開源倡議組織",
        enName = "Open Source Initiative (OSI)",
        description = "定義開源軟體標準 (OSD)，保護全球開源協議與社群知識共享授權。",
        donateUrl = "https://members.opensource.org/donate",
        qrDrawableRes = R.drawable.foundation_qr_osi,
        badgeColor = Color(0xFF0369A1),
        impactTag = "開源標準定義者"
    ),
    OpenSourceFoundation(
        id = "eff",
        name = "電子前哨基金會",
        enName = "Electronic Frontier Foundation (EFF)",
        description = "保護數位世界的使用者隱私、言論自由與開源開發者合法權益。",
        donateUrl = "https://supporters.eff.org/donate/donate",
        qrDrawableRes = R.drawable.foundation_qr_eff,
        badgeColor = Color(0xFF7C3AED),
        impactTag = "數位人權與開源權益"
    )
)

@Composable
fun OpenSourceDonationCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedIndex by remember { mutableIntStateOf(0) }
    val foundation = OPEN_SOURCE_FOUNDATIONS[selectedIndex]

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("open_source_donation_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 標題列
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFDC2626).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "開源社群捐助",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "開源社群與自由軟體捐助",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "專案秉持開源精神完全免費，邀請將愛心給予全球開源基金會",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 開源宣告小卡
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VolunteerActivism,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "捐款直接交予所選組織；本專案不收取款項，亦不代表組織背書。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 基金會選擇分頁晶片
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(OPEN_SOURCE_FOUNDATIONS) { index, item ->
                    FilterChip(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        label = { Text(item.name) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(item.badgeColor)
                            )
                        },
                        modifier = Modifier.testTag("chip_foundation_${item.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // QR Code 與基金會簡介卡
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = foundation.badgeColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = foundation.impactTag,
                                color = foundation.badgeColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = foundation.enName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = foundation.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = foundation.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // QR Code 呈現區
                    BoxWithConstraints(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .testTag("qr_code_${foundation.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        val image = ImageBitmap.imageResource(foundation.qrDrawableRes)
                        // Generated assets have 8 source pixels per module (including quiet zone).
                        // Keep module edges on integral display pixels instead of fractional resampling.
                        val modules = image.width / 8
                        val imageSize = with(LocalDensity.current) {
                            // Preserve the original 8dp inset so the rounded frame never clips quiet-zone corners.
                            val pixelsPerModule = ((maxWidth - 16.dp).toPx() / modules).toInt().coerceAtLeast(1)
                            (pixelsPerModule * modules).toDp()
                        }
                        Image(
                            bitmap = image,
                            contentDescription = "${foundation.name}官方捐款網址 QR Code",
                            modifier = Modifier.size(imageSize),
                            contentScale = ContentScale.Fit,
                            filterQuality = FilterQuality.None
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "手機相機掃描尚未驗證；可複製或開啟官方網址。",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = foundation.donateUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 動作按鈕列
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(foundation.donateUrl))
                                Toast.makeText(context, "已複製 ${foundation.name} 捐款網址", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_copy_donate_url")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("複製網址", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(foundation.donateUrl)).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "無法開啟瀏覽器: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("btn_open_donate_url")
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("前往官方捐款", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
