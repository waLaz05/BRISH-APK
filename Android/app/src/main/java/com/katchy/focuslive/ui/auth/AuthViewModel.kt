package com.katchy.focuslive.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.katchy.focuslive.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _user = MutableStateFlow<FirebaseUser?>(authRepository.currentUser)
    val user: StateFlow<FirebaseUser?> = _user.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.authStateFlow.collect { firebaseUser ->
                _user.value = firebaseUser
            }
        }
    }

    fun signIn(credential: AuthCredential) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            val result = authRepository.signInWithCredential(credential)
            result.onSuccess { firebaseUser ->
                // _user.value is updated by the flow
                saveUserToFirestore(firebaseUser)
            }.onFailure { exception ->
                _loading.value = false
                _error.value = exception.message
            }
        }
    }
    
    fun signInAnonymously() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            val result = authRepository.signInAnonymously()
            result.onSuccess { firebaseUser ->
                 // _user.value updated by flow
                 saveUserToFirestore(firebaseUser)
            }.onFailure { exception ->
                _loading.value = false
                _error.value = "Guest login failed: ${exception.message}"
            }
        }
    }

    private fun saveUserToFirestore(user: FirebaseUser) {
        viewModelScope.launch {
            val result = authRepository.saveUserToFirestore(user)
            result.onFailure { exception ->
                _error.value = "Error saving user data: ${exception.message}"
            }
            _loading.value = false
        }
    }
}
