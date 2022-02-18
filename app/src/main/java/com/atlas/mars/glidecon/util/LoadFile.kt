package com.atlas.mars.glidecon.util

import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException

class LoadFile( var path: String) {
    // Log.e(MapsActivity.TAG, e.toString());
// TODO: handle exception
    //Log.e(MapsActivity.TAG, e.toString());
    // Log.d(MapsActivity.TAG,textBuilder.toString());
    val text: String?
        get() {
            var txt: String? = null
            var reader: BufferedReader? = null
            try {
                reader = BufferedReader(FileReader(path))
                val textBuilder = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    textBuilder.append(line)
                }
                reader.close()
                txt = textBuilder.toString()

                // Log.d(MapsActivity.TAG,textBuilder.toString());
            } catch (e: FileNotFoundException) {
                // TODO: handle exception
                e.printStackTrace()
                //Log.e(MapsActivity.TAG, e.toString());
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
                // Log.e(MapsActivity.TAG, e.toString());
            } finally {
                if (reader != null) {
                    try {
                        reader.close()
                    } catch (e: IOException) {
                        // TODO Auto-generated catch block
                        e.printStackTrace()
                    }
                }
            }
            return txt
        }

}