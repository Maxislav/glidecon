package com.atlas.mars.glidecon.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.atlas.mars.glidecon.Ololo
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.databinding.DialogLandingBoxBinding
import com.atlas.mars.glidecon.model.LandingBoxDrawer
import com.atlas.mars.glidecon.model.LandingBoxViewModel
import com.atlas.mars.glidecon.store.MapBoxStore
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.windSubject
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.*
import java.lang.Math.toDegrees
import java.lang.Runnable
import kotlin.math.atan


class DialogLendingBox(val activity: AppCompatActivity, val ololo: Ololo) : AlertDialog.Builder(activity) {
    lateinit var alertDialog: AlertDialog
    val imageView: ImageView

    private val touchStartSubject: BehaviorSubject<Pair<Float, Float>> = BehaviorSubject.create()
    private val touchMoveSubject: BehaviorSubject<Pair<Float, Float>> = BehaviorSubject.create()
    private val myThrottle: BehaviorSubject<Pair<Pair<Float, Float>, Pair<Float, Float>>> = BehaviorSubject.create()
    private val imageSizeSubject = BehaviorSubject.create<Int>()
    private lateinit var landingBoxDrawer: LandingBoxDrawer
    private var myViewModel: LandingBoxViewModel
    var i = 0
    var job: Job? = null
    var isSubscribe = true;

    var center: Pair<Float, Float> = Pair(0.0f, 0.0f)

    private var initialStartA: Double? = 0.0

    init {
        val binding: DialogLandingBoxBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_landing_box, null, false)
        setView(binding.root);

        myViewModel = ViewModelProviders.of(activity, MyViewModelFactory(0.0)).get(LandingBoxViewModel::class.java)
        imageView = binding.imageView

        binding.landingBoxViewModel = myViewModel
        myViewModel.ratioFly.observe(activity, {
            Log.d(TAG, "$it")
        })
        binding.lifecycleOwner = activity

        setPositiveButton("Save") { dialog: DialogInterface, which: Int ->
            onDestroy()
        }
        setNegativeButton("Cancel", null)
        myThrottle.subscribeBy {
            //   Log.d(TAG, "coroutine ${it.second.first}")
            val startXY = it.first
            val dx = startXY.first - imageSizeSubject.value / 2
            val dy = imageSizeSubject.value / 2 - startXY.second
            val aStart = getStartAngle(dx, dy)

            val moveXY = it.second
            val dx2 = moveXY.first - imageSizeSubject.value / 2
            val dy2 = imageSizeSubject.value / 2 - moveXY.second
            val aMove = getStartAngle(dx2, dy2)

            Log.d(TAG, "start-move ${aMove - aStart}")
            if (initialStartA != null) {
                myViewModel.setAngle(initialStartA!! + aMove - aStart)
            }

        }
        imageSizeSubject
                .take(1)
                .doOnNext { size ->
                    center = Pair(size.toFloat() / 2, size.toFloat() / 2)
                    landingBoxDrawer = LandingBoxDrawer(context, size)
                    imageView.setImageBitmap(landingBoxDrawer.bitmap)
                }
                .concatMap {
                    windSubject
                }
                .subscribeBy {

                }

    }

    private fun getStartAngle(dx: Float, dy: Float): Double {
        val a = toDegrees(atan(dx / dy).toDouble())
        return if (0 < dy && 0 < dx) {
            a
        } else if (0 < dx && dy <= 0) {
            a + 180
        } else if (dx <= 0 && dy <= 0) {
            a + 180
        } else {
            a + 360
        }
    }

    @DelicateCoroutinesApi
    override fun create(): AlertDialog {
        alertDialog = super.create()
        imageView.layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        imageView.layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        imageView.post(Runnable {
            imageView.layoutParams.height = imageView.width
            imageView.layoutParams.width = imageView.width
            initTouchListener(imageView)
        })
        alertDialog.window?.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL)

        alertDialog.setOnDismissListener {
            onDestroy()
        }
        myViewModel.setAngle(MapBoxStore.landingBoxAngleSubject.value)
        /* MapBoxStore.landingBoxAngleSubject.
                 .take(1)
                 .subscribeBy {

                 }*/
        return alertDialog
    }

    @DelicateCoroutinesApi
    private fun start(n: Pair<Pair<Float, Float>, Pair<Float, Float>>) {
        //job?.cancelAndJoin()
        job = GlobalScope.launch {
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


        imageSizeSubject.onNext(Math.min(width, height))
        fun a(): Boolean {
            return true
        }
        /// touchStartSubject
        touchMoveSubject
                .withLatestFrom(touchStartSubject)
                .filter { job == null }
                .subscribeBy {
                    start(Pair(it.second, it.first))
                }


        /* Observables.combineLatest(touchStartSubject, touchMoveSubject)
                 .filter { job == null }
                 .subscribeBy { pair: Pair<Pair<Float, Float>, Pair<Float, Float>> ->
                  //   start(pair)
                 }*/
        imageView.setOnTouchListener { v: View?, event: MotionEvent? ->
            val X: Float? = event?.x
            val Y: Float? = event?.y
            when (event!!.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    Log.d(TAG, "action down $X $Y")
                    if (X != null && Y != null) {
                        initialStartA = myViewModel.angle.value
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

    private fun onDestroy() {
        isSubscribe = false
    }

    companion object {
        const val TAG = "DialogLendingBox"
    }

    class MyViewModelFactory(private val mParam: Double) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return LandingBoxViewModel(mParam) as T
        }
    }

}
