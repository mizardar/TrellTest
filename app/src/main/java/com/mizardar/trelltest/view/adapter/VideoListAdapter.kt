package com.mizardar.trelltest.view.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.MediaController
import androidx.recyclerview.widget.RecyclerView
import com.mizardar.trelltest.model.ModelVideo
import com.mizardar.trelltest.view.listeners.VideoInteractionListener
import com.mizardar.trelltest.databinding.ItemVideoBinding
import java.io.File

class VideoListAdapter(
    private val context: Context,
    private val videoList: List<ModelVideo>,
    private val videoInteractionListener: VideoInteractionListener
) : RecyclerView.Adapter<VideoListAdapter.VideoHolder>(){



    inner class VideoHolder(private val itemVideoBinding: ItemVideoBinding) : RecyclerView.ViewHolder(itemVideoBinding.root){

        fun setVideoModel(modelVideo: ModelVideo){
            val file = File(modelVideo.videoPath)
            itemVideoBinding.modelVideo = modelVideo
            itemVideoBinding.videoInteractionListener = videoInteractionListener
            itemVideoBinding.videoView.setVideoURI(Uri.fromFile(file))
            itemVideoBinding.videoView.setMediaController(MediaController(context));
            itemVideoBinding.videoView.requestFocus();
            itemVideoBinding.videoView.start()
            itemVideoBinding.videoView.resume()
            itemVideoBinding.videoView.setOnCompletionListener {
                videoInteractionListener.onVideoCompleted()
            }
            videoInteractionListener.onPositionChanged(modelVideo)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
        val itemVideoBinding = ItemVideoBinding.inflate(LayoutInflater.from(context),parent,false)
        return VideoHolder(itemVideoBinding)
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    override fun onBindViewHolder(holder: VideoHolder, position: Int) {
        val modelVideo = videoList[position]
        holder.setVideoModel(modelVideo)
    }
}