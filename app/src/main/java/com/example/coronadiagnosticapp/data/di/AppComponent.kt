package com.example.coronadiagnosticapp.data.di

import android.content.Context
import com.example.coronadiagnosticapp.ui.fragments.oxymeter.OxymeterFragment
import com.example.coronadiagnosticapp.ui.fragments.camera.CameraFragment
import com.example.coronadiagnosticapp.ui.fragments.dailtyMetric.DailyMetricFragment
import com.example.coronadiagnosticapp.ui.fragments.information.BackgroundDiseasesFragment
import com.example.coronadiagnosticapp.ui.fragments.home.HomeFragment
import com.example.coronadiagnosticapp.ui.fragments.information.InformationFragment
import com.example.coronadiagnosticapp.ui.fragments.notification.NotificationFragment
import com.example.coronadiagnosticapp.ui.fragments.onboarding.ConsentFormFragment
import com.example.coronadiagnosticapp.ui.fragments.questions.QuestionFragment
import com.example.coronadiagnosticapp.ui.fragments.questions.QuestionnaireFragment
import com.example.coronadiagnosticapp.ui.fragments.recorder.RecorderFragment
import com.example.coronadiagnosticapp.ui.fragments.register.RegisterFragment
import com.example.coronadiagnosticapp.ui.fragments.resultFragment.ResultFragment
import com.example.coronadiagnosticapp.ui.fragments.splash.SplashFragment
import com.example.coronadiagnosticapp.ui.fragments.smoking.SmokingFragment
import com.example.coronadiagnosticapp.ui.fragments.onboarding.WelcomeFragment
import com.example.coronadiagnosticapp.ui.fragments.recorder.RecordingFragment
import com.example.coronadiagnosticapp.ui.fragments.recorder.RecordingsMainFragment
import com.example.coronadiagnosticapp.ui.fragments.terms.TermsAndConditionsFragment
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Component(modules = [DataRepositoryModule::class])
@Singleton
interface AppComponent {

    fun inject(fragment: RegisterFragment)
    fun inject(fragment: InformationFragment)
    fun inject(fragment: BackgroundDiseasesFragment)
    fun inject(fragment: DailyMetricFragment)
    fun inject(fragment: ResultFragment)
    fun inject(fragment: RecorderFragment)
    fun inject(fragment: CameraFragment)
    fun inject(fragment: WelcomeFragment)
    fun inject(fragment: OxymeterFragment)
    fun inject(fragment: QuestionnaireFragment)
    fun inject(fragment: QuestionFragment)
    fun inject(fragment: SmokingFragment)

//    fun <T> inject(injectable:T)
    fun inject(fragment: HomeFragment)
    fun inject(fragment: SplashFragment)
    fun inject(fragment: NotificationFragment)
    fun inject(fragment: RecordingsMainFragment)
    fun inject(fragment: RecordingFragment)
    fun inject(fragment: TermsAndConditionsFragment)
    fun inject(fragment: ConsentFormFragment)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }
}