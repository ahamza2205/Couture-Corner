package com.example.couturecorner.di

import android.content.Context
import com.apollographql.apollo3.ApolloClient
import com.example.couturecorner.data.local.SharedPreference
import com.example.couturecorner.data.local.SharedPreferenceImp
import com.example.couturecorner.data.remote.CurrencyApiService
import com.example.couturecorner.data.remote.IremoteData
import com.example.couturecorner.data.remote.RemoteData
import com.example.couturecorner.data.repository.Irepo
import com.example.couturecorner.data.repository.Repo
import com.example.couturecorner.network.MyApolloClient
import com.google.firebase.auth.FirebaseAuth
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
    fun provideRemoteData(apolloClient: ApolloClient , sharedPreference: SharedPreference): IremoteData {
        return RemoteData(apolloClient, sharedPreference) // Ensure RemoteData is set up properly
    }

    // Provide SharedPreference implementation
    @Provides
    @Singleton
    fun provideSharedPreference(@ApplicationContext context: Context): SharedPreference {
        return SharedPreferenceImp(context)
    }

    @Provides
    @Singleton
    fun provideApolloClient(): ApolloClient {
        return MyApolloClient.apolloClient
    }
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    // Provide Irepo implementation
    @Provides
    @Singleton
    fun provideRepo(remoteData: IremoteData, sharedPreference: SharedPreference, currencyApi: CurrencyApiService): Irepo {
        return Repo(remoteData, sharedPreference, currencyApi)
    }

}


