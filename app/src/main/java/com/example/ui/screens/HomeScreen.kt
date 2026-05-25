package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Announcement
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Notice
import com.example.data.model.PrayerTime
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldPrimaryVariant
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LightSurface
import com.example.ui.theme.AlertRed
import com.example.ui.viewmodel.MasjidViewModel
import com.example.utils.BanglaDateUtils
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: MasjidViewModel,
    modifier: Modifier = Modifier
) {
    val notices by viewModel.notices.collectAsState()
    val prayerTimes by viewModel.prayerTimes.collectAsState()

    // Screen-level Dialog states
    var showAddNoticeDialog by remember { mutableStateOf(false) }
    var showEditPrayerDialog by remember { mutableStateOf<PrayerTime?>(null) }

    // Digital clock state updating dynamically every second
    var currentTime by remember { mutableStateOf(SimpleDateFormat("hh:mm:ss a", Locale.US).format(Date())) }
    var currentGregorianDate by remember { mutableStateOf(SimpleDateFormat("EEEE, d MMMM yyyy", Locale.US).format(Date())) }
    var currentBengaliDate by remember { mutableStateOf(BanglaDateUtils.getBengaliDate()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("hh:mm:ss a", Locale.US).format(Date())
            currentGregorianDate = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.US).format(Date())
            currentBengaliDate = BanglaDateUtils.getBengaliDate()
            delay(1000)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // 1. Serene Spiritual Mosque-Themed Hero Header
        item {
            HeaderCard(
                currentTime = currentTime,
                gregorianDate = currentGregorianDate,
                bengaliDate = currentBengaliDate
            )
        }

        // 2. NEW FEATURE: Prominent Notice Board at the Top (immediately after header)
        item {
            TopNoticeBoardSection(
                notices = notices,
                onAddNoticeClick = { showAddNoticeDialog = true },
                onDeleteNotice = { notice -> viewModel.removeNotice(notice) }
            )
        }

        // 3. Prayer Times Grid Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Prayer Times Icon",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "আজকের নামাজের সময়সূচী",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                }
                Text(
                    text = "বাংলাদেশ সময়",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        // 4. Prayer Times Flow/Grid Section (6 major waqts)
        item {
            // Ensure correct canonical ordering of Islamic prayers
            val orderedKeys = listOf("fajr", "dhuhr", "asr", "maghrib", "isha", "jumuah")
            val sortedPrayerTimes = orderedKeys.mapNotNull { key ->
                prayerTimes.find { it.id == key }
            }
            val chunks = sortedPrayerTimes.chunked(3)
            chunks.forEach { rowPrayers ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowPrayers.forEach { prayer ->
                        PrayerGridCard(
                            prayer = prayer,
                            onEditClick = { showEditPrayerDialog = prayer },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowPrayers.size < 3) {
                        Spacer(modifier = Modifier.weight((3 - rowPrayers.size).toFloat()))
                    }
                }
            }
        }
    }

    // Modal Sheet 1: Create New Announcement Notice
    if (showAddNoticeDialog) {
        AddNoticeDialog(
            onDismiss = { showAddNoticeDialog = false },
            onSave = { title, content ->
                viewModel.postNotice(title, content)
                showAddNoticeDialog = false
            }
        )
    }

    // Modal Sheet 2: Edit Waqt/Iqamah Times
    showEditPrayerDialog?.let { currentPrayer ->
        EditPrayerTimeDialog(
            prayer = currentPrayer,
            onDismiss = { showEditPrayerDialog = null },
            onSave = { updatedCode, updatedEng, updatedBeng, updatedTime, updatedIqamah ->
                viewModel.updatePrayerTime(updatedCode, updatedEng, updatedBeng, updatedTime, updatedIqamah)
                showEditPrayerDialog = null
            }
        )
    }
}

@Composable
fun HeaderCard(
    currentTime: String,
    gregorianDate: String,
    bengaliDate: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldPrimary)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Spiritual Geometric / Abstract Background details via Canvas
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(24.dp))
            ) {
                val paintBrush = Brush.verticalGradient(
                    colors = listOf(EmeraldPrimary, EmeraldPrimaryVariant, Color(0xFF002219))
                )
                drawRect(brush = paintBrush)

                // Dome line at bottom
                val path = Path().apply {
                    moveTo(0f, size.height)
                    cubicTo(
                        size.width * 0.25f, size.height * 0.9f,
                        size.width * 0.25f, size.height * 0.65f,
                        size.width * 0.5f, size.height * 0.65f
                    )
                    cubicTo(
                        size.width * 0.75f, size.height * 0.65f,
                        size.width * 0.75f, size.height * 0.9f,
                        size.width, size.height
                    )
                }
                drawPath(
                    path = path,
                    color = GoldAccent.copy(alpha = 0.12f)
                )

                // Simple top-left and top-right stars (or crescent accents)
                drawCircle(
                    color = GoldAccent.copy(alpha = 0.15f),
                    radius = 40f,
                    center = Offset(size.width * 0.85f, size.height * 0.25f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mosque icon badge
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(GoldAccent.copy(alpha = 0.2f), CircleShape)
                        .border(1.dp, GoldAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook, // book symbolizing Quran
                        contentDescription = "Spiritual Crest",
                        tint = GoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "হেদায়েত জামে মসজিদ",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Masjid Management App",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Real-time ticking digital clock
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = currentTime,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldAccent
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Divider(
                    color = Color.White.copy(alpha = 0.15f),
                    thickness = 1.dp,
                    modifier = Modifier.width(180.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Beautiful dual calendar dates
                Text(
                    text = gregorianDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = bengaliDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoldAccent,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun PrayerGridCard(
    prayer: PrayerTime,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = LightSurface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
            .testTag("prayer_card_${prayer.id}")
            .border(
                width = 1.dp,
                color = EmeraldPrimary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Edit trigger and Name Row
            Box(modifier = Modifier.fillMaxWidth()) {
                // Bengali name in center
                Text(
                    text = prayer.bengaliName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                    modifier = Modifier.align(Alignment.Center)
                )

                // Quick edit pencil button in corner
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Prayer Time",
                    tint = GoldAccent,
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.TopEnd)
                        .clickable { onEditClick() }
                )
            }

            Text(
                text = prayer.englishName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Main Waqt Time Displays
            Text(
                text = prayer.time,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = GoldAccent,
                textAlign = TextAlign.Center
            )

            // Dynamic Iqamah sub-label
            if (prayer.iqamahTime.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .background(
                            color = EmeraldPrimary.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = prayer.iqamahTime,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = EmeraldPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun NoticeItemCard(
    notice: Notice,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("notice_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notice.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = notice.dateString,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_notice_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Notice",
                        tint = AlertRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = notice.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                lineHeight = 22.sp
            )
        }
    }
}

// Dialog Component for adding new announcements
@Composable
fun AddNoticeDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, content: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var errors by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "নতুন নোটিশ প্রকাশ করুন",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; errors = "" },
                    label = { Text("নোটিশের শিরোনাম") },
                    placeholder = { Text("উদা: ঈদুল ফিতরের জামাত সূচী") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notice_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        focusedLabelColor = EmeraldPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it; errors = "" },
                    label = { Text("নোটিশের বিস্তারিত ঘোষণা") },
                    placeholder = { Text("এখানে ঘোষণার সমস্ত বিবরণ গুছিয়ে লিখুন...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("notice_content_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        focusedLabelColor = EmeraldPrimary
                    )
                )

                if (errors.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errors, color = AlertRed, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isBlank() || content.isBlank()) {
                                errors = "দয়া করে সমস্ত তথ্য পূরণ করুন।"
                            } else {
                                onSave(title, content)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_notice_button")
                    ) {
                        Text("ঘোষণা করুন", color = Color.White)
                    }
                }
            }
        }
    }
}

// Dialog Component for updating prayer times
@Composable
fun EditPrayerTimeDialog(
    prayer: PrayerTime,
    onDismiss: () -> Unit,
    onSave: (id: String, eng: String, beng: String, time: String, iqamah: String) -> Unit
) {
    var time by remember { mutableStateOf(prayer.time) }
    var iqamah by remember { mutableStateOf(prayer.iqamahTime) }
    var errors by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "${prayer.bengaliName} ওয়াক্ত পুনর্নির্ধারণ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it; errors = "" },
                    label = { Text("আজানের সময় (যেমন: 04:30 AM)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prayer_time_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        focusedLabelColor = EmeraldPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = iqamah,
                    onValueChange = { iqamah = it; errors = "" },
                    label = { Text("জামাতের সময় (যেমন: Iqamah: 04:45 AM)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prayer_iqamah_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        focusedLabelColor = EmeraldPrimary
                    )
                )

                if (errors.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errors, color = AlertRed, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (time.isBlank()) {
                                errors = "আজানের ওয়াক্ত সময় পূরণ করা আবশ্যক।"
                            } else {
                                onSave(prayer.id, prayer.englishName, prayer.bengaliName, time, iqamah)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_prayer_button")
                    ) {
                        Text("হালনাগাদ", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun TopNoticeBoardSection(
    notices: List<Notice>,
    onAddNoticeClick: () -> Unit,
    onDeleteNotice: (Notice) -> Unit
) {
    var selectedNoticeForDetail by remember { mutableStateOf<Notice?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("top_notice_board_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = LightSurface
        ),
        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            // Header Row of the Notice Board
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pulsing/glowing indicator
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(AlertRed, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "জরুরি নোটিশ বোর্ড",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                }

                // Small sleek "Add Notice" button for Admin
                IconButton(
                    onClick = onAddNoticeClick,
                    modifier = Modifier
                        .size(32.dp)
                        .background(EmeraldPrimary.copy(alpha = 0.08f), CircleShape)
                        .testTag("admin_add_notice_shortcut")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Notice",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (notices.isEmpty()) {
                // Peaceful/empty notice state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldPrimary.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Announcement,
                            contentDescription = "No Notice",
                            tint = EmeraldPrimary.copy(alpha = 0.3f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "বর্তমানে কোনো নতুন নোটিশ বা ঘোষণা নেই।\nনামাজে মনোযোগী হউন, আল্লাহ সবাইকে সহায় হোন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                // Scrolling horizontal lists of notices!
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 4.dp)
                ) {
                    items(notices, key = { it.id }) { notice ->
                        NoticeCarouselCard(
                            notice = notice,
                            onCardClick = { selectedNoticeForDetail = notice },
                            onDelete = { onDeleteNotice(notice) }
                        )
                    }
                }

                // Show instruction to swipe / click
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "← ডানে-বামে সুইপ করুন (${BanglaDateUtils.toBanglaNumerals(notices.size.toString())} টি সচল নোটিশ)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                    Text(
                        text = "বিস্তারিত দেখতে নোটিশে ট্যাপ করুন *",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }

    // Detail Pop-up when a notice is clicked for smooth full view!
    selectedNoticeForDetail?.let { notice ->
        NoticeDetailDialog(
            notice = notice,
            onDismiss = { selectedNoticeForDetail = null }
        )
    }
}

@Composable
fun NoticeCarouselCard(
    notice: Notice,
    onCardClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(130.dp)
            .clickable(onClick = onCardClick)
            .testTag("notice_carousel_item"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldPrimary.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notice.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Notice",
                        tint = AlertRed,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { onDelete() }
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = notice.dateString,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = notice.content,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun NoticeDetailDialog(
    notice: Notice,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "নোটিশের বিস্তারিত",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = notice.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = EmeraldPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Calendar Icon",
                        tint = GoldAccent,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = notice.dateString,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = notice.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ঠিক আছে", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
