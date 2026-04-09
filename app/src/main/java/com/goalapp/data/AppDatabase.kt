package com.goalapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [GoalEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "goals_db")
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE goals ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE goals ADD COLUMN archivedAt INTEGER")
            }
        }
        
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE goals ADD COLUMN notificationEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE goals ADD COLUMN notificationHour INTEGER")
                db.execSQL("ALTER TABLE goals ADD COLUMN notificationMinute INTEGER")
            }
        }
    }
}
