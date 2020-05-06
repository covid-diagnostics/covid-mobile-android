package com.example.coronadiagnosticapp.data.di

import android.content.Context
import com.example.coronadiagnosticapp.ui.activities.oxymeter.OxymeterActivity
import com.example.coronadiagnosticapp.ui.fragments.camera.CameraFragment
import com.example.coronadiagnosticapp.ui.fragments.dailtyMetric.DailyMetricFragment
import com.example.coronadiagnosticapp.ui.fragments.information.BackgroundDiseasesFragment
import com.example.coronadiagnosticapp.ui.fragments.information.InformationFragment
import com.example.coronadiagnosticapp.ui.fragments.instruction.InstructionsFragment
import com.example.coronadiagnosticapp.ui.fragments.questions.QuestionFragment
import com.example.coronadiagnosticapp.ui.fragments.questions.QuestionnaireFragment
import com.example.coronadiagnosticapp.ui.fragments.recorder.RecorderFragment
import com.example.coronadiagnosticapp.ui.fragments.register.RegisterFragment
import com.example.coronadiagnosticapp.ui.fragments.resultFragment.ResultFragment
import com.example.coronadiagnosticapp.ui.fragments.smoking.SmokingFragment
import com.example.coronadiagnosticapp.ui.fragments.welcome.WelcomeFragment
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
    fun inject(fragment: InstructionsFragment)
    fun inject(activity: OxymeterActivity)
    fun inject(fragment: QuestionnaireFragment)
    fun inject(fragment: QuestionFragment)
    fun inject(fragment: SmokingFragment)

//    fun <T> inject(injectable:T)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }
}