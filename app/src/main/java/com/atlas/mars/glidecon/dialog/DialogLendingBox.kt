package com.atlas.mars.glidecon.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import com.atlas.mars.glidecon.Ololo
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.databinding.DialogLandingBoxBinding
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.concurrent.TimeUnit
import kotlin.coroutines.suspendCoroutine


class DialogLendingBox(val activity: Activity, val ololo: Ololo) : AlertDialog.Builder(activity) {
    lateinit var alertDialog: AlertDialog
    val imageView: ImageView

    private val touchStartSubject: BehaviorSubject<Pair<Float, Float>> = BehaviorSubject.create()
    private val touchMoveSubject: BehaviorSubject<Pair<Float, Float>> = BehaviorSubject.create()
    private val myThrottle: BehaviorSubject<Pair<Pair<Float, Float>, Pair<Float, Float>>> = BehaviorSubject.create()
    var i = 0
    var job: Job? = null

    init {
        val binding: DialogLandingBoxBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_landing_box, null, false)
        setView(binding.root);
        imageView = binding.imageView
        setPositiveButton("Save") { dialog: DialogInterface, which: Int ->
            Log.d(TAG, "")
        }
        setNegativeButton("Cancel", null)
    }

    override fun create(): AlertDialog {
        alertDialog = super.create()
        imageView.layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        imageView.layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        imageView.post(Runnable {
            imageView.layoutParams.height = imageView.width
            imageView.layoutParams.width = imageView.width
            initTouchListener(imageView)
        })
        return alertDialog
    }

    @DelicateCoroutinesApi
    private fun start(n: Pair<Pair<Float, Float>, Pair<Float, Float>>) = GlobalScope.launch {
        job?.cancelAndJoin()
        job = launch {
            coroutine(n)
            job = null
        }
    }

    private suspend fun coroutine(n: Pair<Pair<Float, Float>, Pair<Float, Float>>): Unit {
        delay(40)
        myThrottle.onNext(n)
    }

    @DelicateCoroutinesApi
    @SuppressLint("ClickableViewAccessibility")
    private fun initTouchListener(imageView: ImageView) {
        val width = imageView.layoutParams.width
        val height = imageView.layoutParams.height

        // imageSizeSubject.onNext(Math.min(width, height))
        fun a(): Boolean {
            return true
        }
        myThrottle.subscribeBy {
            Log.d(TAG, "coroutine $it")
        }
        Observables.combineLatest(touchStartSubject, touchMoveSubject)
                .filter{ job == null }
                .subscribeBy { pair: Pair<Pair<Float, Float>, Pair<Float, Float>> ->
                    start(pair)
                }
        imageView.setOnTouchListener { v: View?, event: MotionEvent? ->
            val X: Float? = event?.x
            val Y: Float? = event?.y
            when (event!!.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    Log.d(TAG, "action down $X $Y")
                    if (X != null && Y != null) {
                        touchStartSubject.onNext(Pair(X, Y))
                    }

                }

                MotionEvent.ACTION_MOVE -> {
                    if (X != null && Y != null) {
                        // Log.d(TAG, "action move $X $Y")
                        touchMoveSubject.onNext(Pair(X, Y))
                        //  touchPositionSubject.onNext(Pair(X, Y))
                    }
                }
            }
            true
        }
    }

    suspend fun tttt() {
         delay(10000)
        return suspendCoroutine {
            Log.d(TAG, "ollol")
            // it.resume("")
        }
    }


    @DelicateCoroutinesApi
    private fun createSubscriber(): BehaviorSubject<Boolean> {
        val source = BehaviorSubject.create<Boolean>()

        val s = suspend {
            delay(1000)
            source.onNext(true)
            source.onComplete()
            Log.d(TAG, "word") //
            source

        }

        GlobalScope.launch {
            s()
        }

        /*runBlocking { // this: CoroutineScope
            launch { // launch a new coroutine and continue
                s() // non-blocking delay for 1 second (default time unit is ms)
            }
            Log.d(TAG, "word")// main coroutine continues while a previous one is delayed
        }*/
        return source
    }

    private suspend fun coroutine2(): Unit {
        delay(500)
        return suspendCoroutine {
            Log.d(TAG, "ollol")
            // it.resume("")
        }
    }

    companion object {
        const val TAG = "DialogLendingBox"
    }

}
