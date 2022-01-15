package com.atlas.mars.glidecon.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.atlas.mars.glidecon.R
import com.atlas.mars.glidecon.store.MapBoxStore
import com.atlas.mars.glidecon.store.MapBoxStore.Companion.cameraPositionSubject
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.AsyncSubject

class FragmentTilt : Fragment() {
    private val TAG = "FragmentTilt"
    private val _onDestroy = AsyncSubject.create<Boolean>();
    var seekBar: SeekBar? = null
    var isMove = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tilt, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        seekBar = view?.findViewById(R.id.seekBar)
        view?.post(Runnable {
            view?.height
            view?.let {
                val lp = seekBar?.layoutParams
                lp?.width = it.height
                seekBar?.layoutParams = lp
                seekBarCreated()
            }
        })

    }

    private fun seekBarCreated() {
        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (isMove) {
                    MapBoxStore.tiltSubject.onNext(progress)
                }

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isMove = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isMove = false
            }
        })
        cameraPositionSubject
                .takeUntil(_onDestroy)
                .filter { !isMove }
                .subscribeBy(
                        onNext = {
                            val value = (100 * it.tilt / 60).toInt()
                            seekBar?.progress = value
                        }
                )
    }

    override fun onDestroy() {
       _onDestroy.onComplete()
        super.onDestroy()
    }
}