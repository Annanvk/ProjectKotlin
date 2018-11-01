package io.tage.android.app.modules.main.history

import android.annotation.SuppressLint
import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import io.tage.android.app.R
import io.tage.android.app.common.*
import io.tage.android.app.databinding.HistoryBinding
import io.tage.android.app.services.Services.userId
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk25.coroutines.onClick

class HistoryAdapter(val historyList: ArrayList<Rent> = ArrayList(), val context: Context) : RecyclerView.Adapter<HistoryAdapter.HistoryHolder>(), Filterable {

    var filteredHistoryList: ArrayList<Rent>? = null

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = Filter.FilterResults()
                if (constraint == null || constraint.isEmpty()) {
                    filteredHistoryList = ArrayList(historyList.size)
                    filteredHistoryList!!.addAll(historyList)
                    results.values = historyList
                    results.count = historyList.size
                } else {
                    val txt = constraint.toString().toLowerCase()
                    val filt = historyList.filter {
                        it.thingTitle.toLowerCase().contains(txt) or it.thingVendor.toLowerCase().contains(txt)
                    }
                    filteredHistoryList = ArrayList(filt.size)
                    filteredHistoryList!!.addAll(filt)
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

    lateinit var onClickHistoryDetail: (rentId: String, itemTitle: String, renterReview: ItemReview?) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryHolder =
            HistoryHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.history, parent, false))
            { onClickHistoryDetail(it.id, it.thingTitle, it.renterReview) }

    fun addData(data: Array<Rent>) {
        historyList.clear()
        historyList.addAll(data)
        notifyDataSetChanged()
    }

    override fun getItemCount() = filteredHistoryList?.size ?: 0

    override fun onBindViewHolder(holder: HistoryHolder, position: Int) = holder.bind(filteredHistoryList!![position], context)

    inner class HistoryHolder(val binding: HistoryBinding, private val click: (Rent) -> Unit) : RecyclerView.ViewHolder(binding.root) {

        private val image: ImageView = binding.root.findViewById(R.id.image_holder)
        private val info: TextView = binding.root.findViewById(R.id.info)
        private val price: TextView = binding.root.findViewById(R.id.tv_price)
        private val right_divider: View = binding.root.findViewById(R.id.right_divider)
        private val arrowImage: ImageView = binding.root.findViewById(R.id.iv_arrow)

        @SuppressLint("StringFormatMatches", "StringFormatInvalid")
        fun bind(rent: Rent, context: Context) {

            if (userId != null) {
                if (userId == rent.holderId) {
                    arrowImage.imageResource = R.drawable.out_arrow
                    right_divider.backgroundColor = context.color(R.color.color_price_label)
                    binding.root.onClick { }
                } else {
                    arrowImage.imageResource = R.drawable.in_arrow
                    right_divider.backgroundColor = context.color(R.color.colorAccent)
                    binding.root.onClick {
                        click(rent)
                    }
                }
            }
            info.text = context.getString(R.string.from, "", sdf.format(rent.startRent)) + "\n" +
                    context.getString(R.string.to, "", sdf.format(rent.endRent))
            image.loadThumb(rent.thingPhoto, null, null, image.layoutParams.width)
//            binding.root.setOnClickListener { itemClick(rent.id) }
            price.text = getPriceText(context, rent, true)

            binding.rent = rent
            binding.executePendingBindings()
        }
    }
}