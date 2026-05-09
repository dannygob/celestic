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

/**
 * Hilt dependency injection module that provides application‑wide singletons.
 *
 * This module supplies the Room database, DAO, and the ImageClassifier.
 * SharedDataRepository does not need a provider because it uses @Inject
 * in its constructor and Hilt can create it automatically.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ===== DATABASE =====

    /**
     * Provides a singleton instance of the CelesticDatabase.
     * The database is created using the application context.
     */
    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): CelesticDatabase {
        return CelesticDatabase.getDatabase(context)
    }

    /**
     * Provides the CelesticDao from the database instance.
     * This DAO exposes all Room operations for the app.
     */
    @Provides
    fun provideCelesticDao(database: CelesticDatabase): com.example.celestic.data.dao.CelesticDao {
        return database.celesticDao()
    }

    // ===== IMAGE CLASSIFIER =====

    /**
     * Provides a singleton instance of the ImageClassifier.
     * This classifier loads ML models and performs image analysis.
     */
    @Singleton
    @Provides
    fun provideImageClassifier(@ApplicationContext context: Context): ImageClassifier {
        return ImageClassifier(context)
    }

    // SharedDataRepository does not require a @Provides method
    // because Hilt can construct it automatically using @Inject.
}
