package com.atlas.mars.glidecon

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import com.atlas.mars.glidecon.databinding.ActivitySettingMapBoxBinding
import io.reactivex.subjects.AsyncSubject


class SettingMapBoxActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingMapBoxBinding
    private val _onDestroy = AsyncSubject.create<Boolean>()

    // var radius: Int = 500

    var radius = ObservableField<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingMapBoxBinding.inflate(layoutInflater)
        binding.settingMapBoxActivity = this

        radius.set("500")
        setContentView(binding.root)
        // val myParams: ObservableField<MyParams> = ObservableField<MyParams>()
    }
    class MyParams{

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setCustomView(R.layout.action_bar_list_saved_track)
        val backView = supportActionBar?.customView;
        backView?.setOnClickListener { v: View ->
            Log.d(ListSavedTrack.TAG, "Title clicked")
            finish()
        }
        return true
    }

    override fun onDestroy() {
        _onDestroy.onComplete()
        super.onDestroy()
    }
}