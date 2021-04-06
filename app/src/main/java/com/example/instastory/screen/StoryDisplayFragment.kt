package com.example.instastory.screen

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.instastory.R
import com.example.instastory.adapter.CommentsAdapter
import com.example.instastory.app.StoryApp
import com.example.instastory.customview.StoriesProgressView
import com.example.instastory.data.CommentsModel
import com.example.instastory.data.Story
import com.example.instastory.data.StoryUser
import com.example.instastory.databinding.FragmentStoryDisplayBinding
import com.example.instastory.utils.OnSwipeTouchListener
import com.example.instastory.utils.RelativeLayoutTouchListener
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

    private var updateStoryUserList: ((Int, Int) -> Unit)? = null
    private var isKeyboardShowing: Boolean? = false
    private var commentsAdapter: CommentsAdapter? = null

    private val position: Int by
    lazy { arguments?.getInt(EXTRA_POSITION) ?: 0 }

    private val storyUser: StoryUser by
    lazy {
        (arguments?.getParcelable<StoryUser>(
                EXTRA_STORY_USER
        ) as StoryUser)
    }

    private val stories: MutableList<Story> by
    lazy { storyUser.stories!! }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.storyDisplayVideo.useController = false
        counter = restorePosition()
        //updateStoryUserList?.let { it(position,counter) }
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
        counter = restorePosition()
        Log.d("onStart", "$counter")
    }

    override fun onResume() {
        super.onResume()
        onResumeCalled = true
        //counter = restorePosition()
        updateStoryUserList?.let { it(position, counter) }
        updateStory()
        if (stories[counter].isVideo() == true && !onVideoPrepared) {
            simpleExoPlayer?.playWhenReady = false
            return
        }

        simpleExoPlayer?.seekTo(5)
        simpleExoPlayer?.playWhenReady = true
        if (counter == 0) {
            binding.storiesProgressView.startStories()
        } else {
            // restart animation
            counter = StoryDisplayActivity.progressState.get(arguments?.getInt(EXTRA_POSITION) ?: 0)
            binding.storiesProgressView.startStories(counter)
        }
    }

    override fun onPause() {
        super.onPause()
        simpleExoPlayer?.playWhenReady = false
        binding.storiesProgressView.abandon()
    }

    override fun onComplete() {
        simpleExoPlayer?.release()
        pageViewOperator?.nextPageView()
    }

    override fun onPrev() {

        if (counter - 1 < 0) return
        --counter
        savePosition(counter)
        stories[counter].commentsList?.clear()
        commentsAdapter?.updateList(stories[counter].commentsList)
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
        stories[counter].commentsList?.clear()
        commentsAdapter?.updateList(stories[counter].commentsList)
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
            binding.ivUnMute.visibility = View.VISIBLE
        } else {
            binding.storyDisplayVideo.hide()
            binding.storyDisplayVideoProgress.hide()
            binding.storyDisplayImage.show()
            Glide.with(this).load(stories[counter].url).into(binding.storyDisplayImage)
            binding.ivMute.visibility = View.GONE
            binding.ivUnMute.visibility = View.GONE
        }

        val cal: Calendar = Calendar.getInstance(Locale.ENGLISH).apply {
            timeInMillis = stories[counter].storyDate!!
        }
        binding.storyDisplayTime.text = DateFormat.format("MM-dd-yyyy HH:mm:ss", cal).toString()

        if(!stories[counter].ctaText.isNullOrEmpty()){
            binding.llOfferView.show()
            binding.bottomOverlayView.show()
            binding.tvGetOffer.text = stories[counter].ctaText
        }else{
            binding.llOfferView.hide()
            binding.bottomOverlayView.hide()
        }
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
                    binding.storiesProgressView.skip()
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
                    binding.storiesProgressView.getProgressWithIndex(counter)
                            .setDuration(simpleExoPlayer?.duration ?: 8000L)
                    onVideoPrepared = true
                    resumeCurrentStory()
                }
            }
        })

        val curentVol = simpleExoPlayer?.volume


        binding.ivUnMute.setOnClickListener {
            if (curentVol == 0f) {
                simpleExoPlayer?.volume = 1f
                binding.ivUnMute.visibility = View.VISIBLE
                binding.ivMute.visibility = View.GONE
            } else {
                simpleExoPlayer?.volume = 0f
                binding.ivMute.visibility = View.VISIBLE
                binding.ivUnMute.visibility = View.GONE
            }
        }

        binding.ivMute.setOnClickListener {
            if (curentVol == 0f) {
                simpleExoPlayer?.volume = 0f
                binding.ivUnMute.visibility = View.VISIBLE
                binding.ivMute.visibility = View.GONE
            } else {
                simpleExoPlayer?.volume = 1f
                binding.ivMute.visibility = View.GONE
                binding.ivUnMute.visibility = View.VISIBLE
            }
        }
    }

    private fun setUpUi() {
        val logTag = "ActivitySwipeDetector"
        val MIN_DISTANCE = 50

        var downX: Float ? = 0f
        var downY: Float? = 0f
        var upX: Float
        var upY: Float
        val touchListener = object : OnSwipeTouchListener(activity!!) {
            override fun onSwipeTop() {
                Toast.makeText(activity, "onSwipeTop", Toast.LENGTH_LONG).show()
            }

            override fun onSwipeBottom() {
                Toast.makeText(activity, "onSwipeBottom", Toast.LENGTH_LONG).show()
            }

           /* override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = event.getX();
                        downY = event.getY();
                        return true;
                    }
                    MotionEvent.ACTION_UP -> {
                        upX = event.getX();
                        upY = event.getY();

                        val deltaX = downX!! - upX;
                        val deltaY = downY!! - upY;

                        // swipe horizontal?
                        if (Math.abs(deltaX) > MIN_DISTANCE) {
                            // left or right
                            if (deltaX < 0) {
                                //this.onLeftToRightSwipe();
                                //Toast.makeText(activity, "left to right swipe", Toast.LENGTH_SHORT).show()
                                return true;
                            }
                            if (deltaX > 0) {
                                //this.onRightToLeftSwipe();
                                //Toast.makeText(activity, "right to left swipe", Toast.LENGTH_SHORT).show()
                                return true;
                            }
                        } else {
                            Log.i(logTag, "Swipe was only " + Math.abs(deltaX) + " long horizontally, need at least " + MIN_DISTANCE);
                            // return false; // We don't consume the event
                        }

                        // swipe vertical?
                        if (Math.abs(deltaY) > MIN_DISTANCE) {
                            // top or down
                            if (deltaY < 0) {
                                Toast.makeText(activity, "top to bottom swipe", Toast.LENGTH_SHORT).show()
                                //this.onTopToBottomSwipe();
                                return true;
                            }
                            if (deltaY > 0) {
                                //this.onBottomToTopSwipe();
                                Toast.makeText(activity, "bottom to top swipe", Toast.LENGTH_SHORT).show()
                                return true;
                            }
                        } else {
                            Log.i(logTag, "Swipe was only " + Math.abs(deltaX) + " long vertically, need at least " + MIN_DISTANCE);
                            // return false; // We don't consume the event
                        }

                        return false; // no swipe horizontally and no swipe vertically
                    }// case MotionEvent.ACTION_UP:
                }
                return false
            }
*/
            override fun onClick(view: View) {
                when (view) {
                    binding.next -> {
                        if (counter == stories.size - 1) {
                            pageViewOperator?.nextPageView()
                        } else {
                            binding.storiesProgressView.skip()
                        }
                    }
                    binding.previous -> {
                        if (counter == 0) {
                            pageViewOperator?.backPageView()
                        } else {
                            binding.storiesProgressView.reverse()
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
                        //Toast.makeText(requireContext(), "action swipe up", Toast.LENGTH_SHORT).show()
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

        binding.storiesProgressView.setStoriesCountDebug(
                stories.size, position = arguments?.getInt(EXTRA_POSITION) ?: -1
        )
        binding.storiesProgressView.setAllStoryDuration(8000L)
        binding.storiesProgressView.setStoriesListener(this)
        binding.storiesProgressView.startStories(counter)
        Glide.with(this).load(storyUser.profilePicUrl).circleCrop().into(binding.storyDisplayProfilePicture)
        binding.storyDisplayNick.text = storyUser.username


        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            binding.root.getWindowVisibleDisplayFrame(r)
            val screenHeight = binding.root.rootView.height

            // r.bottom is the position above soft keypad or device button.
            // if keypad is shown, the r.bottom is smaller than that before.
            val keypadHeight = screenHeight - r.bottom

            Log.d("hi::", "keypadHeight = $keypadHeight")

            if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                // keyboard is opened
                if (!isKeyboardShowing!!) {
                    isKeyboardShowing = true
                    onKeyboardVisibilityChanged(true)
                }
            }
            else {
                // keyboard is closed
                if (isKeyboardShowing == true) {
                    isKeyboardShowing = false
                    onKeyboardVisibilityChanged(false)
                }
            }
        }

        initRV()

        //val bottomSheetBehavior = BottomSheetBehavior.from<View>(binding.bottomSheet.commentsLayout)

        binding.bottomSheet.etComment.clearFocus()

        binding.bottomSheet.etComment.setOnClickListener {
            //bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            //val bottomSheetDialogFragment: BottomSheetDialogFragment = SendBottomSheetFragment()
            //bottomSheetDialogFragment.show(fragmentManager!!, bottomSheetDialogFragment.tag)

        }


        binding.bottomSheet.btnSendComment.setOnClickListener{
            val commentsModel = CommentsModel()
            commentsModel.userComment = binding.bottomSheet.etComment.text.toString()
            commentsModel.userProfileUrl = storyUser.profilePicUrl

            stories[counter].commentsList?.add(commentsModel)
            commentsAdapter?.updateList(stories[counter].commentsList!!)
            binding.bottomSheet.rvComments.scrollToPosition(commentsAdapter?.itemCount!! - 1)
            binding.bottomSheet.etComment.setText("")
        }

        binding.ivCloseStory.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.ivShare.setOnClickListener {
            Toast.makeText(requireActivity(), "Perform Share Action!!", Toast.LENGTH_SHORT).show()
        }

        binding.llOfferView.setOnClickListener {
            pauseCurrentStory()
            val bottomSheet = BottomSheet()
            bottomSheet.show(activity!!.supportFragmentManager, stories[counter].ctaUrl)
            bottomSheet.setOnBottomSheetCloseListener(object :
                BottomSheet.OnBottomSheetCloseListener {
                override fun onBottomSheetClose() {
                    //Toast.makeText(requireActivity(), "closedd...", Toast.LENGTH_SHORT).show()
                    resumeCurrentStory()
                }

            })
        }
        val listener = RelativeLayoutTouchListener(requireActivity())
        listener.setOnSwipeUpListener(object  : RelativeLayoutTouchListener.OnSwipeUp{
            override fun onSwipeUp() {
                pauseCurrentStory()
                val bottomSheet = BottomSheet()
                bottomSheet.show(activity!!.supportFragmentManager, stories[counter].ctaUrl)
                bottomSheet.setOnBottomSheetCloseListener(object :
                    BottomSheet.OnBottomSheetCloseListener {
                    override fun onBottomSheetClose() {
                        //Toast.makeText(requireActivity(), "closedd...", Toast.LENGTH_SHORT).show()
                        resumeCurrentStory()
                    }

                })
            }

        })
        binding.llOfferView.setOnTouchListener(listener)
    }


    private fun onKeyboardVisibilityChanged(opened: Boolean) {
        Log.d("hi::", "onKeyboardVisibilityChanged: keyboard $opened")
        //isKeyboardOpen = opened

        if (opened) {
            pauseCurrentStory()
        } else {
            resumeCurrentStory()
        }
    }

    private fun initRV() {

        commentsAdapter = CommentsAdapter(mutableListOf(), requireActivity())
        binding.bottomSheet.rvComments.adapter = commentsAdapter

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
        updateStoryUserList?.let { it(position, pos) }
    }

    private fun restorePosition(): Int {
        return StoryDisplayActivity.progressState.get(position)
    }

    fun pauseCurrentStory() {
        simpleExoPlayer?.playWhenReady = false
        binding.storiesProgressView.pause()
    }

    fun resumeCurrentStory() {
        if (onResumeCalled) {
            simpleExoPlayer?.playWhenReady = true
            showStoryOverlay()
            binding.storiesProgressView.resume()
        }
    }

    companion object {
        private const val EXTRA_POSITION = "EXTRA_POSITION"
        private const val EXTRA_STORY_USER = "EXTRA_STORY_USER"
        fun newInstance(position: Int, story: StoryUser, updateStoryUserList: (userIndex: Int, viewIndex: Int) -> Unit): StoryDisplayFragment {
            return StoryDisplayFragment().apply {
                arguments = Bundle().apply {
                    putInt(EXTRA_POSITION, position)
                    putParcelable(EXTRA_STORY_USER, story)
                }
                this.updateStoryUserList = updateStoryUserList
            }
        }
    }

}