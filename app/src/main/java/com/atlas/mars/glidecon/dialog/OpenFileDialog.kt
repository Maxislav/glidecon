package com.atlas.mars.glidecon.dialog

import android.R
import android.app.AlertDialog
import android.content.Context
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import java.io.File
import java.io.FilenameFilter
import java.util.*

class OpenFileDialog(context: Context) : AlertDialog.Builder(context) {
    private val LOG_TAG = "LOG_OpenFileDialog"
   //  private var currentPath = Environment.getExternalStorageDirectory().path
    private var currentPath = context.getExternalFilesDir(null)!!.absolutePath
    private val files: MutableList<File> = ArrayList()
    private val title: TextView
    private val listView: ListView
    private var filenameFilter: FilenameFilter? = null
    private var selectedIndex = -1
    private var listener: OpenDialogListener? = null
    private var folderIcon: Drawable? = null
    private var fileIcon: Drawable? = null
    private var accessDeniedMessage: String? = null
    private var isOnlyFoldersFilter = false

    interface OpenDialogListener {
        fun onSelectedFile(fileName: String?)
        fun onSelectPath(filePath: String?)
    }

    //abstract void onSelectedFile();
    private inner class FileAdapter(context: Context?, files: List<File?>?) : ArrayAdapter<File>(context!!, R.layout.simple_list_item_1, files!!) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent) as TextView
            val file = getItem(position)
            if (view != null) {
                view.text = file!!.name
                if (file.isDirectory) {
                    setDrawable(view, folderIcon)
                } else {
                    setDrawable(view, fileIcon)
                    if (selectedIndex == position) view.setBackgroundColor(context.resources.getColor(R.color.holo_blue_dark)) else view.setBackgroundColor(context.resources.getColor(R.color.transparent))
                }
            }
            return view
        }

        private fun setDrawable(view: TextView?, drawable: Drawable?) {
            if (view != null) {
                if (drawable != null) {
                    drawable.setBounds(0, 0, 60, 60)
                    view.setCompoundDrawables(drawable, null, null, null)
                } else {
                    view.setCompoundDrawables(null, null, null, null)
                }
            }
        }
    }

    override fun show(): AlertDialog {
        files.addAll(getFiles(currentPath))
        listView.adapter = FileAdapter(context, files)
        return super.show()
    }

    fun setCurrentPath(path: String?): OpenFileDialog {
        if (path != null) {
            currentPath = path
        }
        return this
    }

    /* fun setFilter(filter: String?): OpenFileDialog {
         filenameFilter = FilenameFilter { file, fileName ->
             val tempFile = File(String.format("%s/%s", file.path, fileName))
             if (tempFile.isFile) tempFile.name.matches(filter) else true
         }
         return this
     }*/

    fun setOnlyFoldersFilter(): OpenFileDialog {
        isOnlyFoldersFilter = true
        filenameFilter = FilenameFilter { file, fileName ->
            val tempFile = File(String.format("%s/%s", file.path, fileName))
            tempFile.isDirectory
        }
        return this
    }

    fun setOpenDialogListener(listener: OpenDialogListener?): OpenFileDialog {
        this.listener = listener
        return this
    }

    fun setFolderIcon(drawable: Drawable?): OpenFileDialog {
        folderIcon = drawable
        return this
    }

    fun setFileIcon(drawable: Drawable?): OpenFileDialog {
        fileIcon = drawable
        return this
    }

    fun setAccessDeniedMessage(message: String?): OpenFileDialog {
        accessDeniedMessage = message
        return this
    }

    private fun createMainLayout(context: Context): LinearLayout {
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.minimumHeight = getLinearLayoutMinHeight(context)
        return linearLayout
    }

    private fun getItemHeight(context: Context): Int {
        val value = TypedValue()
        val metrics = DisplayMetrics()
        context.theme.resolveAttribute(R.attr.listPreferredItemHeightSmall, value, true)
        getDefaultDisplay(context).getMetrics(metrics)
        return TypedValue.complexToDimension(value.data, metrics).toInt()
    }

    private fun createTextView(context: Context, style: Int): TextView {
        val textView = TextView(context)
        textView.setTextAppearance(context, style)
        val itemHeight = getItemHeight(context)
        textView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight)
        textView.minHeight = itemHeight
        textView.gravity = Gravity.CENTER_VERTICAL
        textView.setPadding(15, 0, 0, 0)
        return textView
    }

    private fun createTitle(context: Context): TextView {
        return createTextView(context, R.style.TextAppearance_DeviceDefault_DialogWindowTitle)
    }

    private fun createBackItem(context: Context): TextView {
        val textView = createTextView(context, R.style.TextAppearance_DeviceDefault_Small)
        val drawable = getContext().resources.getDrawable(R.drawable.ic_menu_directions)
        drawable.setBounds(0, 0, 60, 60)
        textView.setCompoundDrawables(drawable, null, null, null)
        textView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        textView.setOnClickListener {
            val file = File(currentPath)
            val parentDirectory = file.parentFile
            if (parentDirectory != null) {
                currentPath = parentDirectory.path
                //
                _rebuildFiles(listView.adapter as FileAdapter)
            }
        }
        return textView
    }

    private fun getTextWidth(text: String, paint: Paint): Int {
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        return bounds.left + bounds.width() + 80
    }

    private fun changeTitle() {
        var titleText = currentPath
        val screenWidth = getScreenSize(context).x
        val maxWidth = (screenWidth * 0.99).toInt()
        if (getTextWidth(titleText, title.paint) > maxWidth) {
            while (getTextWidth("...$titleText", title.paint) > maxWidth) {
                val start = titleText.indexOf("/", 2)
                titleText = if (start > 0) titleText.substring(start) else titleText.substring(2)
            }
            title.text = "...$titleText"
        } else {
            title.text = titleText
        }
    }

    private fun getFiles(directoryPath: String): List<File> {
        val directory = File(directoryPath)
        var list = directory.listFiles(filenameFilter)
        if (list == null) list = arrayOf()
        val fileList = Arrays.asList(*list)
        Collections.sort(fileList, object : Comparator<File?> {

            fun compareExist(file: File, file2: File): Int {
                return if (file.isDirectory && file2.isFile) {
                    -1
                } else if (file.isFile && file2.isDirectory) {
                    1
                } else {
                    file.path.compareTo(file2.path)
                }
            }

            override fun compare(file: File?, file2: File?): Int {
                return if (file !== null && file2 !== null) {
                    compareExist(file, file2)
                } else {
                    0
                }
            }
        })
        return fileList
    }

    private fun _rebuildFiles(adapter: ArrayAdapter<File>) {
        try {
            val fileList = getFiles(currentPath)
            files.clear()
            selectedIndex = -1
            files.addAll(fileList)
            adapter.notifyDataSetChanged()
            changeTitle()
        } catch (e: NullPointerException) {
            var message: String? = context.resources.getString(R.string.unknownName)
            if (accessDeniedMessage != "") message = accessDeniedMessage
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun createListView(context: Context): ListView {
        val listView = ListView(context)
        listView.onItemClickListener = OnItemClickListener { adapterView, view, index, l ->
            val adapter: ArrayAdapter<File> = adapterView.adapter as FileAdapter
            val file = adapter.getItem(index)
            /**
             * преход в папку
             */
            /**
             * преход в папку
             */
            /**
             * преход в папку
             */
            /**
             * преход в папку
             */
            if (file!!.isDirectory) {
                currentPath = file.path
                Log.d(LOG_TAG, "currentPath ->$currentPath")
                if (listener != null) {
                    listener!!.onSelectPath(currentPath)
                }

                /*Log.d("Files", "Path: " + currentPath);
                    File directory = new File(currentPath);
                    File[] files = directory.listFiles();
                    Log.d("Files", "Size: "+ files.length);
                    for (int i = 0; i < files.length; i++)
                    {
                        Log.d("LOG_Files", "FileName:" + files[i].getName());
                    }*/_rebuildFiles(adapter)
            } else {
                /**
                 * Выбор файла
                 */
                /**
                 * Выбор файла
                 */
                /**
                 * Выбор файла
                 */
                /**
                 * Выбор файла
                 */
                selectedIndex = if (index != selectedIndex) {
                    index
                } else {
                    -1
                }
                Log.d(LOG_TAG, "selectedIndex ->$selectedIndex")
                adapter.notifyDataSetChanged()
            }
        }
        return listView
    }

    companion object {
        private fun getDefaultDisplay(context: Context): Display {
            return (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        }

        private fun getScreenSize(context: Context): Point {
            val screeSize = Point()
            getDefaultDisplay(context).getSize(screeSize)
            return screeSize
        }

        private fun getLinearLayoutMinHeight(context: Context): Int {
            return getScreenSize(context).y
        }
    }

    init {
        title = createTitle(context)
        changeTitle()
        val linearLayout = createMainLayout(context)
        linearLayout.addView(createBackItem(context))
        listView = createListView(context)
        linearLayout.addView(listView)
        setCustomTitle(title)
                .setView(linearLayout)
                .setPositiveButton(R.string.ok) { dialog, which ->
                    if (selectedIndex > -1 && listener != null) {
                        listener!!.onSelectedFile(listView.getItemAtPosition(selectedIndex).toString())
                        /* File directory = new File(currentPath);
                            File[] files = directory.listFiles();
                            files[selectedIndex].getName();*/
                        //((OpenDialogListener)context).onSelectedFile(files[selectedIndex].getName());
                    }
                    if (listener != null && isOnlyFoldersFilter) {
                        listener!!.onSelectedFile(currentPath)
                    }
                }
                .setNegativeButton(R.string.cancel, null)
    }
}