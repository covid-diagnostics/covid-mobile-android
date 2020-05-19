package com.example.coronadiagnosticapp.ui.fragments.recorder

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController
import be.tarsos.dsp.io.PipedAudioStream
import be.tarsos.dsp.io.android.AndroidFFMPEGLocator
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.ui.audioAnalyzer.AudioAnalyzerImpl
import com.example.coronadiagnosticapp.ui.fragments.ScopedFragment
import com.example.coronadiagnosticapp.utils.getAppComponent
import com.example.coronadiagnosticapp.utils.toast
import kotlinx.android.synthetic.main.fragment_recordings_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class RecordingsMainFragment : ScopedFragment(), RecorderFragment.Callback{

    private val TAG = javaClass.name

    @Inject
    lateinit var viewModel: RecorderViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_recordings_main, container, false)

    override fun onAttach(context: Context) {
        context.getAppComponent().inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBarHorizontal.max = viewModel.recordingCount
        progressBarHorizontal.progress = 1
        fill(viewModel.getCurrentRecording()!!)
    }

    private fun fill(recording: Recording) {
        instruction_tv.setText(recording.title)
        example_tv.setText(recording.example)
        recording.explanation?.let {
            extra_info_tv.setText(it)
        } ?: extra_info_tv.setText("")

        val recorderFragment = childFragmentManager.findFragmentByTag("Record") as? RecorderFragment
        recorderFragment?.resetUI()
            ?: replaceToRecordFragment()
    }

    private fun replaceToRecordFragment() {
        replaceInnerFragment(RecorderFragment(this), "Record")
    }

    private fun replaceInnerFragment(fragment: Fragment, tag: String) {
        childFragmentManager.beginTransaction()
            .replace(R.id.record_fragment_container, fragment, tag)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit()
    }

    private fun updateNextRecording() {
        //  Continue to next question recording
        val nextRecording = viewModel.getNextRecording()
        if (nextRecording == null) {
            //         if there's no questions left move to next fragment
            findNavController()
                .navigate(R.id.action_recorderFragment_to_resultFragment)
            return
        }

        //        fill the data from next question
        progressBarHorizontal.progress = viewModel.getCurrentRecordingIndex() + 1
        //        fill the data from next question
        fill(nextRecording)
    }

    override fun onRecordingFinished(fileLocation: String) {
        val callback = object : RecordingFragment.Callback {
            override fun onContinueTapped() = uploadFile(fileLocation)
            override fun onRecordTapped() = replaceToRecordFragment()
        }
        val fragment = RecordingFragment(fileLocation, callback)
        replaceInnerFragment(fragment, "Recording")
    }


    private fun uploadFile(fileLocation: String) {
        // Upload file
        toast("Uploading...")
        launch(Dispatchers.IO) {
            viewModel.uploadFile(File(fileLocation))
            withContext(Dispatchers.Main) {
                toast("Uploaded!")
                updateNextRecording()
            }
            Log.d(TAG, "File finished uploading!")
        }
    }

}
