package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.Notice
import com.example.data.model.PrayerTime
import com.example.data.model.SalaryRecord
import com.example.data.model.Transaction
import com.example.data.model.Member
import com.example.data.model.MemberPayment
import com.example.data.repository.MasjidRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MasjidViewModel(
    application: Application,
    private val repository: MasjidRepository
) : AndroidViewModel(application) {

    init {
        // Prepopulate the DB in a background thread if lists are empty
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }
    }

    // Reactive StateFlows from Room
    val notices: StateFlow<List<Notice>> = repository.allNotices
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val transactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val salaryRecords: StateFlow<List<SalaryRecord>> = repository.allSalaryRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val prayerTimes: StateFlow<List<PrayerTime>> = repository.allPrayerTimes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val members: StateFlow<List<Member>> = repository.allMembers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val memberPayments: StateFlow<List<MemberPayment>> = repository.allMemberPayments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Derived reactive StateFlow for Net Account Balance in BDT
    val totalBalance: StateFlow<Double> = repository.allTransactions
        .map { txList ->
            txList.sumOf { tx ->
                if (tx.type.equals("INCOME", ignoreCase = true)) {
                    tx.amount
                } else {
                    -tx.amount
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    // Notice Board Operations
    fun postNotice(title: String, content: String) {
        val formatter = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.US)
        val dateStr = formatter.format(Date())
        viewModelScope.launch {
            repository.insertNotice(
                Notice(
                    title = title,
                    content = content,
                    dateString = dateStr
                )
            )
        }
    }

    fun removeNotice(notice: Notice) {
        viewModelScope.launch {
            repository.deleteNotice(notice)
        }
    }

    // Fund Tracking Operations
    fun addTransaction(description: String, type: String, amount: Double) {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val dateStr = formatter.format(Date())
        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    description = description,
                    type = type.uppercase(Locale.US),
                    amount = amount,
                    dateString = dateStr
                )
            )
        }
    }

    fun removeTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    // Salary tracking Operations
    fun addOrUpdateSalaryRecord(year: Int, monthId: Int, monthName: String, baseSalary: Double, paidAmount: Double) {
        viewModelScope.launch {
            // Check if record exists for this month/year to update it or insert new
            val existing = salaryRecords.value.find { it.year == year && it.month == monthId }
            val record = if (existing != null) {
                existing.copy(
                    baseSalary = baseSalary,
                    paidAmount = paidAmount,
                    lastUpdated = System.currentTimeMillis()
                )
            } else {
                SalaryRecord(
                    year = year,
                    month = monthId,
                    monthName = monthName,
                    baseSalary = baseSalary,
                    paidAmount = paidAmount
                )
            }
            repository.insertSalaryRecord(record)
        }
    }

    fun removeSalaryRecord(record: SalaryRecord) {
        viewModelScope.launch {
            repository.deleteSalaryRecord(record)
        }
    }

    // Prayer settings controller
    fun updatePrayerTime(id: String, englishName: String, bengaliName: String, time: String, iqamahTime: String) {
        viewModelScope.launch {
            repository.insertPrayerTime(
                PrayerTime(
                    id = id,
                    englishName = englishName,
                    bengaliName = bengaliName,
                    time = time,
                    iqamahTime = iqamahTime
                )
            )
        }
    }

    // Member Operations
    fun addMember(name: String, phone: String, address: String) {
        viewModelScope.launch {
            repository.insertMember(Member(name = name, phone = phone, address = address))
        }
    }

    fun removeMember(member: Member) {
        viewModelScope.launch {
            repository.deleteMember(member)
        }
    }

    // Member Payment toggler and logger
    fun toggleSalaryStatus(memberId: Int, year: Int, month: Int, monthName: String, rate: Double = 500.0) {
        viewModelScope.launch {
            val existing = memberPayments.value.find { it.memberId == memberId && it.year == year && it.month == month }
            val updated = if (existing != null) {
                if (existing.salaryStatus == "PAID") {
                    existing.copy(salaryStatus = "DUE", salaryAmountPaid = 0.0, lastUpdated = System.currentTimeMillis())
                } else {
                    existing.copy(salaryStatus = "PAID", salaryAmountPaid = rate, lastUpdated = System.currentTimeMillis())
                }
            } else {
                MemberPayment(
                    memberId = memberId,
                    year = year,
                    month = month,
                    monthName = monthName,
                    salaryAmountPaid = rate,
                    salaryStatus = "PAID"
                )
            }
            repository.insertMemberPayment(updated)
        }
    }

    fun updateTarabiAmount(memberId: Int, year: Int, amount: Double) {
        viewModelScope.launch {
            val existing = memberPayments.value.find { it.memberId == memberId && it.year == year && it.month == 13 }
            val updated = if (existing != null) {
                existing.copy(
                    tarabiAmountPaid = amount,
                    lastUpdated = System.currentTimeMillis()
                )
            } else {
                MemberPayment(
                    memberId = memberId,
                    year = year,
                    month = 13,
                    monthName = "তারাবি অনুদান",
                    tarabiAmountPaid = amount,
                    salaryStatus = "PAID"
                )
            }
            repository.insertMemberPayment(updated)
        }
    }
}

// Simple Factory for constructing ViewModel with dependencies
class MasjidViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MasjidViewModel::class.java)) {
            val database = AppDatabase.getDatabase(application)
            val repository = MasjidRepository(
                noticeDao = database.noticeDao(),
                transactionDao = database.transactionDao(),
                salaryRecordDao = database.salaryRecordDao(),
                prayerTimeDao = database.prayerTimeDao(),
                memberDao = database.memberDao(),
                memberPaymentDao = database.memberPaymentDao()
            )
            @Suppress("UNCHECKED_CAST")
            return MasjidViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
