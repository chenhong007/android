package com.example.time.di

import android.content.Context
import com.example.time.data.database.TimeDatabase
import com.example.time.data.dao.*
import com.example.time.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt 应用模块
 * 提供全局依赖
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideTimeDatabase(
        @ApplicationContext context: Context
    ): TimeDatabase {
        return TimeDatabase.getInstance(context)
    }
    
    // Provide DAOs
    @Provides
    @Singleton
    fun provideUsageTrackingDao(database: TimeDatabase): UsageTrackingDao {
        return database.usageTrackingDao()
    }
    
    @Provides
    @Singleton
    fun provideNotificationRecordDao(database: TimeDatabase): NotificationRecordDao {
        return database.notificationRecordDao()
    }
    
    @Provides
    @Singleton
    fun provideUserSettingsDao(database: TimeDatabase): UserSettingsDao {
        return database.userSettingsDao()
    }
    
    @Provides
    @Singleton
    fun provideScreenEventDao(database: TimeDatabase): ScreenEventDao {
        return database.screenEventDao()
    }
    
    // Provide Repositories
    @Provides
    @Singleton
    fun provideScreenEventRepository(
        screenEventDao: ScreenEventDao
    ): ScreenEventRepository {
        return ScreenEventRepository(screenEventDao)
    }
    
    @Provides
    @Singleton
    fun provideUsageRepository(
        usageTrackingDao: UsageTrackingDao,
        screenEventRepository: ScreenEventRepository
    ): UsageRepository {
        return UsageRepository(usageTrackingDao, screenEventRepository)
    }
    
    @Provides
    @Singleton
    fun provideNotificationRepository(
        notificationRecordDao: NotificationRecordDao
    ): NotificationRepository {
        return NotificationRepository(notificationRecordDao)
    }
    
    @Provides
    @Singleton
    fun provideSettingsRepository(
        userSettingsDao: UserSettingsDao
    ): SettingsRepository {
        return SettingsRepository(userSettingsDao)
    }
}
