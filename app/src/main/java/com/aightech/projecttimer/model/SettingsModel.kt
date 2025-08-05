package com.aightech.projecttimer.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
//import com.aightech.projecttimer.BuildConfig   // ← add this

class SettingsModel : ViewModel() {
    private val _dark = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> get() = _dark

    // now you can reference VERSION_NAME
    val version: String = "0.1"

    fun toggleTheme(on: Boolean) {
        _dark.value = on
    }
}
