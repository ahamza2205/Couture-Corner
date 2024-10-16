package com.example.couturecorner.setting.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.couturecorner.data.repository.Repo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrencyViewModel @Inject constructor(
    private val currencyRepo: Repo,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _selectedCurrency = MutableLiveData<String>()
    val selectedCurrency: LiveData<String> = _selectedCurrency

    private val _convertedCurrency = MutableLiveData<Double?>()
    val convertedCurrency: LiveData<Double?> = _convertedCurrency
    init {
        _selectedCurrency.value = sharedPreferences.getString("selected_currency", "EGP")
    }
    fun saveSelectedCurrency(currency: String) {
        sharedPreferences.edit().putString("selected_currency", currency).apply()
        _selectedCurrency.value = currency
    }

    fun convertCurrency(from: String, to: String, amount: Double, callback: (Double?) -> Unit) {
        viewModelScope.launch {
            try {
                val conversionResult = currencyRepo.convertCurrency(from, to, amount, "f48a1a9677-68abaacec8-slhgsh")
                val result = conversionResult?.result?.get(to)
                callback(result)
            } catch (e: Exception) {
                callback(null)
            }
        }
    }
    fun getSelectedCurrency(): String? {
        return sharedPreferences.getString("selected_currency", "EGP")
    }
}


