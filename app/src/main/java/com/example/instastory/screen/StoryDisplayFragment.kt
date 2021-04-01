package com.example.instastory.screen

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.instastory.R
import com.example.instastory.app.StoryApp
import com.example.instastory.customview.StoriesProgressView
import com.example.instastory.data.Story
import com.example.instastory.data.StoryUser
import com.example.instastory.utils.OnSwipeTouchListener
import com.example.instastory.databinding.FragmentStoryDisplayBinding
import com.example.instastory.utils.hide
import com.example.instastory.utils.show
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.util.*

class StoryDisplayFragment : Fragment(),
    StoriesProgressView.StoriesListener {

    private val position: Int by
    lazy { arguments?.getInt(EXTRA_POSITION) ?: 0 }

    private val storyUser: StoryUser by
    lazy {
        (arguments?.getParcelable<StoryUser>(
            EXTRA_STORY_USER
        ) as StoryUser)
    }

    private val stories: MutableList<Story> by
    lazy { storyUser?.stories!! }

    private var simpleExoPlayer: SimpleExoPlayer? = null
    private lateinit var mediaDataSourceFactory: DataSource.Factory
    private var pageViewOperator: PageViewOperator? = null
    private var counter = 0
    private var pressTime = 0L
    private var limit = 500L
    private var onResumeCalled = false
    private var onVideoPrepared = false

    lateinit var binding: FragmentStoryDisplayBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStoryDisplayBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.storyDisplayVideo.useController = false
        updateStory()
        setUpUi()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.pageViewOperator = context as PageViewOperator
    }

    override fun onStart() {
        super.onStart()
        //Grishma commented this line
        //counter = restorePosition()
    }

    override fun onResume() {
        super.onResume()
        onResumeCalled = true
        if (stories[counter].isVideo() == true && !onVideoPrepared) {
            simpleExoPlayer?.playWhenReady = false
            return
        }

        simpleExoPlayer?.seekTo(5)
        simpleExoPlayer?.playWhenReady = true
        if (counter == 0) {
            binding.storiesProgressView?.startStories()
        } else {
            // restart animation
            counter = StoryDisplayActivity.progressState.get(arguments?.getInt(EXTRA_POSITION) ?: 0)
            binding.storiesProgressView?.startStories(counter)
        }
    }

    override fun onPause() {
        super.onPause()
        simpleExoPlayer?.playWhenReady = false
        binding.storiesProgressView?.abandon()
    }

    override fun onComplete() {
        simpleExoPlayer?.release()
        pageViewOperator?.nextPageView()
    }

    override fun onPrev() {
        if (counter - 1 < 0) return
        --counter
        savePosition(counter)
        updateStory()
    }

    override fun onNext() {
        if (stories.size <= counter + 1) {
            return
        }
        Log.e("hi::", "onNext: counter before increment: $counter")
        ++counter
        savePosition(counter)
        Log.e("hi::", "onNext: counter after increment: $counter")
        updateStory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        simpleExoPlayer?.release()
    }

    private fun updateStory() {
        simpleExoPlayer?.stop()
        if (stories[counter].isVideo() == true) {
            binding.storyDisplayVideo.show()
            binding.storyDisplayImage.hide()
            binding.storyDisplayVideoProgress.show()
            initializePlayer()
        } else {
            binding.storyDisplayVideo.hide()
            binding.storyDisplayVideoProgress.hide()
            binding.storyDisplayImage.show()
            Glide.with(this).load(stories[counter].url).into(binding.storyDisplayImage)
        }

        val cal: Calendar = Calendar.getInstance(Locale.ENGLISH).apply {
            timeInMillis = stories[counter].storyDate!!
        }
        binding.storyDisplayTime.text = DateFormat.format("MM-dd-yyyy HH:mm:ss", cal).toString()
    }

    private fun initializePlayer() {
        if (simpleExoPlayer == null) {
            simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(requireContext())
        } else {
            simpleExoPlayer?.release()
            simpleExoPlayer = null
            simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(requireContext())
        }

        mediaDataSourceFactory = CacheDataSourceFactory(
            StoryApp.simpleCache,
            DefaultHttpDataSourceFactory(
                Util.getUserAgent(
                    context,
                    Util.getUserAgent(requireContext(), getString(R.string.app_name))
                )
            )
        )
        val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(
            Uri.parse(stories[counter].url)
        )
        simpleExoPlayer?.prepare(mediaSource, false, false)
        if (onResumeCalled) {
            simpleExoPlayer?.playWhenReady = true
        }

        binding.storyDisplayVideo.setShutterBackgroundColor(Color.BLACK)
        binding.storyDisplayVideo.player = simpleExoPlayer

        simpleExoPlayer?.addListener(object : Player.EventListener {
            override fun onPlayerError(error: ExoPlaybackException?) {
                super.onPlayerError(error)
                binding.storyDisplayVideoProgress.hide()
                if (counter == stories.size.minus(1)) {
                    pageViewOperator?.nextPageView()
                } else {
                    binding.storiesProgressView?.skip()
                }
            }

            override fun onLoadingChanged(isLoading: Boolean) {
                super.onLoadingChanged(isLoading)
                if (isLoading) {
                    binding.storyDisplayVideoProgress.show()
                    pressTime = System.currentTimeMillis()
                    pauseCurrentStory()
                } else {
                    binding.storyDisplayVideoProgress.hide()
                    binding.storiesProgressView?.getProgressWithIndex(counter)
                        ?.setDuration(simpleExoPlayer?.duration ?: 8000L)
                    onVideoPrepared = true
                    resumeCurrentStory()
                }
            }
        })
    }

    private fun setUpUi() {
        val touchListener = object : OnSwipeTouchListener(activity!!) {
            override fun onSwipeTop() {
                Toast.makeText(activity, "onSwipeTop", Toast.LENGTH_LONG).show()
            }

            override fun onSwipeBottom() {
                Toast.makeText(activity, "onSwipeBottom", Toast.LENGTH_LONG).show()
            }

            override fun onClick(view: View) {
                when (view) {
                    binding.next -> {
                        if (counter == stories.size - 1) {
                            pageViewOperator?.nextPageView()
                        } else {
                            binding.storiesProgressView?.skip()
                        }
                    }
                    binding.previous -> {
                        if (counter == 0) {
                            pageViewOperator?.backPageView()
                        } else {
                            binding.storiesProgressView?.reverse()
                        }
                    }
                }
            }

            override fun onLongClick() {
                hideStoryOverlay()
            }

            override fun onTouchView(view: View, event: MotionEvent): Boolean {
                super.onTouchView(view, event)
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        pressTime = System.currentTimeMillis()
                        pauseCurrentStory()
                        return false
                    }
                    MotionEvent.ACTION_UP -> {
                        showStoryOverlay()
                        resumeCurrentStory()
                        return limit < System.currentTimeMillis() - pressTime
                    }
                }
                return false
            }
        }
        binding.previous.setOnTouchListener(touchListener)
        binding.next.setOnTouchListener(touchListener)

        binding.storiesProgressView?.setStoriesCountDebug(
            stories.size, position = arguments?.getInt(EXTRA_POSITION) ?: -1
        )
        binding.storiesProgressView?.setAllStoryDuration(4000L)
        binding.storiesProgressView?.setStoriesListener(this)

        Glide.with(this).load(storyUser.profilePicUrl).circleCrop().into(binding.storyDisplayProfilePicture)
        binding.storyDisplayNick.text = storyUser.username
    }

    private fun showStoryOverlay() {
        if (binding.storyOverlay == null || binding.storyOverlay.alpha != 0F) return

        binding.storyOverlay.animate()
            .setDuration(100)
            .alpha(1F)
            .start()
    }

    private fun hideStoryOverlay() {
        if (binding.storyOverlay == null || binding.storyOverlay.alpha != 1F) return

        binding.storyOverlay.animate()
            .setDuration(200)
            .alpha(0F)
            .start()
    }

    private fun savePosition(pos: Int) {
        StoryDisplayActivity.progressState.put(position, pos)
    }

    private fun restorePosition(): Int {
        return StoryDisplayActivity.progressState.get(position)
    }

    fun pauseCurrentStory() {
        simpleExoPlayer?.playWhenReady = false
        binding.storiesProgressView?.pause()
    }

    fun resumeCurrentStory() {
        if (onResumeCalled) {
            simpleExoPlayer?.playWhenReady = true
            showStoryOverlay()
            binding.storiesProgressView?.resume()
        }
    }

    companion object {
        private const val EXTRA_POSITION = "EXTRA_POSITION"
        private const val EXTRA_STORY_USER = "EXTRA_STORY_USER"
        fun newInstance(position: Int, story: StoryUser): StoryDisplayFragment {
            return StoryDisplayFragment().apply {
                arguments = Bundle().apply {
                    putInt(EXTRA_POSITION, position)
                    putParcelable(EXTRA_STORY_USER, story)
                }
            }
        }
    }
}