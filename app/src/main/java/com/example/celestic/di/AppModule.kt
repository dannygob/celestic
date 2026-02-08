package com.example.celestic.di

import android.content.Context
import com.example.celestic.manager.AprilTagManager
import com.example.celestic.manager.ArUcoManager
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

    @Provides
    @Singleton
    fun provideImageClassifier(@ApplicationContext context: Context): ImageClassifier {
        return ImageClassifier(context)
    }

    @Provides
    @Singleton
    fun provideArUcoManager(): ArUcoManager {
        return ArUcoManager()
    }

    @Provides
    @Singleton
    fun provideAprilTagManager(): AprilTagManager {
        return AprilTagManager()
    }
}
