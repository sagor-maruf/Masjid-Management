package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SalaryRecord
import com.example.data.model.Member
import com.example.data.model.MemberPayment
import com.example.ui.theme.AlertRed
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldPrimaryVariant
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LightSurface
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.MasjidViewModel
import com.example.utils.BanglaDateUtils
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SalaryScreen(
    viewModel: MasjidViewModel,
    modifier: Modifier = Modifier
) {
    val salaryRecords by viewModel.salaryRecords.collectAsState()
    val members by viewModel.members.collectAsState()
    val memberPayments by viewModel.memberPayments.collectAsState()

    var activeTab by remember { mutableStateOf(1) } // Default to Tab 1 (Members list) to prioritize core feature!
    var showAddMonthDialog by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<SalaryRecord?>(null) }
    var selectedMemberProfile by remember { mutableStateOf<Member?>(null) }
    var showAddMemberDialog by remember { mutableStateOf(false) }

    // Aggregate values for Tab 0 (Ledger)
    val totalDue = remember(salaryRecords) {
        salaryRecords.sumOf { maxOf(0.0, it.baseSalary - it.paidAmount) }
    }
    val totalPaid = remember(salaryRecords) {
        salaryRecords.sumOf { it.paidAmount }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("salary_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Premium Sliding Pill Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(EmeraldPrimary.copy(alpha = 0.05f))
                    .padding(4.dp)
            ) {
                // Tab 1: Members (৪০ ঘর সদস্য চাঁদা ট্র্যাকার)
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (activeTab == 1) EmeraldPrimary else Color.Transparent)
                        .clickable { activeTab = 1 }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Members Tab",
                            tint = if (activeTab == 1) Color.White else EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "৪০ ঘর সদস্য চাঁদা",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == 1) Color.White else EmeraldPrimary
                        )
                    }
                }

                // Tab 0: Ledger (ইমাম সাহেবের মোট সম্মানি খতিয়ান)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (activeTab == 0) EmeraldPrimary else Color.Transparent)
                        .clickable { activeTab = 0 }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = "Ledger Tab",
                            tint = if (activeTab == 0) Color.White else EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "সম্মানি খতিয়ান",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == 0) Color.White else EmeraldPrimary
                        )
                    }
                }
            }

            // Tab Screen Dispatcher
            if (activeTab == 0) {
                // ====== Imam's base ledger view (existing features preserved) ======
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    item {
                        LedgerStatsCard(
                            totalPaid = totalPaid,
                            totalDue = totalDue,
                            totalMonths = salaryRecords.size
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "সম্মানি ও ভাতা খতিয়ান",
                                style = MaterialTheme.typography.titleMedium,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold
                            )

                            Button(
                                onClick = { showAddMonthDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldPrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("add_salary_month_button")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Add records list", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("নতুন মাস যোগ", fontSize = 12.sp)
                            }
                        }
                    }

                    if (salaryRecords.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccountBalanceWallet,
                                        contentDescription = "No salary entries",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "বেতন খতিয়ানে কোনো ডেটা পাওয়া যায়নি।",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    } else {
                        items(salaryRecords, key = { it.id }) { record ->
                            SalaryRecordRow(
                                record = record,
                                onEdit = { editingRecord = record },
                                onDelete = { viewModel.removeSalaryRecord(record) }
                            )
                        }
                    }
                }
            } else {
                // ====== 40 Householder Subscriber List View ======
                var memberSearchQuery by remember { mutableStateOf("") }
                val activeTargetYear = Calendar.getInstance().get(Calendar.YEAR)

                // Filter members
                val filteredMembers = remember(members, memberSearchQuery) {
                    members.filter {
                        it.name.contains(memberSearchQuery, ignoreCase = true) ||
                        it.address.contains(memberSearchQuery, ignoreCase = true)
                    }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    // Search layout and count label
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = memberSearchQuery,
                            onValueChange = { memberSearchQuery = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("member_search_input"),
                            shape = RoundedCornerShape(14.dp),
                            placeholder = { Text("নাম বা গ্রাম দিয়ে সদস্য খোঁজেন...", fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = EmeraldPrimary)
                            },
                            trailingIcon = {
                                if (memberSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { memberSearchQuery = "" }) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = EmeraldPrimary.copy(alpha = 0.2f)
                            )
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "মোট নিবন্ধিত সদস্য: ${BanglaDateUtils.toBanglaNumerals(members.size.toString())} জন",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimaryVariant
                        )
                        Text(
                            text = "ভাতা হার: ৫০০ ৳/মাস",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = GoldAccent
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp, top = 2.dp)
                    ) {
                        if (filteredMembers.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.PersonSearch,
                                            contentDescription = "No member",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                            modifier = Modifier.size(60.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "কোনো সদস্য খুঁজে পাওয়া যায়নি!",
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        } else {
                            items(filteredMembers.values().toList().sortedBy { it.id }) { member ->
                                // Calculate paid vs due months count for current member for this active year (e.g., 2026)
                                val currentYearPayments = memberPayments.filter { it.memberId == member.id && it.year == activeTargetYear && it.month <= 12 }
                                val paidMonthsCount = currentYearPayments.filter { it.salaryStatus == "PAID" }.size
                                val dueMonthsCount = 12 - paidMonthsCount

                                MemberRowItem(
                                    index = members.indexOf(member) + 1,
                                    member = member,
                                    paidCount = paidMonthsCount,
                                    dueCount = dueMonthsCount,
                                    activeYear = activeTargetYear,
                                    onClick = { selectedMemberProfile = member }
                                )
                            }
                        }
                    }
                }
            }
        }

        // FAB to append brand new household/members in Tab 1
        if (activeTab == 1) {
            FloatingActionButton(
                onClick = { showAddMemberDialog = true },
                containerColor = GoldAccent,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 90.dp, end = 20.dp)
                    .testTag("add_member_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Add new mosque family",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    // Modal dialogs
    if (showAddMonthDialog) {
        CreateSalaryRecordDialog(
            onDismiss = { showAddMonthDialog = false },
            onSave = { year, monthVal, monthName, baseSal, paidAmt ->
                viewModel.addOrUpdateSalaryRecord(year, monthVal, monthName, baseSal, paidAmt)
                showAddMonthDialog = false
            }
        )
    }

    editingRecord?.let { record ->
        EditSalaryRecordDialog(
            record = record,
            onDismiss = { editingRecord = null },
            onSave = { updatedBase, updatedPaid ->
                viewModel.addOrUpdateSalaryRecord(
                    record.year,
                    record.month,
                    record.monthName,
                    updatedBase,
                    updatedPaid
                )
                editingRecord = null
            }
        )
    }

    if (showAddMemberDialog) {
        AddMemberDialog(
            onDismiss = { showAddMemberDialog = false },
            onSave = { name, phone, address ->
                viewModel.addMember(name, phone, address)
                showAddMemberDialog = false
            }
        )
    }

    selectedMemberProfile?.let { member ->
        MemberProfileDialog(
            member = member,
            memberPayments = memberPayments,
            onDismiss = { selectedMemberProfile = null },
            onToggleMonth = { year, mIdx, mName ->
                viewModel.toggleSalaryStatus(member.id, year, mIdx, mName)
            },
            onSaveTarabi = { year, amt ->
                viewModel.updateTarabiAmount(member.id, year, amt)
            },
            onDeleteMember = {
                viewModel.removeMember(member)
                selectedMemberProfile = null
            }
        )
    }
}

// Extension to get safe iterable
fun <T> List<T>.values(): List<T> = this

@Composable
fun MemberRowItem(
    index: Int,
    member: Member,
    paidCount: Int,
    dueCount: Int,
    activeYear: Int,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick)
            .testTag("member_row_${member.id}"),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                modifier = Modifier.weight(1f)
            ) {
                // Circular index badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(EmeraldPrimary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = BanglaDateUtils.toBanglaNumerals(index.toString()),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = member.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Place, contentDescription = "Area", tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = member.address,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        if (member.phone.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(imageVector = Icons.Default.Phone, contentDescription = "Phone", tint = Color.Gray, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = member.phone,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Paid/Due Badge counters
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(SuccessGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${BanglaDateUtils.toBanglaNumerals(paidCount.toString())}Paid",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .background(AlertRed.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${BanglaDateUtils.toBanglaNumerals(dueCount.toString())}Due",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AlertRed
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${BanglaDateUtils.toBanglaNumerals(activeYear.toString())} সাল",
                    fontSize = 9.sp,
                    color = Color.LightGray
                )
            }
        }
    }
}

// Helper Dialog to add new member
@Composable
fun AddMemberDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, address: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
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
                    text = "নতুন পরিবার/সদস্য যুক্ত করুন",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errors = "" },
                    label = { Text("সদস্যের নাম (আবশ্যক)") },
                    placeholder = { Text("যেমন: মো. খলিলুর রহমান") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_member_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        focusedLabelColor = EmeraldPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it; errors = "" },
                    label = { Text("এলাকা / গ্রাম / হোল্ডিং নং") },
                    placeholder = { Text("যেমন: উত্তর পাড়া") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_member_address_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        focusedLabelColor = EmeraldPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("মোবাইল নম্বর (ঐচ্ছিক)") },
                    placeholder = { Text("যেমন: 017XXXXXXXX") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_member_phone_input"),
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
                            if (name.isBlank() || address.isBlank()) {
                                errors = "দয়া করে নাম এবং ঠিকানা লিখুন।"
                            } else {
                                onSave(name, phone, address)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_member_button")
                    ) {
                        Text("যুক্ত করুন", color = Color.White)
                    }
                }
            }
        }
    }
}

// Comprehensive Detailed Profile View (Core Feature 1 & 2 combined)
@Composable
fun MemberProfileDialog(
    member: Member,
    memberPayments: List<MemberPayment>,
    onDismiss: () -> Unit,
    onToggleMonth: (year: Int, monthIdx: Int, monthName: String) -> Unit,
    onSaveTarabi: (year: Int, amount: Double) -> Unit,
    onDeleteMember: () -> Unit
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    var selectedYear by remember { mutableStateOf(currentYear) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    var showTarabiInputDialog by remember { mutableStateOf(false) }
    var tarabiAmountInput by remember { mutableStateOf("") }

    val bengaliMonthsList = listOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )

    // Filter payments for this member and this selected year (excluding Tarabi month 13)
    val yearPayments = memberPayments.filter { it.memberId == member.id && it.year == selectedYear && it.month <= 12 }
    
    // Find tarabi contribution for this selected year (month index 13 represents Tarabi)
    val tarabiRecord = memberPayments.find { it.memberId == member.id && it.year == selectedYear && it.month == 13 }
    val tarabiFundAmount = tarabiRecord?.tarabiAmountPaid ?: 0.0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header of Profile Modal
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(EmeraldPrimary, EmeraldPrimaryVariant)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = member.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            // Close Button
                            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close Detail", tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Place, contentDescription = "village", tint = GoldAccent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "এলাকা: ${member.address}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))

                            if (member.phone.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(16.dp))
                                Icon(imageVector = Icons.Default.Phone, contentDescription = "call", tint = GoldAccent, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = member.phone, fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    // 1. Year Switcher Row
                    item {
                        Column(modifier = Modifier.padding(vertical = 12.dp)) {
                            Text(
                                text = "বছর পরিবর্তন (হিস্টোরি ট্র্যাকিং):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimaryVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(2025, 2026, 2027).forEach { yr ->
                                    val isSelected = selectedYear == yr
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isSelected) GoldAccent else EmeraldPrimary.copy(alpha = 0.05f)
                                            )
                                            .clickable { selectedYear = yr }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${BanglaDateUtils.toBanglaNumerals(yr.toString())} সাল",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else EmeraldPrimary
                                        )
                                    }
                                }
                            }
                        }
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    }

                    // 2. Tarabi Fund Donation Tracking (Core Feature 2)
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = EmeraldPrimary.copy(alpha = 0.03f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBalanceWallet,
                                            contentDescription = "Tarabi Icon",
                                            tint = GoldAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "তারাবির ফান্ড ট্র্যাকিং",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldPrimary
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(GoldAccent.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "রমজান ও ঈদ",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldAccent
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "তারাবির চাঁদা/অনুদান পরিমাণ (মোট):",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = BanglaDateUtils.formatBDT(tarabiFundAmount),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = EmeraldPrimary
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            tarabiAmountInput = tarabiFundAmount.toInt().toString()
                                            showTarabiInputDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("টাকা সংগ্রহ", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    }

                    // 3. Imam's Salary Monthly Calendar breakdown (Core Feature 1)
                    item {
                        Column(modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)) {
                            Text(
                                text = "ইমামের মাসিক বেতন চাঁদা বিবরণী (${BanglaDateUtils.toBanglaNumerals(selectedYear.toString())} সাল)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                            Text(
                                text = "ভাতার অবস্থা পরিবর্তনের জন্য মাসের উপর ট্যাপ করুন।",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    // 12 Months layout inside LazyColumn using manual rows
                    bengaliMonthsList.chunked(2).forEach { chunk ->
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                chunk.forEach { banglaMonth ->
                                    val monthIndex = bengaliMonthsList.indexOf(banglaMonth) + 1
                                    
                                    // Query status
                                    val payment = yearPayments.find { it.month == monthIndex }
                                    val isPaid = payment?.salaryStatus == "PAID"
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(
                                                width = 1.dp,
                                                color = if (isPaid) SuccessGreen.copy(alpha = 0.4f) else AlertRed.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .background(
                                                if (isPaid) SuccessGreen.copy(alpha = 0.06f) else AlertRed.copy(alpha = 0.05f)
                                            )
                                            .clickable {
                                                val englishMonthName = when (monthIndex) {
                                                    1 -> "January"
                                                    2 -> "February"
                                                    3 -> "March"
                                                    4 -> "April"
                                                    5 -> "May"
                                                    6 -> "June"
                                                    7 -> "July"
                                                    8 -> "August"
                                                    9 -> "September"
                                                    10 -> "October"
                                                    11 -> "November"
                                                    else -> "December"
                                                }
                                                onToggleMonth(selectedYear, monthIndex, "$englishMonthName $selectedYear")
                                            }
                                            .padding(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "${BanglaDateUtils.toBanglaNumerals(monthIndex.toString())}. $banglaMonth",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = EmeraldPrimary
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = if (isPaid) "৫০০ ৳ জমা" else "০ ৳ অনাদায়ী",
                                                    fontSize = 10.sp,
                                                    color = if (isPaid) SuccessGreen else AlertRed
                                                )
                                            }

                                            // Visual Indicator dot
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .background(
                                                        if (isPaid) SuccessGreen else AlertRed,
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (isPaid) Icons.Default.Check else Icons.Default.AccessTime,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Spacer bottom
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Delete member button and main action row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = AlertRed)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("সদস্য বাদ দিন", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("বন্ধ করুন", color = Color.White)
                    }
                }
            }
        }
    }

    // Modal to type Tarabi input amount
    if (showTarabiInputDialog) {
        Dialog(onDismissRequest = { showTarabiInputDialog = false }) {
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
                        text = "তারাবির টাকা সংগ্রহ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "সদস্য: ${member.name}", fontSize = 12.sp, color = Color.Gray)
                    Text(text = "বছর: ${BanglaDateUtils.toBanglaNumerals(selectedYear.toString())}", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    OutlinedTextField(
                        value = tarabiAmountInput,
                        onValueChange = { tarabiAmountInput = it },
                        label = { Text("অর্থের পরিমাণ (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tarabi_amount_field"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTarabiInputDialog = false }) {
                            Text("বাতিল")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amtVal = tarabiAmountInput.toDoubleOrNull() ?: 0.0
                                onSaveTarabi(selectedYear, amtVal)
                                showTarabiInputDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("জমা করুন", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Modal delete confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("সদস্য ডিলিট নিশ্চিতকরণ") },
            text = { Text("আপনি কি নিশ্চিতভাবে এই সদস্য এবং তাঁর সমস্ত চাঁদার বিবরণী চিরতরে ডিলিট করতে চান?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteMember()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                ) {
                    Text("হ্যাঁ, ডিলিট করুন", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
fun LedgerStatsCard(
    totalPaid: Double,
    totalDue: Double,
    totalMonths: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldPrimary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(EmeraldPrimary, EmeraldPrimaryVariant)
                    )
                )
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.3f)) {
                Text(
                    text = "হুজুরের মোট সম্মানি পরিশোধ খতিয়ান",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = BanglaDateUtils.formatBDT(totalPaid),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldAccent
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.MonetizationOn,
                        contentDescription = "Months total icon",
                        tint = GoldAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "নথিভুক্ত সামগ্রিক মাস: ${BanglaDateUtils.toBanglaNumerals(totalMonths.toString())}",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.61f)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(60.dp)
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp)
            )

            Column(
                modifier = Modifier
                    .weight(0.9f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = "মোট অনাদায়ী বকেয়া",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = BanglaDateUtils.formatBDT(totalDue),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (totalDue > 0) AlertRed else SuccessGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .background(
                            if (totalDue > 0) AlertRed.copy(alpha = 0.25f) else SuccessGreen.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (totalDue > 0) "বকেয়া সচল" else "সম্পূর্ণ পরিশোধ",
                        fontSize = 9.sp,
                        color = if (totalDue > 0) Color.White else GoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SalaryRecordRow(
    record: SalaryRecord,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val due = record.baseSalary - record.paidAmount
    val badgeText: String
    val badgeBgColor: Color
    val badgeTextColor: Color

    when {
        due <= 0 -> {
            badgeText = "পরিশোধিত"
            badgeBgColor = SuccessGreen.copy(alpha = 0.15f)
            badgeTextColor = SuccessGreen
        }
        record.paidAmount > 0 -> {
            badgeText = "আংশিক পরিশোধ"
            badgeBgColor = GoldAccent.copy(alpha = 0.15f)
            badgeTextColor = GoldAccent
        }
        else -> {
            badgeText = "বকেয়া রয়েছে"
            badgeBgColor = AlertRed.copy(alpha = 0.15f)
            badgeTextColor = AlertRed
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .testTag("salary_row"),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Month and year date icon",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = record.monthName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                }

                // Status Badge turning auto-colored
                Box(
                    modifier = Modifier
                        .background(badgeBgColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "নির্ধারিত সম্মানি:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = BanglaDateUtils.formatBDT(record.baseSalary),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "প্রদত্ত পরিমাণ:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = BanglaDateUtils.formatBDT(record.paidAmount),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = SuccessGreen
                        )
                    }
                    if (due > 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "বকেয়ার পরিমান:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = BanglaDateUtils.formatBDT(due),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = AlertRed
                            )
                        }
                    }
                }

                // Edit pencil button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("edit_salary_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit payments",
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("delete_salary_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete record ledger",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateSalaryRecordDialog(
    onDismiss: () -> Unit,
    onSave: (year: Int, monthId: Int, monthName: String, baseSalary: Double, paidAmount: Double) -> Unit
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    var selectedYear by remember { mutableStateOf(currentYear.toString()) }
    var selectedMonthName by remember { mutableStateOf("May") }
    var baseSalaryStr by remember { mutableStateOf("18000") }
    var paidAmountStr by remember { mutableStateOf("18000") }
    var errors by remember { mutableStateOf("") }

    val monthsList = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    var showMonthDropdown by remember { mutableStateOf(false) }

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
                    text = "সম্মানি খতিয়ানে নতুন মাস",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Month Spinner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldPrimary.copy(alpha = 0.05f))
                        .clickable { showMonthDropdown = !showMonthDropdown }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "নির্বাচন করুন: $selectedMonthName", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        Icon(
                            imageVector = if (showMonthDropdown) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = "Drop spinner icon",
                            tint = EmeraldPrimary
                        )
                    }
                }

                // Expanded simple Grid of Months
                AnimatedVisibility(visible = showMonthDropdown) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val chunks = monthsList.chunked(4)
                        chunks.forEach { rowMonths ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                rowMonths.forEach { mName ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (selectedMonthName == mName) EmeraldPrimary else EmeraldPrimary.copy(alpha = 0.04f))
                                            .clickable {
                                                selectedMonthName = mName
                                                showMonthDropdown = false
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = mName.substring(0, 3),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedMonthName == mName) Color.White else EmeraldPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = selectedYear,
                    onValueChange = { selectedYear = it },
                    label = { Text("বছর (যেমন: 2026)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("salary_year_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        focusedLabelColor = EmeraldPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = baseSalaryStr,
                    onValueChange = { baseSalaryStr = it },
                    label = { Text("মাসিক মোট সম্মানি (BDT ৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("salary_base_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        focusedLabelColor = EmeraldPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = paidAmountStr,
                    onValueChange = { paidAmountStr = it },
                    label = { Text("প্রদত্ত সম্মানি (৳, পরিশোধিত)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("salary_paid_input"),
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
                            val baseSal = baseSalaryStr.toDoubleOrNull()
                            val paidAmt = paidAmountStr.toDoubleOrNull()
                            val yearVal = selectedYear.toIntOrNull()
                            val monthIdx = monthsList.indexOf(selectedMonthName) + 1

                            if (baseSalaryStr.isBlank() || paidAmountStr.isBlank() || selectedYear.isBlank()) {
                                errors = "সবগুলো ঘর পূরণ করুন।"
                            } else if (baseSal == null || baseSal < 0 || paidAmt == null || paidAmt < 0 || yearVal == null) {
                                errors = "সঠিক অংক প্রবেশ করান।"
                            } else if (paidAmt > baseSal) {
                                errors = "প্রদত্ত টাকা নির্ধারিত বেতনের চেয়ে বেশি হতে পারবে না।"
                            } else {
                                onSave(yearVal, monthIdx, "$selectedMonthName $yearVal", baseSal, paidAmt)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_salary_button")
                    ) {
                        Text("যোগ করুন", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun EditSalaryRecordDialog(
    record: SalaryRecord,
    onDismiss: () -> Unit,
    onSave: (baseSalary: Double, paidAmount: Double) -> Unit
) {
    var baseSalaryStr by remember { mutableStateOf(record.baseSalary.toInt().toString()) }
    var paidAmountStr by remember { mutableStateOf(record.paidAmount.toInt().toString()) }
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
                    text = "${record.monthName} भत्ता হালনাগাদ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = baseSalaryStr,
                    onValueChange = { baseSalaryStr = it; errors = "" },
                    label = { Text("নির্ধারিত মাসিক সম্মানি (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_salary_base_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        focusedLabelColor = EmeraldPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = paidAmountStr,
                    onValueChange = { paidAmountStr = it; errors = "" },
                    label = { Text("প্রদত্ত সম্মানি (৳, পরিশোধিত)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_salary_paid_input"),
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
                            val baseSal = baseSalaryStr.toDoubleOrNull()
                            val paidAmt = paidAmountStr.toDoubleOrNull()

                            if (baseSalaryStr.isBlank() || paidAmountStr.isBlank()) {
                                errors = "দয়া করে ঘরগুলো পূরণ করুন।"
                            } else if (baseSal == null || baseSal < 0 || paidAmt == null || paidAmt < 0) {
                                errors = "সঠিক টাকার পরিমাণ প্রবেশ করুন।"
                            } else if (paidAmt > baseSal) {
                                errors = "প্রদত্ত সম্মানি নির্ধারিত সম্মানির চেয়ে বেশি হতে পারবে না।"
                            } else {
                                onSave(baseSal, paidAmt)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_edit_salary_button")
                    ) {
                        Text("সংরক্ষণ করুন", color = Color.White)
                    }
                }
            }
        }
    }
}
