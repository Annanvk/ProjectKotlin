package io.tage.android.app.modules.main.things.thing_detail

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.tage.android.app.R
import io.tage.android.app.common.load
import kotlinx.android.synthetic.main.sliding_image_layout.view.*

class SliderImageAdapter(val context: Context, val photos: Array<String>) : PagerAdapter() {

    override fun isViewFromObject(view: View, obj: Any) = view == obj

    override fun getCount() = photos.size

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageLayout = LayoutInflater.from(context).inflate(R.layout.sliding_image_layout, container, false)
        imageLayout.image_slide.load(photos[position], imageLayout.progress_image_thing_detail)
        container.addView(imageLayout, 0)
        return imageLayout
    }
}