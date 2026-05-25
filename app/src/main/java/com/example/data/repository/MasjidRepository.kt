package com.example.data.repository

import com.example.data.db.NoticeDao
import com.example.data.db.PrayerTimeDao
import com.example.data.db.SalaryRecordDao
import com.example.data.db.TransactionDao
import com.example.data.db.MemberDao
import com.example.data.db.MemberPaymentDao
import com.example.data.model.Notice
import com.example.data.model.PrayerTime
import com.example.data.model.SalaryRecord
import com.example.data.model.Transaction
import com.example.data.model.Member
import com.example.data.model.MemberPayment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MasjidRepository(
    private val noticeDao: NoticeDao,
    private val transactionDao: TransactionDao,
    private val salaryRecordDao: SalaryRecordDao,
    private val prayerTimeDao: PrayerTimeDao,
    private val memberDao: MemberDao,
    private val memberPaymentDao: MemberPaymentDao
) {
    val allNotices: Flow<List<Notice>> = noticeDao.getAllNotices()
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val allSalaryRecords: Flow<List<SalaryRecord>> = salaryRecordDao.getAllSalaryRecords()
    val allPrayerTimes: Flow<List<PrayerTime>> = prayerTimeDao.getAllPrayerTimes()
    val allMembers: Flow<List<Member>> = memberDao.getAllMembers()
    val allMemberPayments: Flow<List<MemberPayment>> = memberPaymentDao.getAllPayments()

    suspend fun insertNotice(notice: Notice) = noticeDao.insertNotice(notice)
    suspend fun deleteNotice(notice: Notice) = noticeDao.deleteNotice(notice)

    suspend fun insertTransaction(transaction: Transaction) = transactionDao.insertTransaction(transaction)
    suspend fun deleteTransaction(transaction: Transaction) = transactionDao.deleteTransaction(transaction)

    suspend fun insertSalaryRecord(record: SalaryRecord) = salaryRecordDao.insertSalaryRecord(record)
    suspend fun deleteSalaryRecord(record: SalaryRecord) = salaryRecordDao.deleteSalaryRecord(record)

    suspend fun insertMember(member: Member) = memberDao.insertMember(member)
    suspend fun deleteMember(member: Member) = memberDao.deleteMember(member)

    suspend fun insertMemberPayment(payment: MemberPayment) = memberPaymentDao.insertPayment(payment)
    suspend fun deleteMemberPayment(payment: MemberPayment) = memberPaymentDao.deletePayment(payment)

    suspend fun insertPrayerTime(prayerTime: PrayerTime) = prayerTimeDao.insertPrayerTime(prayerTime)

    // Prepopulate database with realistic mosque details if they don't already exist
    suspend fun prepopulateIfEmpty() {
        // 1. Prepopulate Prayer Times
        val currentPrayerTimes = allPrayerTimes.first()
        if (currentPrayerTimes.isEmpty()) {
            val defaultTimes = listOf(
                PrayerTime("fajr", "Fajr", "ফজর", "04:15 AM", "Iqamah: 04:30 AM"),
                PrayerTime("dhuhr", "Dhuhr", "যোহর", "12:15 PM", "Iqamah: 12:30 PM"),
                PrayerTime("asr", "Asr", "আসর", "04:45 PM", "Iqamah: 05:00 PM"),
                PrayerTime("maghrib", "Maghrib", "মাগরিব", "06:45 PM", "Iqamah: 06:50 PM"),
                PrayerTime("isha", "Isha", "এশা", "08:15 PM", "Iqamah: 08:30 PM"),
                PrayerTime("jumuah", "Jumu'ah", "জুমু’আ", "01:30 PM", "Khutbah: 01:15 PM")
            )
            for (pt in defaultTimes) {
                prayerTimeDao.insertPrayerTime(pt)
            }
        }

        // 2. Prepopulate Notices
        val currentNotices = allNotices.first()
        if (currentNotices.isEmpty()) {
            val formatter = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.US)
            val currentDateStr = formatter.format(Date())
            
            val defaultNotices = listOf(
                Notice(
                    title = "জুমু'আর খুৎবাহ ও জামাত সংক্রান্ত নোটিশ",
                    content = "আসসালামু আলাইকুম। আগামী শুক্রবার জুমু'আর খুতবা শুরু হবে দুপুর ১:১৫ মিনিটে। সম্মানিত মুসল্লিগণকে অজু ও সুন্নত নামাজ আদায়ের সুবিধার্থে খুতবার পূর্বেই মসজিদে আসার জন্য বিশেষভাবে আহ্বান জানানো যাচ্ছে।",
                    dateString = currentDateStr
                ),
                Notice(
                    title = "মসজিদ সংস্কার ফান্ডে অনুদান প্রদান",
                    content = "সম্মানিত সুধী, আমাদের মসজিদের নীচ তলার সংস্কার কাজের জন্য রড এবং সিমেন্ট ক্রয়ের উদ্দেশ্যে অনুদান সংগ্রহ করা হচ্ছে। আপনারা আপনাদের দান কমিটির কোষাধ্যক্ষের নিকট অথবা 'ফান্ড ট্র্যাকিং' ফর্মটির মাধ্যমে সরাসরি ফান্ড খতিয়ানে যুক্ত করতে পারেন।",
                    dateString = currentDateStr
                )
            )
            for (notice in defaultNotices) {
                noticeDao.insertNotice(notice)
            }
        }

        // 3. Prepopulate Transactions
        val currentTx = allTransactions.first()
        if (currentTx.isEmpty()) {
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.US)
            val baseTime = System.currentTimeMillis()
            
            val defaultTx = listOf(
                Transaction(
                    description = "জুমাআ'র দান বাক্সের সংগ্রহ",
                    type = "INCOME",
                    amount = 14850.0,
                    dateString = formatter.format(Date(baseTime)),
                    timestamp = baseTime
                ),
                Transaction(
                    description = "মসজিদের বিদ্যুৎ বিল পরিশোধ (মে ২০২৬)",
                    type = "EXPENSE",
                    amount = 4520.0,
                    dateString = formatter.format(Date(baseTime - 86400000)),
                    timestamp = baseTime - 86400000
                ),
                Transaction(
                    description = "হাফিজ সুলাইমান সাহেব (হুজুর বেতন)",
                    type = "EXPENSE",
                    amount = 18000.0,
                    dateString = formatter.format(Date(baseTime - (86400000 * 3))),
                    timestamp = baseTime - (86400000 * 3)
                ),
                Transaction(
                    description = "হাজী মোশাররফ সাহেব (বিশেষ অনুদান)",
                    type = "INCOME",
                    amount = 25000.0,
                    dateString = formatter.format(Date(baseTime - (86400000 * 5))),
                    timestamp = baseTime - (86400000 * 5)
                ),
                Transaction(
                    description = "মসজিদের মাইক মেরামত খরচ",
                    type = "EXPENSE",
                    amount = 1200.0,
                    dateString = formatter.format(Date(baseTime - (86400000 * 7))),
                    timestamp = baseTime - (86400000 * 7)
                )
            )
            for (tx in defaultTx) {
                transactionDao.insertTransaction(tx)
            }
        }

        // 4. Prepopulate Salary Records
        val currentSalary = allSalaryRecords.first()
        if (currentSalary.isEmpty()) {
            val defaultSalary = listOf(
                SalaryRecord(year = 2026, month = 6, monthName = "June 2026", baseSalary = 18000.0, paidAmount = 0.0),
                SalaryRecord(year = 2026, month = 5, monthName = "May 2026", baseSalary = 18000.0, paidAmount = 18000.0),
                SalaryRecord(year = 2026, month = 4, monthName = "April 2026", baseSalary = 18000.0, paidAmount = 12000.0),
                SalaryRecord(year = 2026, month = 3, monthName = "March 2026", baseSalary = 15000.0, paidAmount = 15000.0)
            )
            for (salary in defaultSalary) {
                salaryRecordDao.insertSalaryRecord(salary)
            }
        }

        // 5. Prepopulate exactly 40 members
        val currentMembers = allMembers.first()
        if (currentMembers.isEmpty()) {
            val memberNames = listOf(
                "মো. আব্দুল কুদ্দুস", "আলহাজ্ব মোশাররফ হোসেন", "হাজী মো. লোকমান আলী", "ডা. শফিকুল ইসলাম",
                "আলহাজ্ব নূরুল আমিন", "মো. রফিকুল ইসলাম", "মো. মফিজ উদ্দিন", "মো. আনিসুর রহমান",
                "মো. শাহজাহান মিয়া", "মো. আবুল হাশেম", "মো. জয়নাল আবেদীন", "মো. মোস্তফা কামাল",
                "মো. শামসুল হক", "মো. নুরুল ইসলাম", "মো. আব্দুল লতিফ", "মো. সিদ্দিকুর রহমান",
                "মো. হারুন অর রশীদ", "মো. গিয়াস উদ্দিন", "মো. রফিক আহমেদ", "মো. মোজাম্মেল হক",
                "মো. সাখাওয়াত হোসেন", "মো. আশরাফুল আলম", "মো. মাহমুদুল হাসান", "মো. জিয়াউর রহমান",
                "মো. আমিনুল ইসলাম", "মো. মশিউর রহমান", "মো. রেজাউল করিম", "মো. নজরুল ইসলাম",
                "মো. কামরুল হাসান", "মো. সিরাজুল ইসলাম", "মো. মাহফুজুর রহমান", "মো. আতিকুর রহমান",
                "মো. শফিকুর রহমান", "মো. হুমায়ুন কবির", "মো. জাহেদুল ইসলাম", "মো. আশিকুর রহমান",
                "মো. এমদাদুল হক", "মো. আনোয়ার হোসেন", "মো. মতিউর রহমান", "মো. খলিলুর রহমান"
            )
            val sectors = listOf("গুলশান রোড", "মসজিদ রোড", "মসজিদ লেন", "মেইন রোড", "পূর্ব পাড়া", "পশ্চিম পাড়া", "দক্ষিণ পাড়া", "উত্তর পাড়া", "দীঘির পাড়", "বাজার লেন")
            
            for (i in memberNames.indices) {
                val name = memberNames[i]
                val sector = sectors[i % sectors.size]
                val phone = "017" + String.format(Locale.US, "%08d", (10000000 + i * 2315) % 90000000 + 10000000)
                val memberId = memberDao.insertMember(Member(name = name, phone = phone, address = sector))
                
                // Add some realistic payments for 2026
                if (i < 15) {
                    memberPaymentDao.insertPayment(MemberPayment(memberId = memberId.toInt(), year = 2026, month = 1, monthName = "January 2026", salaryAmountPaid = 500.0, salaryStatus = "PAID"))
                    memberPaymentDao.insertPayment(MemberPayment(memberId = memberId.toInt(), year = 2026, month = 2, monthName = "February 2026", salaryAmountPaid = 500.0, salaryStatus = "PAID"))
                    memberPaymentDao.insertPayment(MemberPayment(memberId = memberId.toInt(), year = 2026, month = 3, monthName = "March 2026", salaryAmountPaid = 500.0, salaryStatus = "PAID"))
                    if (i % 2 == 0) {
                        memberPaymentDao.insertPayment(MemberPayment(memberId = memberId.toInt(), year = 2026, month = 4, monthName = "April 2026", salaryAmountPaid = 0.0, salaryStatus = "DUE", tarabiAmountPaid = 1000.0))
                    }
                } else if (i < 30) {
                    memberPaymentDao.insertPayment(MemberPayment(memberId = memberId.toInt(), year = 2026, month = 1, monthName = "January 2026", salaryAmountPaid = 500.0, salaryStatus = "PAID"))
                }
            }
        }
    }
}
