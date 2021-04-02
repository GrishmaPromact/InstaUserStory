package com.example.instastory.screen

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.SparseIntArray
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.bumptech.glide.Glide
import com.example.instastory.app.StoryApp
import com.example.instastory.customview.StoryPagerAdapter
import com.example.instastory.data.StoryUser
import com.example.instastory.utils.CubeOutTransformer
import com.example.instastory.R
import com.example.instastory.databinding.ActivityStoryDisplayBinding
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheUtil
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class StoryDisplayActivity : AppCompatActivity(),
        PageViewOperator {

    private lateinit var pagerAdapter: StoryPagerAdapter
    private var currentPage: Int = 0
    private var position: Int? = 0
    var storyUserList = ArrayList<StoryUser>()
    lateinit var binding: ActivityStoryDisplayBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        position = intent.extras?.getInt("position")
        currentPage = position!!

        if(intent.hasExtra("list")){
            storyUserList = intent.extras?.getParcelableArrayList<StoryUser>("list")!!
        }
        setUpPager()
    }

    override fun backPageView() {
        if (binding.viewPager?.currentItem > 0) {
            try {
                fakeDrag(false)
            } catch (e: Exception) {
                //NO OP
            }
        }
    }

    override fun nextPageView() {
        if (binding.viewPager?.currentItem + 1 < binding.viewPager?.adapter?.count ?: 0) {
            try {
                fakeDrag(true)
            } catch (e: Exception) {
                //NO OP
            }
        } else {
            //there is no next story
            onBackPressed()
            Toast.makeText(this, "All stories displayed.", Toast.LENGTH_LONG).show()
        }
    }

    private fun setUpPager() {
        //val storyUserList = StoryGenerator.generateStories()

        progressState.clear()
        storyUserList.forEachIndexed { index, storyUser ->
            if (storyUser!!.viewInex!! < storyUser.stories!!.size)
                progressState.put(index, storyUser.viewInex!!)
            else progressState.put(index, 0)
        }

        Log.d("progressState","$progressState")

        preLoadStories(storyUserList)

        pagerAdapter = StoryPagerAdapter(
                supportFragmentManager,
                storyUserList,
                ::updateStoryUserList
        )
        binding.viewPager?.adapter = pagerAdapter
        binding.viewPager?.currentItem = currentPage
        binding.viewPager?.setPageTransformer(
            true,
            CubeOutTransformer()
        )
        binding.viewPager?.addOnPageChangeListener(object : PageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPage = position
            }

            override fun onPageScrollCanceled() {
                currentFragment()?.resumeCurrentStory()
            }
        })
    }

    private fun preLoadStories(storyUserList: ArrayList<StoryUser>) {
        val imageList = mutableListOf<String>()
        val videoList = mutableListOf<String>()

        storyUserList.forEach { storyUser ->
            storyUser.stories?.forEach { story ->
                if (story?.isVideo() == true) {
                    videoList.add(story.url.toString())
                } else {
                    imageList.add(story.url.toString())
                }
            }
        }
        preLoadVideos(videoList)
        preLoadImages(imageList)
    }

    private fun preLoadVideos(videoList: MutableList<String>) {
        videoList.map { data ->
            GlobalScope.async {
                val dataUri = Uri.parse(data)
                val dataSpec = DataSpec(dataUri, 0, 500 * 1024, null)
                val dataSource: DataSource =
                    DefaultDataSourceFactory(
                        applicationContext,
                        Util.getUserAgent(applicationContext, getString(R.string.app_name))
                    ).createDataSource()

                val listener =
                    CacheUtil.ProgressListener { requestLength: Long, bytesCached: Long, _: Long ->
                        val downloadPercentage = (bytesCached * 100.0
                                / requestLength)
                        Log.d("preLoadVideos", "downloadPercentage: $downloadPercentage")
                    }

                try {
                    CacheUtil.cache(
                        dataSpec,
                        StoryApp.simpleCache,
                        CacheUtil.DEFAULT_CACHE_KEY_FACTORY,
                        dataSource,
                        listener,
                        null
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun preLoadImages(imageList: MutableList<String>) {
        imageList.forEach { imageStory ->
            Glide.with(this).load(imageStory).preload()
        }
    }

    private fun currentFragment(): StoryDisplayFragment? {
        return pagerAdapter.findFragmentByPosition(binding.viewPager, currentPage) as StoryDisplayFragment
    }

    /**
     * Change ViewPage sliding programmatically(not using reflection).
     * https://tech.dely.jp/entry/2018/12/13/110000
     * What for?
     * setCurrentItem(int, boolean) changes too fast. And it cannot set animation duration.
     */
    private var prevDragPosition = 0

    private fun fakeDrag(forward: Boolean) {
        if (prevDragPosition == 0 && binding.viewPager?.beginFakeDrag()) {
            ValueAnimator.ofInt(0, binding.viewPager?.width).apply {
                duration = 400L
                interpolator = FastOutSlowInInterpolator()
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(p0: Animator?) {}

                    override fun onAnimationEnd(animation: Animator?) {
                        removeAllUpdateListeners()
                        if (binding.viewPager?.isFakeDragging) {
                            binding.viewPager?.endFakeDrag()
                        }
                        prevDragPosition = 0
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                        removeAllUpdateListeners()
                        if (binding.viewPager?.isFakeDragging) {
                            binding.viewPager?.endFakeDrag()
                        }
                        prevDragPosition = 0
                    }

                    override fun onAnimationStart(p0: Animator?) {}
                })
                addUpdateListener {
                    if (!binding.viewPager?.isFakeDragging) return@addUpdateListener
                    val dragPosition: Int = it.animatedValue as Int
                    val dragOffset: Float =
                        ((dragPosition - prevDragPosition) * if (forward) -1 else 1).toFloat()
                    prevDragPosition = dragPosition
                    binding.viewPager?.fakeDragBy(dragOffset)
                }
            }.start()
        }
    }

    companion object {
        val progressState = SparseIntArray()
    }

    override fun onBackPressed() {
        storyUserList.forEachIndexed { index, storyUser ->
            Log.d("viewedIndex", "${storyUserList[index].viewInex}")
        }
        val intent = Intent()
        intent.putParcelableArrayListExtra(MainActivity.UPDATED_STORY_USER_LIST,storyUserList)
        setResult(Activity.RESULT_OK,intent)
        finish()
        super.onBackPressed()
    }

    private fun updateStoryUserList(userIndex: Int, viewIndex: Int){
        if (storyUserList[userIndex].viewInex!! < (viewIndex+1)){
            storyUserList[userIndex].viewInex = (viewIndex+1)
        }
    }
}
