package com.astronomyshop.app.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.astronomyshop.app.R
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

class CrashTestFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_crash_test, container, false)

        view.findViewById<Button>(R.id.btnJavaCrash).setOnClickListener {
            triggerRuntimeCrash()
        }

        view.findViewById<Button>(R.id.btnNullPointer).setOnClickListener {
            triggerNullPointerCrash()
        }

        view.findViewById<Button>(R.id.btnIndexCrash).setOnClickListener {
            triggerIndexOutOfBoundsCrash()
        }

        view.findViewById<Button>(R.id.btnStackOverflow).setOnClickListener {
            triggerIllegalStateCrash()
        }

        view.findViewById<Button>(R.id.btnBackgroundCrash).setOnClickListener {
            triggerBackgroundThreadCrash()
        }

        view.findViewById<Button>(R.id.btnMainThreadAnr).setOnClickListener {
            triggerMainThreadAnr()
        }

        view.findViewById<Button>(R.id.btnLockAnr).setOnClickListener {
            triggerLockContentionAnr()
        }

        return view
    }

    private fun triggerRuntimeCrash() {
        throw RuntimeException("Test crash: manual RuntimeException from CrashTestFragment")
    }

    private fun triggerNullPointerCrash() {
        val value: String? = null
        val length = value!!.length
        println(length)
    }

    private fun triggerIndexOutOfBoundsCrash() {
        val items = listOf("Mercury", "Venus")
        val badItem = items[10]
        println(badItem)
    }

    private fun triggerIllegalStateCrash() {
        throw IllegalStateException("Test crash: illegal state")
    }

    private fun recurseForever() {
        recurseForever()
    }

    private fun triggerBackgroundThreadCrash() {
        thread(name = "CrashTestThread") {
            throw RuntimeException("Test crash: background thread crash")
        }
    }

    private fun triggerMainThreadAnr() {
        // Freeze UI thread for 20 seconds
        Thread.sleep(20_000)
    }

    private fun triggerLockContentionAnr() {
        val latch = CountDownLatch(1)

        thread(name = "LockHolderThread") {
            Thread.sleep(30_000)
            latch.countDown()
        }

        // Blocks main thread waiting for background thread
        latch.await()
    }
}