package com.duetduetku.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duetduetku.app.data.datastore.UserPreferences
import com.duetduetku.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val userName: String = "",
    val dailyLimit: Double = 0.0,
    val notificationsEnabled: Boolean = false,
    val appTheme: Int = 0, // 0: System, 1: Light, 2: Dark
    val profilePhotoUri: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    private val userPreferences: UserPreferences,
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileState())
    val uiState: StateFlow<ProfileState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val name = userPreferences.userName.first()
            val limit = userPreferences.dailyLimit.first()
            val notif = userPreferences.dailyReminder.first()
            val theme = userPreferences.appTheme.first()
            val photoUri = userPreferences.profilePhotoUri.first()

            _uiState.value = ProfileState(
                userName = name,
                dailyLimit = limit,
                notificationsEnabled = notif,
                appTheme = theme,
                profilePhotoUri = photoUri
            )
        }
    }

    fun updateName(newName: String) {
        viewModelScope.launch {
            userPreferences.setUserName(newName)
            _uiState.value = _uiState.value.copy(userName = newName)
        }
    }

    fun updateDailyLimit(newLimit: Double) {
        viewModelScope.launch {
            userPreferences.setDailyLimit(newLimit)
            _uiState.value = _uiState.value.copy(dailyLimit = newLimit)
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setDailyReminder(enabled)
            _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
        }
    }

    fun setAppTheme(theme: Int) {
        viewModelScope.launch {
            userPreferences.setAppTheme(theme)
            _uiState.value = _uiState.value.copy(appTheme = theme)
        }
    }
    
    fun updateProfilePhoto(uri: String) {
        viewModelScope.launch {
            val savedPath = saveImageToInternalStorage(uri)
            if (savedPath != null) {
                userPreferences.setProfilePhotoUri(savedPath)
                _uiState.value = _uiState.value.copy(profilePhotoUri = savedPath)
            }
        }
    }

    private fun saveImageToInternalStorage(uriString: String): String? {
        return try {
            val uri = android.net.Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputFile = java.io.File(context.filesDir, "profile_photo.jpg")
            val outputStream = java.io.FileOutputStream(outputFile)
            
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            outputFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }



    fun clearAllData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.deleteAllTransactions()
            // Optional: Reset preferences? Maybe not user name.
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}
