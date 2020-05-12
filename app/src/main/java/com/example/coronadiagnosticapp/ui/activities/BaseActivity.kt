package com.example.coronadiagnosticapp.ui.activities

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.example.coronadiagnosticapp.R


// A base activity all other activities in this app should inherit from.
abstract class BaseActivity : AppCompatActivity() {
    override fun onResume() {
        super.onResume()
        setupUI(findViewById(R.id.main_parent))
    }

    // Set up touch listener for non-text box views to hide keyboard.
    private fun setupUI(view: View) {
        if (view !is EditText) {
            view.setOnTouchListener { _, _ ->
                hideSoftKeyboard(this)
                false
            }
        }
        //If a layout container, iterate over children and seed recursion.
        (view as? ViewGroup)?.children?.forEach { setupUI(it) }
    }

    private fun hideSoftKeyboard(activity: Activity) {
        val inputMethodManager: InputMethodManager = activity.getSystemService(
            Activity.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
    }
}