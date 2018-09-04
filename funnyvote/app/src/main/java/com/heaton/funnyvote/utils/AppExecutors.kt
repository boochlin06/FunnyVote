/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.heaton.funnyvote.utils

import android.os.Handler
import android.os.Looper
import android.support.annotation.VisibleForTesting
import com.heaton.funnyvote.data.VoteData.LocalVoteDataSource
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Global executor pools for the whole application.
 *
 *
 * Grouping tasks like this avoids the effects of task starvation (e.g. disk reads don't wait behind
 * webservice requests).
 */
const val THREAD_COUNT = 3

/**
 * Global executor pools for the whole application.
 *
 * Grouping tasks like this avoids the effects of task starvation (e.g. disk reads don't wait behind
 * webservice requests).
 */
open class AppExecutors constructor(
        val diskIO: Executor = Executors.newSingleThreadExecutor(),
        val networkIO: Executor = Executors.newFixedThreadPool(THREAD_COUNT),
        val mainThread: Executor = MainThreadExecutor()
) {

    private class MainThreadExecutor : Executor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())

        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }

    companion object {
        private val TAG = AppExecutors::class.java.simpleName
        private var INSTANCE: AppExecutors? = null

        @JvmStatic
        fun getInstance(): AppExecutors? {
            if (INSTANCE == null) {
                synchronized(AppExecutors::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = AppExecutors()
                    }
                }
            }
            return INSTANCE!!
        }

        @JvmStatic
        @VisibleForTesting
        internal fun clearInstance() {
            INSTANCE = null
        }
    }
}