package com.example.couturecorner.setting.viewmodel
import androidx.lifecycle.ViewModel
import com.example.couturecorner.data.repository.Repo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel

class CheckOutViewModel@Inject constructor(
    private val repo: Repo
):ViewModel() {

    fun getAddressState(): Boolean {
        return repo.getAddressState()
    }



}