package io.tage.android.app.modules.main.things.my_thing

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import io.tage.android.app.R
import io.tage.android.app.common.Thing
import io.tage.android.app.common.getPriceText
import io.tage.android.app.common.loadThumb
import io.tage.android.app.databinding.RawMyThingsItemsBinding
import io.tage.android.app.modules.main.home.ParametrLineView
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.text.SimpleDateFormat
import java.util.*


class MyThingsAdapter(val thingsList: ArrayList<Thing> = ArrayList(), val fragment: ThingsFragment) : RecyclerView.Adapter<MyThingsAdapter.MyThingsViewHolder>(), Filterable {

    var filteredThingsList: ArrayList<Thing>? = null

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
                val results = Filter.FilterResults()
                if (constraint == null || constraint.isEmpty()) {
                    filteredThingsList = ArrayList(thingsList.size)
                    filteredThingsList!!.addAll(thingsList)
                    results.values = thingsList
                    results.count = thingsList.size
                } else {
                    val txt = constraint.toString().toLowerCase()
                    val filt = thingsList.filter {
                        it.title.toLowerCase().contains(txt) or it.vendor.toLowerCase().contains(txt)
                    }
                    filteredThingsList = ArrayList(filt.size)
                    filteredThingsList!!.addAll(filt)
                    results.values = filt
                    results.count = filt.size ?: 0
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                notifyDataSetChanged()
            }
        }
    }

    lateinit var onClickThingDetail: (Thing) -> Unit

    fun addData(data: Array<Thing>) {
        thingsList.clear()
        thingsList.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyThingsViewHolder =
            MyThingsViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.raw_my_things_items, parent, false))
            { openThingDetail(it) }

    override fun getItemCount() = filteredThingsList?.size ?: 0

    override fun onBindViewHolder(holder: MyThingsViewHolder, position: Int) = holder.bind(filteredThingsList!![position], fragment)

    class MyThingsViewHolder(val binding: RawMyThingsItemsBinding, private val click: (Thing) -> Unit) : RecyclerView.ViewHolder(binding.root) {


        private val image: ImageView = binding.root.findViewById(R.id.image_holder)
        private val fieldValueBox: LinearLayout = binding.root.findViewById(R.id.fields_box)

        private val textDate: TextView = binding.root.findViewById(R.id.text_date)
        private val vendor: TextView = binding.root.findViewById(R.id.text_vendor)
        private val rented: TextView = binding.root.findViewById(R.id.text_rented)
        private val price: TextView = binding.root.findViewById(R.id.tv_price)
        private val clickLayout: FrameLayout = binding.root.findViewById(R.id.click_layout)

//        private val progressImageMyThing: ProgressBar = binding.root.findViewById(R.id.progress_image_my_things)

        fun bind(myThingsFiltered: Thing, fragment: ThingsFragment) {
            image.loadThumb(myThingsFiltered.photos[0], null, null, image.layoutParams.width)
            val date = SimpleDateFormat("d MMM yyyy, HH:mm")
            //val date = DateFormat.getDateInstance(DateFormat.LONG)
            textDate.text = date.format(myThingsFiltered.createdAt)
            vendor.text = myThingsFiltered.vendor
            if (myThingsFiltered.rented) rented.visibility = VISIBLE else rented.visibility = INVISIBLE
            rented.setText(R.string.rented)

            initFields(TreeMap(myThingsFiltered.fields), fragment.context!!)

            clickLayout.onClick {
                click(myThingsFiltered)
            }
            price.text = getPriceText(fragment.context!!, myThingsFiltered, true)

            binding.thing = myThingsFiltered
            binding.executePendingBindings()
        }

        private fun initFields(fields: TreeMap<String, String>, context: Context) {
            fieldValueBox.removeAllViews()
            for ((index, key) in fields.keys.withIndex()) {

                if (index > 2) {
                    val tv = TextView(context)
                    tv.textSize = 12f
                    tv.text = "..."
                    fieldValueBox.addView(tv)
                    break
                }
                val value = fields[key]
                val line = ParametrLineView(context)
                line.fillParamsKey(key, value!!)
                fieldValueBox.addView(line)
            }
        }
    }

    private fun openThingDetail(thing: Thing) {
        onClickThingDetail(thing)
    }
}