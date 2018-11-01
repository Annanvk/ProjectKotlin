package io.tage.android.app.modules.main.things.confirm

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import io.tage.android.app.R
import io.tage.android.app.R.layout.spinner_cards
import io.tage.android.app.common.BankCard
import kotlinx.android.synthetic.main.spinner_cards.view.*
import java.util.*
import kotlin.collections.ArrayList

class CardsSpinnerAdapter(var listCards: ArrayList<BankCard?> = ArrayList()) : BaseAdapter() {

    var context: Context? = null

    constructor(context: Context, listCards: ArrayList<BankCard?>) : this() {
        this.context = context
        this.listCards = listCards
    }

    @SuppressLint("ResourceType", "ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = (context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE)) as LayoutInflater
        val view = inflater.inflate(spinner_cards, parent, false)

        if (getItem(position) == null) view.text_card.setText(R.string.add_bank_card)
        else view.text_card.text = "**** **** **** " + getItem(position)?.last4
        return view
    }

    override fun getItem(position: Int) = listCards.get(position)

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = listCards.size

    fun addCards(data: Array<BankCard>) {
        listCards.clear()
        Collections.sort(listCards, compareBy {
            if (it!!.isDefault) 1
            else -1
        })
        listCards.addAll(data)
        listCards.add(null)
        notifyDataSetChanged()
    }
}