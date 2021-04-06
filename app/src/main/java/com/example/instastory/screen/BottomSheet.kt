package com.example.instastory.screen

import android.app.Dialog
import android.content.DialogInterface
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.instastory.R
import com.example.instastory.databinding.LayoutBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class BottomSheet : BottomSheetDialogFragment() {

    private lateinit var onBottomSheetCloseListener:OnBottomSheetCloseListener
    var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    var bi: LayoutBottomSheetBinding? = null

    private class MyWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheet = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        //inflating layout
        val view = View.inflate(context, R.layout.layout_bottom_sheet, null)

        //binding views to data binding.
        bi = DataBindingUtil.bind(view)

        //setting layout with bottom sheet
        bottomSheet.setContentView(view)
        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)


        //setting Peek at the 16:9 ratio keyline of its parent.
        (bottomSheetBehavior as BottomSheetBehavior<View>).peekHeight = Resources.getSystem().displayMetrics.heightPixels
        (bottomSheetBehavior as BottomSheetBehavior<View>).state = BottomSheetBehavior.STATE_EXPANDED
        showView(bi?.appBarLayout!!, actionBarSize)
        //setting max height of bottom sheet
        bi?.extraSpace?.setMinimumHeight(Resources.getSystem().displayMetrics.heightPixels)


        (bottomSheetBehavior as BottomSheetBehavior<View>).setBottomSheetCallback(object :
            BottomSheetCallback() {
            override fun onStateChanged(view: View, i: Int) {
                if (BottomSheetBehavior.STATE_EXPANDED == i) {
                    // hideAppBar(bi!!.profileLayout)
                    showView(bi?.appBarLayout!!, actionBarSize)

                }
                if (BottomSheetBehavior.STATE_COLLAPSED == i) {
                    //hideAppBar(bi!!.appBarLayout)
                    showView(bi!!.webView, actionBarSize)
                }
                if (BottomSheetBehavior.STATE_HIDDEN == i) {
                    dismiss()
                }
            }

            override fun onSlide(view: View, v: Float) {}
        })

        //aap bar cancel button clicked
        bi?.cancelBtn?.setOnClickListener(View.OnClickListener { dismiss() })

        //aap bar edit button clicked
       /* bi?.editBtn?.setOnClickListener(View.OnClickListener {
            Toast.makeText(
                context,
                "Edit button clicked",
                Toast.LENGTH_SHORT
            ).show()
        })

        //aap bar more button clicked
        bi?.moreBtn?.setOnClickListener(View.OnClickListener {
            Toast.makeText(
                context,
                "More button clicked",
                Toast.LENGTH_SHORT
            ).show()
        })
*/

        //hiding app bar at the start
        //hideAppBar(bi?.appBarLayout!!)


        bi?.webView?.loadUrl(tag.toString())
        bi?.webView?.webViewClient = MyWebViewClient()
        return bottomSheet
    }

    override fun onStart() {
        super.onStart()
        bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun hideAppBar(view: View) {
        val params = view.layoutParams
        params.height = 0
        view.layoutParams = params
    }

    private fun showView(view: View, size: Int) {
        val params = view.layoutParams
        params.height = size
        view.layoutParams = params
    }

    private val actionBarSize: Int
        private get() {
            val array =
                context!!.theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
            return array.getDimension(0, 0f).toInt()
        }


    interface OnBottomSheetCloseListener{
        fun onBottomSheetClose()
    }

    fun setOnBottomSheetCloseListener(listener:OnBottomSheetCloseListener) {
        onBottomSheetCloseListener = listener
    }

    // rest of the code
    override fun onDismiss(dialog: DialogInterface)
    {
        super.dismiss()
        onBottomSheetCloseListener.onBottomSheetClose()
    }
}