package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Notice
import com.example.data.model.PrayerTime
import com.example.data.model.SalaryRecord
import com.example.data.model.Transaction
import com.example.data.model.Member
import com.example.data.model.MemberPayment

@Database(
    entities = [
        Notice::class,
        Transaction::class,
        SalaryRecord::class,
        PrayerTime::class,
        Member::class,
        MemberPayment::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noticeDao(): NoticeDao
    abstract fun transactionDao(): TransactionDao
    abstract fun salaryRecordDao(): SalaryRecordDao
    abstract fun prayerTimeDao(): PrayerTimeDao
    abstract fun memberDao(): MemberDao
    abstract fun memberPaymentDao(): MemberPaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "masjid_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
