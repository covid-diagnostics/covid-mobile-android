package com.example.coronadiagnosticapp.ui.fragments.splash

import android.animation.Animator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.utils.getAppComponent
import kotlinx.android.synthetic.main.fragment_splash.*
import javax.inject.Inject

class SplashFragment : Fragment(), Animator.AnimatorListener {

    @Inject
    lateinit var viewModel: SplashViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_splash, container, false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.getAppComponent()?.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        animateLogo()
    }

    private fun animateLogo() = with(logo_img) {
        alpha = 0.5f
        scaleX = 0f
        scaleY = 0f
        animate()
            .setDuration(500)
            .setListener(this@SplashFragment)
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .start()
    }

    private fun moveToNextScreen() {
        val isLoggedIn = viewModel.isLoggedIn()
        viewModel.setIsFirstTime(!isLoggedIn)
        val id = if (isLoggedIn) {
            R.id.action_splashFragment_to_homeFragment
        } else {
            R.id.action_splashFragment_to_onBoardingMainFragment
        }
        findNavController().navigate(id)
    }

    override fun onAnimationEnd(animation: Animator?) {
        moveToNextScreen()
    }

    override fun onAnimationRepeat(animation: Animator?) {}

    override fun onAnimationCancel(animation: Animator?) {}

    override fun onAnimationStart(animation: Animator?) {}
}
