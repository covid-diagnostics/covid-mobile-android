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
import com.example.coronadiagnosticapp.ui.activities.oxymeter.OxymeterActivity
import com.example.coronadiagnosticapp.ui.fragments.camera.CameraFragment
import com.example.coronadiagnosticapp.ui.fragments.dailtyMetric.DailyMetricFragment
import com.example.coronadiagnosticapp.ui.fragments.information.InformationFragment
import com.example.coronadiagnosticapp.ui.fragments.instruction.InstructionsFragment
import com.example.coronadiagnosticapp.ui.fragments.questions.QuestionFragment
import com.example.coronadiagnosticapp.ui.fragments.questions.QuestionnaireFragment
import com.example.coronadiagnosticapp.ui.fragments.recorder.RecorderFragment
import com.example.coronadiagnosticapp.ui.fragments.recorder.RecorderFragment2
import com.example.coronadiagnosticapp.ui.fragments.register.RegisterFragment
import com.example.coronadiagnosticapp.ui.fragments.resultFragment.ResultFragment
import com.example.coronadiagnosticapp.ui.fragments.welcome.WelcomeFragment
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
    fun inject(fragment: RecorderFragment2)
    fun inject(fragment: CameraFragment)
    fun inject(fragment: WelcomeFragment)
    fun inject(fragment: InstructionsFragment)
    fun inject(activity: OxymeterActivity)
    fun inject(fragment: QuestionnaireFragment)
    fun inject(fragment: QuestionFragment)

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
        sharedProvider: SharedProvider,
        tokenServiceInterceptor: TokenServiceInterceptor
    ): Repository =
        RepositoryImpl(networkDataSource, dbDao, sharedProvider, tokenServiceInterceptor)

    @Provides
    fun provideNetworkDataSource(api: ApiServer): NetworkDataSource = NetworkDataSourceImpl(api)

    @Singleton
    @Provides
    fun provideDatabase(context: Context): AppDatabase = AppDatabase.invoke(context)

    @Provides
    fun provideDatabaseDao(database: AppDatabase): DbDao = database.getUserDao()

    @Provides
    fun provideRetrofitApi(tokenServiceInterceptor: TokenServiceInterceptor): ApiServer =
        ApiServer.invoke(tokenServiceInterceptor)

    @Provides
    fun provideTokenProvider(context: Context): SharedProvider = SharedProviderImpl(context)

    @Provides
    @Singleton
    fun provideNetworkInterceptor(): TokenServiceInterceptor = TokenServiceInterceptor()
}