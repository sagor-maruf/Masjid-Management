package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Notice
import com.example.data.model.PrayerTime
import com.example.data.model.SalaryRecord
import com.example.data.model.Transaction
import com.example.data.model.Member
import com.example.data.model.MemberPayment
import kotlinx.coroutines.flow.Flow

@Dao
interface NoticeDao {
    @Query("SELECT * FROM notices ORDER BY timestamp DESC")
    fun getAllNotices(): Flow<List<Notice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: Notice)

    @Delete
    suspend fun deleteNotice(notice: Notice)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
}

@Dao
interface SalaryRecordDao {
    @Query("SELECT * FROM salary_records ORDER BY year DESC, month DESC")
    fun getAllSalaryRecords(): Flow<List<SalaryRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalaryRecord(record: SalaryRecord)

    @Delete
    suspend fun deleteSalaryRecord(record: SalaryRecord)
}

@Dao
interface PrayerTimeDao {
    @Query("SELECT * FROM prayer_times")
    fun getAllPrayerTimes(): Flow<List<PrayerTime>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerTime(prayerTime: PrayerTime)
}

@Dao
interface MemberDao {
    @Query("SELECT * FROM members ORDER BY id ASC")
    fun getAllMembers(): Flow<List<Member>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member): Long

    @Delete
    suspend fun deleteMember(member: Member)
}

@Dao
interface MemberPaymentDao {
    @Query("SELECT * FROM member_payments")
    fun getAllPayments(): Flow<List<MemberPayment>>

    @Query("SELECT * FROM member_payments WHERE memberId = :memberId")
    fun getPaymentsByMember(memberId: Int): Flow<List<MemberPayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: MemberPayment)

    @Delete
    suspend fun deletePayment(payment: MemberPayment)
}

