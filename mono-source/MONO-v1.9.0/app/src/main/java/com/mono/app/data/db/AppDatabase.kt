package com.mono.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mono.app.data.model.Folder
import com.mono.app.data.model.Member
import com.mono.app.data.model.MemberDetails
import com.mono.app.data.model.Task

@Database(
    entities = [Member::class, Task::class, MemberDetails::class, Folder::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun memberDao(): MemberDao
    abstract fun taskDao(): TaskDao
    abstract fun memberDetailsDao(): MemberDetailsDao
    abstract fun folderDao(): FolderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `member_details` (
                        `id` TEXT NOT NULL,
                        `memberId` TEXT NOT NULL,
                        `fatherName` TEXT NOT NULL,
                        `nationalId` TEXT NOT NULL,
                        `nationalIdSerial` TEXT NOT NULL,
                        `birthDate` TEXT NOT NULL,
                        `birthPlace` TEXT NOT NULL,
                        `certNo` TEXT NOT NULL,
                        `certSerial` TEXT NOT NULL,
                        `phone` TEXT NOT NULL,
                        `operatorName` TEXT NOT NULL,
                        `simPuk` TEXT NOT NULL,
                        `accountNo` TEXT NOT NULL,
                        `cardNo` TEXT NOT NULL,
                        `cardExp` TEXT NOT NULL,
                        `cardCvv2` TEXT NOT NULL,
                        `cardPassword` TEXT NOT NULL,
                        `username` TEXT NOT NULL,
                        `password` TEXT NOT NULL,
                        `creditScore` TEXT NOT NULL,
                        `checkStatus` TEXT NOT NULL,
                        `errors` TEXT NOT NULL,
                        `freeCapacity` TEXT NOT NULL,
                        `activeLoans` TEXT NOT NULL,
                        `takenLoans` TEXT NOT NULL,
                        `collateral` TEXT NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`memberId`) REFERENCES `members`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_member_details_memberId` ON `member_details` (`memberId`)"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // folders
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `folders` (
                        `id` TEXT NOT NULL,
                        `parentId` TEXT,
                        `name` TEXT NOT NULL,
                        `avatarLetter` TEXT NOT NULL,
                        `avatarColor` TEXT NOT NULL,
                        `avatarImageUri` TEXT,
                        `sortOrder` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """
                )
                // members new columns
                db.execSQL("ALTER TABLE `members` ADD COLUMN `avatarImageUri` TEXT")
                db.execSQL("ALTER TABLE `members` ADD COLUMN `folderId` TEXT")
                // member_details new columns
                val cols = listOf(
                    "postalCode TEXT NOT NULL DEFAULT ''",
                    "landlinePhone TEXT NOT NULL DEFAULT ''",
                    "sanaStatus TEXT NOT NULL DEFAULT ''",
                    "digitalBranch TEXT NOT NULL DEFAULT ''",
                    "currentBranch TEXT NOT NULL DEFAULT ''",
                    "digitalSignature TEXT NOT NULL DEFAULT ''",
                    "checkCertSymbol TEXT NOT NULL DEFAULT ''",
                    "tokenStatus TEXT NOT NULL DEFAULT ''",
                    "creditReportUri TEXT NOT NULL DEFAULT ''",
                    "balanceStatus TEXT NOT NULL DEFAULT ''",
                    "shenasnamehPhotoUri TEXT NOT NULL DEFAULT ''",
                    "nationalCardPhotoUri TEXT NOT NULL DEFAULT ''",
                    "employmentCertPhotoUri TEXT NOT NULL DEFAULT ''",
                    "payslipPhotoUri TEXT NOT NULL DEFAULT ''",
                    "extraDocsUris TEXT NOT NULL DEFAULT ''"
                )
                cols.forEach { col ->
                    // col is like "postalCode TEXT NOT NULL DEFAULT ''"
                    db.execSQL("ALTER TABLE `member_details` ADD COLUMN $col")
                }
            }
        }

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mono.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
