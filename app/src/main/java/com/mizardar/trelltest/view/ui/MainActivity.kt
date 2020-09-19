package com.mizardar.trelltest.view.ui

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider.getUriForFile
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.mizardar.trelltest.viewmodel.MainActivityViewModel
import com.mizardar.trelltest.model.ModelVideo
import com.mizardar.trelltest.R
import com.mizardar.trelltest.database.DatabaseHelper
import com.mizardar.trelltest.databinding.ActivityMainBinding
import com.mizardar.trelltest.view.adapter.VideoListAdapter
import com.mizardar.trelltest.view.listeners.VideoInteractionListener
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var mainActivityViewModel: MainActivityViewModel
    private val videoList = mutableListOf<ModelVideo>()
    private lateinit var videoRecyclerView: RecyclerView
    private lateinit var videoListAdapter: VideoListAdapter
    private lateinit var snapHelper: PagerSnapHelper

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var contentResolverRef : ContentResolver
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var bookmarkedVideos : List<String>

    private var currentPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = DataBindingUtil.setContentView(this,
            R.layout.activity_main
        )

        init()
    }

    private fun init() {
        initDatabase()
        initAllBookmarkedVideos();
        initContentResolver()
        initBinding()
        initSharedPref()
        initListAdapter()
        initViewModel()
        checkStoragePermission();
    }

    private fun initDatabase() {
        databaseHelper = DatabaseHelper(this)
    }


    private fun initAllBookmarkedVideos() {
        bookmarkedVideos = getBookmarkedVideos()
    }
    private fun initContentResolver() {
        contentResolverRef = contentResolver
    }

    private fun initBinding() {
        setLoading(true)
    }

    private fun initSharedPref(){
        sharedPreferences = getSharedPreferences("trelltest_pref", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }

    private fun setLoading(isLoading: Boolean) {
        activityMainBinding.isLoading = isLoading
        activityMainBinding.executePendingBindings()
    }

    private fun initListAdapter() {
        videoRecyclerView = activityMainBinding.videoRecyclerView
        videoListAdapter = VideoListAdapter(
            this,
            videoList,
            videoInteractionListener
        )
        videoRecyclerView.adapter = videoListAdapter

        snapHelper = PagerSnapHelper() // Or PagerSnapHelper
        snapHelper.attachToRecyclerView(videoRecyclerView)
    }


    private val videoInteractionListener = object :
        VideoInteractionListener {
        override fun onShareClick(modelVideo: ModelVideo) {
            try {
                val sharedFile = File(modelVideo.videoPath)
                val contentUri: Uri =
                    getUriForFile(
                        this@MainActivity,
                        "com.mizardar.trelltest.fileprovider",
                        sharedFile
                    )
                val shareIntent = ShareCompat.IntentBuilder.from(this@MainActivity)
                    .setStream(contentUri)
                    .intent
                shareIntent.setDataAndType( contentUri,"video/*")
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(shareIntent)
            } catch (e: Exception) {
                Log.e("ex", "exception ocurred")
            }

        }

        override fun onBookmarkClick(modelVideo: ModelVideo) {
            changeBookmark(modelVideo)
        }

        override fun onPositionChanged(modelVideo: ModelVideo) {
            currentPosition = videoList.indexOf(modelVideo);
            editor.apply {
                putInt("lastPosition", currentPosition)
                commit()
            }
        }

        override fun onVideoCompleted() {
            if (currentPosition < videoList.size - 1)
                videoRecyclerView.smoothScrollToPosition(currentPosition + 1)
        }
    }

    private fun changeBookmark(modelVideo: ModelVideo) {
        changeDatabaseValue(modelVideo)
        val position = videoList.indexOf(modelVideo)
        val isBookMarked = modelVideo.isBookMarked
        modelVideo.isBookMarked = !isBookMarked
        videoList.removeAt(position)
        videoList.add(position, modelVideo)
        videoListAdapter.notifyItemChanged(position)

    }

    private fun changeDatabaseValue(modelVideo: ModelVideo) {
        if (modelVideo.isBookMarked){
            databaseHelper.deleteBookmarkedVideo(modelVideo)
        }else{
            databaseHelper.addBookmarkedVideo(modelVideo)
        }
    }

    private fun getBookmarkedVideos() : List<String> {
        return databaseHelper.getAllBookmarkedVideos()
    }

    private fun initViewModel() {
        mainActivityViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        mainActivityViewModel.getFilePathList()
            .observe(this, Observer<List<ModelVideo>> { videoModelList ->
                Log.e("TSG", "size : ${videoModelList.size}")
                videoList.clear()
                videoList.addAll(videoModelList)
                videoListAdapter.notifyDataSetChanged()
                setLoading(false)
                goToDefaultVideo()
            })
    }

    private fun goToDefaultVideo() {
        val savedPosition = getSavedPosition()
        if (savedPosition <= videoList.size -1){
            videoRecyclerView.scrollToPosition(savedPosition)
        }
    }

    private fun getSavedPosition(): Int {
        return sharedPreferences.getInt("lastPosition",0)
    }

    private fun checkStoragePermission() {

        if (ContextCompat.checkSelfPermission(
                baseContext,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mainActivityViewModel.getVideos(contentResolverRef, bookmarkedVideos)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), 100
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED)
            && (grantResults.isNotEmpty() &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED)
        ) {
            mainActivityViewModel.getVideos(contentResolverRef, bookmarkedVideos)
        } else {
            finish()
        }
    }
}