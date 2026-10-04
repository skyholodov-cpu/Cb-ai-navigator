package com.example.ui.consultant

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.ChatMessageEntity
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.Cyan80
import com.example.ui.theme.Emerald80
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageItemView(
    message: ChatMessageEntity,
    onToggleBookmark: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.sender == "USER"
    val context = LocalContext.current
    val timeFormatted = remember(message.timestamp) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (isUser) {
            UserMessageBubble(message = message, timeFormatted = timeFormatted)
        } else {
            NavigatorResponseCard(
                message = message,
                timeFormatted = timeFormatted,
                onToggleBookmark = onToggleBookmark,
                onCopy = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("AI Navigator Analysis", message.text))
                    Toast.makeText(context, "Анализ скопирован в буфер", Toast.LENGTH_SHORT).show()
                },
                onShare = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Стратегический анализ AI Navigator:\n\n${message.text}")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Поделиться анализом"))
                }
            )
        }
    }
}

@Composable
fun UserMessageBubble(
    message: ChatMessageEntity,
    timeFormatted: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = modifier
    ) {
        Card(
            shape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.padding(start = 48.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // If user attached an image, show preview
                if (!message.imageUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = Uri.parse(message.imageUri),
                        contentDescription = "Прикреплённый график или отчёт",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        lineHeight = 22.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    ),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun NavigatorResponseCard(
    message: ChatMessageEntity,
    timeFormatted: String,
    onToggleBookmark: (Long, Boolean) -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val parsedSections = remember(message.text) { parseStructuredReport(message.text) }

    Card(
        shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: AI Navigator Brand & Bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "AI Navigator",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Navigator",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "FINANCIAL INTELLIGENCE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { onToggleBookmark(message.id, message.isBookmarked) },
                    modifier = Modifier.testTag("bookmark_button_${message.id}")
                ) {
                    Icon(
                        imageVector = if (message.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (message.isBookmarked) "В закладках" else "Добавить в архив",
                        tint = if (message.isBookmarked) AccentGold else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Content Sections
            if (parsedSections.isNotEmpty()) {
                parsedSections.forEach { section ->
                    ReportSectionBox(section = section)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            } else {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action toolbar & timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = onCopy,
                        label = { Text("Копировать", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Копировать",
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                    AssistChip(
                        onClick = onShare,
                        label = { Text("Поделиться", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Поделиться",
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                }

                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

data class ParsedSection(
    val title: String,
    val content: String,
    val icon: ImageVector,
    val accentColor: Color
)

fun parseStructuredReport(fullText: String): List<ParsedSection> {
    val sections = mutableListOf<ParsedSection>()
    val lines = fullText.lines()
    var currentHeader: String? = null
    val currentContent = StringBuilder()

    fun flush() {
        val header = currentHeader
        if (header != null) {
            val (icon, color) = when {
                header.contains("Краткий вывод", ignoreCase = true) ->
                    Pair(Icons.Default.Lightbulb, AccentGreen)
                header.contains("Детальный анализ", ignoreCase = true) ->
                    Pair(Icons.Default.QueryStats, Cyan80)
                header.contains("Риски", ignoreCase = true) ->
                    Pair(Icons.Default.WarningAmber, AccentGold)
                header.contains("Сценарии", ignoreCase = true) ->
                    Pair(Icons.Default.Speed, Color(0xFFA78BFA))
                header.contains("Практические", ignoreCase = true) || header.contains("шаги", ignoreCase = true) ->
                    Pair(Icons.Default.Psychology, Emerald80)
                else ->
                    Pair(Icons.Default.QueryStats, Cyan80)
            }
            sections.add(
                ParsedSection(
                    title = header,
                    content = currentContent.toString().trim(),
                    icon = icon,
                    accentColor = color
                )
            )
            currentContent.clear()
        }
    }

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.startsWith("###")) {
            flush()
            currentHeader = trimmed.removePrefix("###").trim()
        } else if (currentHeader != null) {
            if (currentContent.isNotEmpty()) {
                currentContent.append("\n")
            }
            currentContent.append(line)
        }
    }
    flush()
    return sections
}

@Composable
fun ReportSectionBox(section: ParsedSection) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = section.accentColor.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            section.accentColor.copy(alpha = 0.25f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = section.icon,
                    contentDescription = null,
                    tint = section.accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = section.accentColor,
                        fontSize = 13.sp
                    )
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = section.content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            )
        }
    }
}
