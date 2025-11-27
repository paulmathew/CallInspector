package com.example.callinspector.utils

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.callinspector.CallInspectorApp

fun loge(tag:String="",msg:String){
    Log.e(tag, msg)
}

