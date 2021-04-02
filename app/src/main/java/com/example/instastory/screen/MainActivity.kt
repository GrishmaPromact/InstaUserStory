package com.example.instastory.screen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.c2m.storyviewer.adapter.StoryUserAdapter
import com.example.instastory.data.Story
import com.example.instastory.data.StoryUser
import com.example.instastory.databinding.ActivityMainBinding
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private var position: Int? = 0
    private var storyUserModel: StoryUser? = null
    private var storyUserAdapter: StoryUserAdapter? = null
    //private var storyUsersList: ArrayList<StoryUserModel>? = null
    private var storyUsersList: ArrayList<StoryUser>? = null
    lateinit var binding: ActivityMainBinding
    companion object{
        val LAUNCH_STORY_DISPLAY_ACTIVITY = 101
        const val UPDATED_STORY_USER_LIST = "updatedStoryUserList"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        storyUsersList = getAnotherUsersList()
        initRV()
    }


    private fun getAnotherUsersList(): ArrayList<StoryUser> {
        val storyUserList = ArrayList<StoryUser>()

        val storyUserModel = StoryUser()
        storyUserModel.username = "User1"
        storyUserModel.profilePicUrl = "https://randomuser.me/api/portraits/women/1.jpg"
        storyUserModel.isPinStory = true


        val stories = Story()
        stories.url = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        //stories.url = "https://player.vimeo.com/external/403295268.sd.mp4?s=3446f787cefa52e7824d6ce6501db5261074d479&profile_id=165&oauth2_token_id=57447761"
        stories.storyDate = System.currentTimeMillis() - (1 * (24 - 0) * 60 * 60 * 1000)


        val stories1 = Story()
        stories1.url = "https://images.pexels.com/photos/3849168/pexels-photo-3849168.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260"
        stories1.storyDate = System.currentTimeMillis() - (1 * (24 - 1) * 60 * 60 * 1000)


        val stories2 = Story()
        stories2.url = "https://player.vimeo.com/external/409206405.sd.mp4?s=0bc456b6ff355d9907f285368747bf54323e5532&profile_id=165&oauth2_token_id=57447761"
        stories2.storyDate = System.currentTimeMillis() - (1 * (24 - 2) * 60 * 60 * 1000)

        val storiesList : MutableList<Story> = mutableListOf()
        storiesList.add(stories)
        storiesList.add(stories1)
        storiesList.add(stories2)

        storyUserModel.stories = storiesList

        storyUserList.add(storyUserModel)


        val storyUserModel1 = StoryUser()
        storyUserModel1.username = "User2"
        storyUserModel1.profilePicUrl = "https://randomuser.me/api/portraits/women/9.jpg"

        val stories3= Story()

        stories3.url = "https://player.vimeo.com/external/422787651.sd.mp4?s=ec96f3190373937071ba56955b2f8481eaa10cce&profile_id=165&oauth2_token_id=57447761"
        stories3.storyDate = System.currentTimeMillis() - (1 * (24 - 3) * 60 * 60 * 1000)

        val stories5 = Story()
        stories5.url = "https://images.pexels.com/photos/134020/pexels-photo-134020.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260"
        stories5.storyDate = System.currentTimeMillis() - (1 * (24 - 5) * 60 * 60 * 1000)

        val storiesList1 : MutableList<Story> = mutableListOf()
        storiesList1.add(stories3)
        storiesList1.add(stories5)

        storyUserModel1.stories = storiesList1

        storyUserList.add(storyUserModel1)


        val storyUserModel2 = StoryUser()
        storyUserModel2.username = "User3"
        storyUserModel2.profilePicUrl = "https://randomuser.me/api/portraits/men/6.jpg"

        val stories6= Story()
        stories6.url = "https://player.vimeo.com/external/394678700.sd.mp4?s=353646e34d7bde02ad638c7308a198786e0dff8f&profile_id=165&oauth2_token_id=57447761"
        stories6.storyDate = System.currentTimeMillis() - (1 * (24 - 6) * 60 * 60 * 1000)


        val stories7 = Story()
        stories7.url = "https://images.pexels.com/photos/1612461/pexels-photo-1612461.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260"
        stories7.storyDate = System.currentTimeMillis() - (1 * (24 - 7) * 60 * 60 * 1000)


        val stories8 = Story()
        stories8.url = "https://images.pexels.com/photos/2260800/pexels-photo-2260800.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260"
        stories8.storyDate = System.currentTimeMillis() - (1 * (24 - 8) * 60 * 60 * 1000)

        val storiesList2 : MutableList<Story> = mutableListOf()
        storiesList2.add(stories6)
        storiesList2.add(stories7)
        storiesList2.add(stories8)

        storyUserModel2.stories = storiesList2

        storyUserList.add(storyUserModel2)

        val storyUserModel4 = StoryUser()
        storyUserModel4.username = "User4"
        storyUserModel4.profilePicUrl = "https://randomuser.me/api/portraits/men/7.jpg"
        //storyUserModel4.isPinStory = true

        val stories12= Story()
        stories12.url = "https://player.vimeo.com/external/403295710.sd.mp4?s=788b046826f92983ada6e5caf067113fdb49e209&profile_id=165&oauth2_token_id=57447761"
        stories12.storyDate = System.currentTimeMillis() - (1 * (24 - 3) * 60 * 60 * 1000)

        val stories13 = Story()
        stories13.url = "https://images.pexels.com/photos/1591382/pexels-photo-1591382.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260"
        stories13.storyDate = System.currentTimeMillis() - (1 * (24 - 5) * 60 * 60 * 1000)

        val storiesList4 : MutableList<Story> = mutableListOf()
        storiesList4.add(stories12)
        storiesList4.add(stories13)

        storyUserModel4.stories = storiesList4

        storyUserList.add(storyUserModel4)



/*
        val storyUserModel3 = StoryUser()
        storyUserModel3.username = "User1"
        storyUserModel3.profilePicUrl = "https://randomuser.me/api/portraits/women/1.jpg"
        storyUserModel3.isPinStory = true


        val stories9 = Story()
        stories9.url = "https://player.vimeo.com/external/403295268.sd.mp4?s=3446f787cefa52e7824d6ce6501db5261074d479&profile_id=165&oauth2_token_id=57447761"
        stories9.storyDate = System.currentTimeMillis() - (1 * (24 - 0) * 60 * 60 * 1000)


        val stories10 = Story()
        stories10.url = "https://images.pexels.com/photos/3849168/pexels-photo-3849168.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260"
        stories10.storyDate = System.currentTimeMillis() - (1 * (24 - 1) * 60 * 60 * 1000)


        val stories11 = Story()
        stories11.url = "https://player.vimeo.com/external/409206405.sd.mp4?s=0bc456b6ff355d9907f285368747bf54323e5532&profile_id=165&oauth2_token_id=57447761"
        stories11.storyDate = System.currentTimeMillis() - (1 * (24 - 2) * 60 * 60 * 1000)

        val storiesList3 : MutableList<Story> = mutableListOf()
        storiesList3.add(stories9)
        storiesList3.add(stories10)
        storiesList3.add(stories11)

        storyUserModel3.stories = storiesList3

        storyUserList.add(storyUserModel3)


        val storyUserModel4 = StoryUser()
        storyUserModel4.username = "User2"
        storyUserModel4.profilePicUrl = "https://images.pexels.com/photos/2458400/pexels-photo-2458400.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260"

        val stories12= Story()
        stories12.url = "https://images.pexels.com/photos/134020/pexels-photo-134020.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260"
        stories12.storyDate = System.currentTimeMillis() - (1 * (24 - 3) * 60 * 60 * 1000)

        val stories13 = Story()
        stories13.url = "https://player.vimeo.com/external/422787651.sd.mp4?s=ec96f3190373937071ba56955b2f8481eaa10cce&profile_id=165&oauth2_token_id=57447761"
        stories13.storyDate = System.currentTimeMillis() - (1 * (24 - 5) * 60 * 60 * 1000)

        val storiesList4 : MutableList<Story> = mutableListOf()
        storiesList4.add(stories12)
        storiesList4.add(stories13)

        storyUserModel4.stories = storiesList4

        storyUserList.add(storyUserModel4)


        val storyUserModel5 = StoryUser()
        storyUserModel5.username = "User3"
        storyUserModel5.profilePicUrl = "https://randomuser.me/api/portraits/men/6.jpg"

        val stories14= Story()
        stories14.url = "https://player.vimeo.com/external/394678700.sd.mp4?s=353646e34d7bde02ad638c7308a198786e0dff8f&profile_id=165&oauth2_token_id=57447761"
        stories14.storyDate = System.currentTimeMillis() - (1 * (24 - 6) * 60 * 60 * 1000)


        val stories15 = Story()
        stories15.url = "https://images.pexels.com/photos/1612461/pexels-photo-1612461.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260"
        stories15.storyDate = System.currentTimeMillis() - (1 * (24 - 7) * 60 * 60 * 1000)


        val stories16 = Story()
        stories16.url = "https://images.pexels.com/photos/2260800/pexels-photo-2260800.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260"
        stories16.storyDate = System.currentTimeMillis() - (1 * (24 - 8) * 60 * 60 * 1000)

        val storiesList5 : MutableList<Story> = mutableListOf()
        storiesList5.add(stories14)
        storiesList5.add(stories15)
        storiesList5.add(stories16)

        storyUserModel5.stories = storiesList5

        storyUserList.add(storyUserModel5)*/

        return storyUserList
    }

    private fun initRV() {

        storyUserAdapter = StoryUserAdapter(storyUsersList, this)
        binding.rvStoryUser.adapter = storyUserAdapter


        storyUserAdapter?.onSelectionChangeListener = { storyUser: StoryUser, position: Int ->
           val intent = Intent(this, StoryDisplayActivity::class.java)
            intent.putExtra("position", position)
            intent.putParcelableArrayListExtra("list", storyUsersList as ArrayList<StoryUser>)
            startActivityForResult(intent, LAUNCH_STORY_DISPLAY_ACTIVITY)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == LAUNCH_STORY_DISPLAY_ACTIVITY && resultCode == RESULT_OK){

            if(data?.hasExtra(UPDATED_STORY_USER_LIST) == true) {
                storyUsersList = data.getParcelableArrayListExtra(UPDATED_STORY_USER_LIST)

                storyUsersList?.forEachIndexed { index, storyUser ->
                    if(storyUser.isPinStory==false) {
                        if (storyUser.viewInex == storyUser.stories?.size) {
                            storyUser.isAllStorySeen = true
                        }
                    }
                    Log.d("viewedMain", "${storyUsersList!![index].viewInex}")
                }

                val newList = storyUsersList?.sortedWith(compareBy({ it.isAllStorySeen == true},{it.isPinStory == false}))
                this.storyUsersList?.clear()
                this.storyUsersList?.addAll(newList!!)
                storyUserAdapter?.updateList(storyUsersList)

            }


           /* if(data?.hasExtra("storyUserList") == true){
                storyUsersList = data.getParcelableArrayListExtra("storyUserList")


                storyUsersList?.forEachIndexed { index, storyUserModel ->
                    //storySeenList?.size == storyUserModel.storiesList?.size
                    var storySeenList :  MutableList<Boolean>? = mutableListOf()
                    storyUserModel.storiesList?.forEachIndexed { index, stories ->
                        if(stories.isStorySeen==true)
                            storySeenList?.add(stories.isStorySeen!!)
                    }

                    storyUserModel.isStorySeen = storyUserModel.storiesList?.size == storySeenList?.size

                }
                storyUserAdapter?.updateList(storyUsersList)
            }*/
        }
    }

    override fun onResume() {
        super.onResume()
    }

}