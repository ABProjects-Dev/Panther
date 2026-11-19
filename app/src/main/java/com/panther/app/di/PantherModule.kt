package com.panther.app.di

import android.content.Context
import androidx.room.Room
import com.panther.app.data.local.MIGRATION_1_2
import com.panther.app.data.local.PantherDatabase
import com.panther.app.data.repository.RoomPantherRepository
import com.panther.app.domain.repository.PantherRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun repository(implementation: RoomPantherRepository): PantherRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton fun database(@ApplicationContext context: Context): PantherDatabase =
        Room.databaseBuilder(context, PantherDatabase::class.java, "panther.db")
            // Explicit migration: the schema gate in `AGENTS.project.md` forbids destructive upgrades.
            .addMigrations(MIGRATION_1_2)
            .build()
    @Provides fun dao(database: PantherDatabase) = database.dao()
}
