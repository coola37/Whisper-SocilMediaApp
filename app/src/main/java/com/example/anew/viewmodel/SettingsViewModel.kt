package com.example.anew.viewmodel

import android.app.Application
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class SettingsViewModel(
    application: Application
) : BaseViewModel(application) {

}