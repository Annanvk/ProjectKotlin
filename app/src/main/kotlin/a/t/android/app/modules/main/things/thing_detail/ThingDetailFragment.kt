package io.tage.android.app.modules.main.things.thing_detail

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.animation.AlphaAnimation
import androidx.navigation.fragment.findNavController
import io.reactivex.internal.disposables.DisposableContainer
import io.tage.android.app.R
import io.tage.android.app.common.*
import io.tage.android.app.modules.auth.AuthActivity
import io.tage.android.app.modules.main.MainActivity
import io.tage.android.app.services.Services
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_details_things.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.find
import java.util.*

@SuppressLint("ValidFragment")


class ThingDetailFragment : BaseFragment() {
    override val layout: Int get() = R.layout.fragment_details_things
    override val title: Int get() = R.string.things_details
    private var myThing: Boolean? = null
    private var thing: Thing? = null
    private var viewModel = ItemDetailViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        thing = arguments?.getParcelable(THING_INSTANCE)
        myThing = arguments?.getBoolean(IS_MY_THING)
    }

    override fun setUi() {
        image_view_pager.adapter = SliderImageAdapter(context!!, thing!!.photos)
        circle_indicator_for_image.setViewPager(image_view_pager)
        if (circle_indicator_for_image.childCount > 1) circle_indicator_for_image.visible()
        else circle_indicator_for_image.invisible()
        activity<MainActivity> {
            it.enableBackBtn(true)
            it.toolbar.title = thing!!.vendor
        }
        button_rent_by.text = (getString(R.string.rent_by) + " " + getPriceText(context!!, thing!!, false))
        text_title_detail_thing.text = thing!!.title
        if (thing!!.desc.isEmpty()) {
            description_box.gone()
        } else {
            description_box.visible()
            text_description.text = thing!!.desc
        }
        text_price_item_detail.text = getPriceText(context!!, thing!!, true)
        initFields(thing!!.fields, context!!)
        val location = (activity as MainActivity).currentLocation
//      TODO temp hide
//      text_distance_thing_detail.text = context!!.distCalculate(thing!!.location, location)
        val rate = thing!!.rating
        if (rate != null) {
            raiting_holder.visible()
            rating_bar.rating = rate
        } else raiting_holder.gone()
        initReviewsList()
    }

    private fun initFields(fields: Map<String, String>, context: Context) {
        fields_box_thing_detail.removeAllViews()
        val newFields = TreeMap<String, String>(fields)
        for (key in newFields.keys) {
            val value = newFields[key]
            val line = ParametrLineDetailView(context)
            line.fillParamsKey(key, value!!)
            fields_box_thing_detail.addView(line)
        }
    }

    override fun setObservers(disposableBag: DisposableContainer) {
        button_rent_by.onClick {
            button_rent_by.startAnimation(AlphaAnimation(1F, 0.6F))
            if (Services.userAuthToken == null) startActivityForResult(Intent(activity, AuthActivity::class.java), REQUESTS_AUTH_ACTIVITY)
            else {
                val bundle = Bundle()
                bundle.putParcelable(THING_INSTANCE, thing!!)
                findNavController().navigate(R.id.action_detailsThings_to_confirmationFragment, bundle)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.toolbar_thing_detail, menu)
        val itemEdit: MenuItem = menu!!.findItem(R.id.button_edit)
        when (myThing) {
            true -> {
                itemEdit.isVisible = true
                button_rent_by.gone()
            }
            else -> {
                itemEdit.isVisible = false
                button_rent_by.visible()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.button_edit -> {
                if (thing!!.rented) {
                    infoDialog(R.string.edit_forbid_dialog_text)
                } else {
                    val bundle = Bundle()
                    bundle.putParcelable(THING_INSTANCE, thing)
                    findNavController().navigate(R.id.action_thingDetailFragment_to_addNewThingFragment, bundle)
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        activity<MainActivity> { it.enableBackBtn(false) }
    }   
}
