package com.mizardar.trelltest.view.listeners

import com.mizardar.trelltest.model.ModelVideo

interface VideoInteractionListener {
    fun onShareClick(modelVideo: ModelVideo)
    fun onBookmarkClick(modelVideo: ModelVideo)
    fun onPositionChanged(modelVideo: ModelVideo)
    fun onVideoCompleted()
}