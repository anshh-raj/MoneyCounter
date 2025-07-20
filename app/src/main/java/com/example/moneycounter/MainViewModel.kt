package com.example.moneycounter

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    init {
        viewModelScope.launch {
            while (true){
                delay(500)
                Log.d("viewmodel", ": hello duniya")
            }
        }

    }



    // this onCleared fun is called when the viewmodel is destroyed
    override fun onCleared() {
        super.onCleared()
        Log.d("viewmodel", "onCleared: ViewModel destroyed")
    }
}