package com.yortch.confirmationsaints

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class — the Hilt entry point. Keep this file lean; per-module
 * wiring lives in [com.yortch.confirmationsaints.di.AppModule].
 */
@HiltAndroidApp
class ConfirmationSaintsApp : Application()
