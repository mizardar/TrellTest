package com.mizardar.trelltest.viewmodel

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mizardar.trelltest.model.ModelVideo

class MainActivityViewModel : ViewModel() {
    private val filePathList = MutableLiveData<List<ModelVideo>>()

    fun getFilePathList(): LiveData<List<ModelVideo>> {
        return filePathList
    }



    fun getVideos(
        contentResolverRef: ContentResolver,
        bookmarkedVideos: List<String>
    ) {
        val videoList = mutableListOf<ModelVideo>()
        val cursor : Cursor?;
        val indexData : Int?

        var absolutePath : String? = null;
        val uri : Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        val projection = arrayOf(MediaStore.MediaColumns.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME,MediaStore.Video.Media._ID,MediaStore.Video.Thumbnails.DATA);

        val orderBy = MediaStore.Images.Media.DATE_TAKEN;
        cursor = contentResolverRef.query(uri, projection, null, null,
            "$orderBy DESC"
        );

        indexData = cursor?.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

        while (cursor?.moveToNext()!!) {
            absolutePath = cursor.getString(indexData!!);

            val isBookmarked = bookmarkedVideos.contains(absolutePath)

            val video = ModelVideo(
                isBookMarked = isBookmarked,
                videoPath = absolutePath
            );

            videoList.add(video);

        }
        filePathList.value = videoList
    }

}