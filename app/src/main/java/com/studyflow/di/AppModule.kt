package com.studyflow.di

import android.content.Context
import com.studyflow.data.local.database.AppDatabase
import com.studyflow.data.local.dao.DeckDao
import com.studyflow.data.repository.DeckRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideDao(database: AppDatabase): DeckDao {
        return database.deckDao()
    }

    @Provides
    @Singleton
    fun provideRepository(dao: DeckDao): DeckRepository {
        return DeckRepository(dao)
    }
}
