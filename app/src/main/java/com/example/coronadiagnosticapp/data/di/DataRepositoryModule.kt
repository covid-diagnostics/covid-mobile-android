package com.example.coronadiagnosticapp.data.di

import android.content.Context
import com.example.coronadiagnosticapp.data.db.AppDatabase
import com.example.coronadiagnosticapp.data.db.dao.DbDao
import com.example.coronadiagnosticapp.data.network.ApiServer
import com.example.coronadiagnosticapp.data.network.NetworkDataSource
import com.example.coronadiagnosticapp.data.network.NetworkDataSourceImpl
import com.example.coronadiagnosticapp.data.network.TokenServiceInterceptor
import com.example.coronadiagnosticapp.data.providers.SharedProvider
import com.example.coronadiagnosticapp.data.providers.SharedProviderImpl
import com.example.coronadiagnosticapp.data.repository.Repository
import com.example.coronadiagnosticapp.data.repository.RepositoryImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataRepositoryModule {
    @Provides
    fun provideDataRepository(
        networkDataSource: NetworkDataSource,
        dbDao: DbDao,
        sharedProvider: SharedProvider,
        tokenServiceInterceptor: TokenServiceInterceptor
    ): Repository =
        RepositoryImpl(
            networkDataSource,
            dbDao,
            sharedProvider,
            tokenServiceInterceptor
        )

    @Provides
    fun provideNetworkDataSource(api: ApiServer): NetworkDataSource =
        NetworkDataSourceImpl(api)

    @Singleton
    @Provides
    fun provideDatabase(context: Context): AppDatabase = AppDatabase(context)

    @Provides
    fun provideDatabaseDao(database: AppDatabase): DbDao = database.getUserDao()

    @Provides
    fun provideRetrofitApi(interceptor: TokenServiceInterceptor): ApiServer =
        ApiServer(interceptor)

    @Provides
    fun provideTokenProvider(context: Context): SharedProvider = SharedProviderImpl(context)

    @Provides
    @Singleton
    fun provideNetworkInterceptor(): TokenServiceInterceptor = TokenServiceInterceptor()
}