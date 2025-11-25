package com.example.mad_assignment.ui.User

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mad_assignment.data.Database.AppDatabase
import com.example.mad_assignment.data.Database.UserProfile
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

data class ProfileUiState(
    val email: String? = null,
    val displayName: String? = null,
    val address: String? = null, // Address field is included here
    val profileBitmap: Bitmap? = null,
    val isLoading: Boolean = true,
    val isLoggedOut: Boolean = false
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userDao = AppDatabase.getDatabase(application).userDao()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            viewModelScope.launch {
                userDao.getUser(firebaseUser.uid).collect { userProfile ->
                    if (userProfile != null) {
                        _uiState.update {
                            it.copy(
                                email = userProfile.email,
                                displayName = userProfile.displayName,
                                address = userProfile.address,
                                profileBitmap = userProfile.profilePicture?.toBitmap(),
                                isLoading = false
                            )
                        }
                    } else {
                        val newUser = UserProfile(
                            uid = firebaseUser.uid,
                            email = firebaseUser.email,
                            displayName = firebaseUser.displayName,
                            address = null,
                            profilePicture = null
                        )
                        userDao.upsertUser(newUser)
                    }
                }
            }
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    // This is the correct function for saving user details
    fun updateProfileDetails(newName: String, newAddress: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            val currentProfile = userDao.getUser(user.uid).firstOrNull() ?: return@launch
            val updatedProfile = currentProfile.copy(
                displayName = newName,
                address = newAddress
            )
            userDao.upsertUser(updatedProfile)
        }
    }

    fun uploadProfileImage(uri: Uri) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val imageData = getApplication<Application>().contentResolver.uriToByteArray(uri)
            val currentProfile = userDao.getUser(user.uid).firstOrNull() ?: return@launch
            val updatedProfile = currentProfile.copy(profilePicture = imageData)
            userDao.upsertUser(updatedProfile)
        }
    }

    fun logout() {
        auth.signOut()
        _uiState.update { it.copy(isLoggedOut = true) }
    }

    private fun ByteArray.toBitmap(): Bitmap {
        return BitmapFactory.decodeByteArray(this, 0, this.size)
    }

    private fun android.content.ContentResolver.uriToByteArray(uri: Uri): ByteArray {
        val stream = ByteArrayOutputStream()
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(this, uri))
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(this, uri)
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        return stream.toByteArray()
    }
}