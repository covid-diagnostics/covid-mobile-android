package com.example.coronadiagnosticapp.data.di

import android.content.Context
import com.example.coronadiagnosticapp.data.db.AppDatabase
import com.example.coronadiagnosticapp.data.db.dao.DbDao
import com.example.coronadiagnosticapp.data.network.ApiServer
import com.example.coronadiagnosticapp.data.network.NetworkDataSource
import com.example.coronadiagnosticapp.data.network.NetworkDataSourceImpl
import com.example.coronadiagnosticapp.data.network.TokenServiceInterceptor
import com.example.coronadiagnosticapp.data.providers.TokenProvider
import com.example.coronadiagnosticapp.data.providers.TokenProviderImpl
import com.example.coronadiagnosticapp.data.repository.Repository
import com.example.coronadiagnosticapp.data.repository.RepositoryImpl
import com.example.coronadiagnosticapp.ui.fragments.camera.CameraFragment
import com.example.coronadiagnosticapp.ui.fragments.dailtyMetric.DailyMetricFragment
import com.example.coronadiagnosticapp.ui.fragments.information.InformationFragment
import com.example.coronadiagnosticapp.ui.fragments.recorder.RecorderFragment
import com.example.coronadiagnosticapp.ui.fragments.register.RegisterFragment
import com.example.coronadiagnosticapp.ui.fragments.resultFragment.ResultFragment
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Component(modules = [DataRepositoryModule::class])
@Singleton
interface AppComponent {

    fun inject(fragment: RegisterFragment)
    fun inject(fragment: InformationFragment)
    fun inject(fragment: DailyMetricFragment)
    fun inject(fragment: ResultFragment)
    fun inject(fragment: RecorderFragment)
    fun inject(fragment: CameraFragment)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent

    }


}

@Module
class DataRepositoryModule {
    @Provides
    fun provideDataRepository(
        networkDataSource: NetworkDataSource,
        dbDao: DbDao,
        tokenProvider: TokenProvider,
        tokenServiceInterceptor: TokenServiceInterceptor
    ): Repository =
        RepositoryImpl(networkDataSource, dbDao, tokenProvider, tokenServiceInterceptor)

    @Provides
    fun provideNetworkDataSource(api: ApiServer): NetworkDataSource = NetworkDataSourceImpl(api)

    @Singleton
    @Provides
    fun provideDatabase(context: Context): AppDatabase = AppDatabase.invoke(context)

    @Provides
    fun provideDatabaseDao(database: AppDatabase): DbDao = database.getUserDao()

    @Provides
    fun provideRetrofitApi(tokenServiceInterceptor: TokenServiceInterceptor): ApiServer {
        return ApiServer.invoke(tokenServiceInterceptor)
    }

    @Provides
    fun provideTokenProvider(context: Context): TokenProvider = TokenProviderImpl(context)

    @Provides
    @Singleton
    fun provideNetworkInterceptor(): TokenServiceInterceptor {
        return TokenServiceInterceptor()
    }


}