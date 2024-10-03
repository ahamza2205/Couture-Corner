package com.example.couturecorner.di

import com.example.couturecorner.model.remote.IremoteData
import com.example.couturecorner.model.remote.RemoteData
import com.example.couturecorner.model.repository.Irepo
import com.example.couturecorner.model.repository.Repo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)

object DataModule {

    // Provide IremoteData implementation
    @Provides
    fun provideRemoteData(): IremoteData {
        return RemoteData()
    }

    // Provide Irepo implementation
    @Provides
    fun provideRepo(remoteData: IremoteData): Irepo {
        return Repo(remoteData)
    }
}