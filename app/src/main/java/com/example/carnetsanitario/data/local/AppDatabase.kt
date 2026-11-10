package com.example.carnetsanitario.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.carnetsanitario.data.local.dao.DesparasitacionDao
import com.example.carnetsanitario.data.local.dao.MascotaDao
import com.example.carnetsanitario.data.local.dao.TutorDao
import com.example.carnetsanitario.data.local.dao.VacunaDao
import com.example.carnetsanitario.data.local.entity.DesparasitacionEntity
import com.example.carnetsanitario.data.local.entity.MascotaEntity
import com.example.carnetsanitario.data.local.entity.TutorEntity
import com.example.carnetsanitario.data.local.entity.VacunaEntity

@Database(
    entities = [
        TutorEntity::class,
        MascotaEntity::class,
        VacunaEntity::class,
        DesparasitacionEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tutorDao(): TutorDao
    abstract fun mascotaDao(): MascotaDao
    abstract fun vacunaDao(): VacunaDao
    abstract fun desparasitacionDao(): DesparasitacionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "carnet_sanitario_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}