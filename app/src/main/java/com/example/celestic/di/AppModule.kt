package com.example.celestic.di

import android.content.Context
import com.example.celestic.database.CelesticDatabase
import com.example.celestic.manager.ImageClassifier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ===== DATABASE =====
    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): CelesticDatabase {
        return CelesticDatabase.getDatabase(context)
    }

    @Provides
    fun provideCelesticDao(database: CelesticDatabase): com.example.celestic.data.dao.CelesticDao {
        return database.celesticDao()
    }

    // ===== IMAGE CLASSIFIER =====
    @Singleton
    @Provides
    fun provideImageClassifier(@ApplicationContext context: Context): ImageClassifier {
        return ImageClassifier(context)
    }

    // NO necesitamos provides para SharedDataRepository porque ya tiene @Inject
    // Hilt lo maneja automáticamente
}
