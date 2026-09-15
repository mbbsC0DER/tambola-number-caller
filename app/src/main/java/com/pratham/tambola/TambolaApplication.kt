package com.pratham.tambola

import android.app.Application
import androidx.room.Room
import com.pratham.tambola.data.AppDatabase
import com.pratham.tambola.data.RoomSessionRepository
import com.pratham.tambola.data.SettingsRepository

class TambolaApplication : Application() {
    val database by lazy { Room.databaseBuilder(this, AppDatabase::class.java, "tambola.db").build() }
    val sessions by lazy { RoomSessionRepository(database) }
    val settings by lazy { SettingsRepository(this) }
}
