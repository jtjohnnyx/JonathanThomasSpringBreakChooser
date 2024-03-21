package com.example.jonathanthomasspringbreakchooser

import androidx.annotation.StringRes

data class Location(@StringRes val textResId: Int, val lat: Double, val lon: Double, val lang: String)