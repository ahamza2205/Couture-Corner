package com.example.couturecorner.di

import android.content.Context
import com.example.couturecorner.data.local.SharedPreference
import com.example.couturecorner.data.remote.IremoteData
import com.example.couturecorner.data.remote.RemoteData
import com.example.couturecorner.data.repository.Irepo
import com.example.couturecorner.data.repository.Repo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    // Provide IremoteData implementation
    @Provides
    @Singleton
    fun provideRemoteData(): IremoteData {
        return RemoteData()
    }

    // Provide Irepo implementation
    @Provides
    @Singleton
    fun provideRepo(remoteData: IremoteData, sharedPreference: SharedPreference): Irepo {
        return Repo(remoteData, sharedPreference)
    }


    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreference {
        return SharedPreference(context)
    }
}