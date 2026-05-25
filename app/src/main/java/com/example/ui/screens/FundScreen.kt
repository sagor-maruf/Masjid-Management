package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Transaction
import com.example.ui.theme.AlertRed
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldPrimaryVariant
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LightSurface
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.MasjidViewModel
import com.example.utils.BanglaDateUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FundScreen(
    viewModel: MasjidViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.transactions.collectAsState()
    val totalBalance by viewModel.totalBalance.collectAsState()

    var showAddTxDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("ALL") } // "ALL", "INCOME", "EXPENSE"

    // Filter transactions based on query & selected type
    val filteredTransactions = remember(transactions, searchQuery, filterType) {
        transactions.filter { tx ->
            val matchQuery = tx.description.contains(searchQuery, ignoreCase = true)
            val matchType = when (filterType) {
                "INCOME" -> tx.type.equals("INCOME", ignoreCase = true)
                "EXPENSE" -> tx.type.equals("EXPENSE", ignoreCase = true)
                else -> true
            }
            matchQuery && matchType
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("fund_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // 1. Current Total Balance Card displaying elegant Bangla font
            item {
                BalanceDashboardHeader(
                    totalBalance = totalBalance,
                    transactionsList = transactions
                )
            }

            // 2. Transaction Search and Segmented Filters
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tx_search_input"),
                        shape = RoundedCornerShape(14.dp),
                        placeholder = { Text("খতিয়ান অনুসন্ধান করুন...", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.Search, contentDescription = "Search icon")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = EmeraldPrimary.copy(alpha = 0.2f)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Segmented filters (All, Income, Expense)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "ALL" to "সব খতিয়ান",
                            "INCOME" to "জমা (আয়)",
                            "EXPENSE" to "খরচ"
                        ).forEach { (code, label) ->
                            val isSelected = filterType == code
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) EmeraldPrimary else EmeraldPrimary.copy(alpha = 0.05f)
                                    )
                                    .clickable { filterType = code }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else EmeraldPrimary
                                )
                            }
                        }
                    }
                }
            }

            // 3. Transactions List Section
            if (filteredTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "No transaction",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) "কোনো মিল পাওয়া যায়নি।" else "ফান্ডে কোনো হিসেব নেই।",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            } else {
                items(filteredTransactions, key = { it.id }) { tx ->
                    TransactionItemRow(
                        tx = tx,
                        onDelete = { viewModel.removeTransaction(tx) }
                    )
                }
            }
        }

        // Floating Action Button (FAB) anchored safely
        FloatingActionButton(
            onClick = { showAddTxDialog = true },
            containerColor = GoldAccent,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp)
                .testTag("add_transaction_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add donation expense record",
                modifier = Modifier.size(28.dp)
            )
        }
    }

    // Modal Add Transaction
    if (showAddTxDialog) {
        AddTransactionDialog(
            onDismiss = { showAddTxDialog = false },
            onSave = { desc, type, amount ->
                viewModel.addTransaction(desc, type, amount)
                showAddTxDialog = false
            }
        )
    }
}

@Composable
fun BalanceDashboardHeader(
    totalBalance: Double,
    transactionsList: List<Transaction>
) {
    val totalIncome = transactionsList.filter { it.type.equals("INCOME", ignoreCase = true) }.sumOf { it.amount }
    val totalExpense = transactionsList.filter { it.type.equals("EXPENSE", ignoreCase = true) }.sumOf { it.amount }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(EmeraldPrimary, EmeraldPrimaryVariant)
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "হিসাব ফান্ডের বর্তমান ব্যালেন্স",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Beautiful Large BDT Representation
            Text(
                text = BanglaDateUtils.formatBDT(totalBalance),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldAccent,
                    fontSize = 32.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = Color.White.copy(alpha = 0.15f), thickness = 0.8.dp)

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-columns detailing Total Incomes vs Total Expenses inside the current set
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(SuccessGreen.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.TrendingUp,
                                contentDescription = "Income icon",
                                tint = SuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "মোট আয় ও অনুদান", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = BanglaDateUtils.formatBDT(totalIncome),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(Color.White.copy(alpha = 0.15f))
                )

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(AlertRed.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.TrendingDown,
                                contentDescription = "Expense icon",
                                tint = AlertRed,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "মোট ব্যয় ও খরচ", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = BanglaDateUtils.formatBDT(totalExpense),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItemRow(
    tx: Transaction,
    onDelete: () -> Unit
) {
    val isIncome = tx.type.equals("INCOME", ignoreCase = true)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .testTag("transaction_row"),
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
                // Color-coded Circle Type indicator (Green for Income, Red for Expense)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = if (isIncome) SuccessGreen.copy(alpha = 0.12f) else AlertRed.copy(alpha = 0.12f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncome) Icons.Outlined.TrendingUp else Icons.Outlined.TrendingDown,
                        contentDescription = if (isIncome) "Income Record" else "Expense Record",
                        tint = if (isIncome) SuccessGreen else AlertRed,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tx.description,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tx.dateString,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.61f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount + Delete Action layout
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${if (isIncome) "+" else "-"} ${BanglaDateUtils.formatBDT(tx.amount)}",
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isIncome) SuccessGreen else AlertRed,
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_tx_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete record",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// Transaction dialog card creation
@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onSave: (desc: String, type: String, amount: Double) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("INCOME") } // "INCOME" or "EXPENSE"
    var amountStr by remember { mutableStateOf("") }
    var errorStr by remember { mutableStateOf("") }

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
                    text = "হিসাব ফান্ডের এন্ট্রি ফরম",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Income or Expense toggle choices
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldPrimary.copy(alpha = 0.05f))
                        .padding(4.dp)
                ) {
                    listOf(
                        "INCOME" to "জমা ও অনুদান",
                        "EXPENSE" to "খরচ ও ব্যয়"
                    ).forEach { (code, label) ->
                        val isSelected = type == code
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) (if (code == "INCOME") SuccessGreen else AlertRed) else Color.Transparent
                                )
                                .clickable { type = code }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else EmeraldPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it; errorStr = "" },
                    label = { Text("খতিয়ান বর্ণনা / বিবরণী") },
                    placeholder = { Text("উদা: হাজী কাশেম সাহেবের দান()") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tx_desc_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        focusedLabelColor = EmeraldPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it; errorStr = "" },
                    label = { Text("খরচ বা আয়ের পরিমাণ (BDT ৳)") },
                    placeholder = { Text("উদা: ৫০০") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tx_amount_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        focusedLabelColor = EmeraldPrimary
                    )
                )

                if (errorStr.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorStr, color = AlertRed, fontSize = 12.sp)
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
                            val amt = amountStr.toDoubleOrNull()
                            if (description.isBlank() || amountStr.isBlank()) {
                                errorStr = "সমস্ত ঘর পূরণ করা আবশ্যক।"
                            } else if (amt == null || amt <= 0) {
                                errorStr = "সঠিক পরিমাণ প্রবেশ করুন (ধনাত্মক সংখ্যা)।"
                            } else {
                                onSave(description, type, amt)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_transaction_button")
                    ) {
                        Text("যুক্ত করুন", color = Color.White)
                    }
                }
            }
        }
    }
}
