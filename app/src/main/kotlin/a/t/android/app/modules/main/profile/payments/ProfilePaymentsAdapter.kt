package io.tage.android.app.modules.main.profile.payments

import android.annotation.SuppressLint
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.stripe.android.model.Card
import com.stripe.android.model.Card.BRAND_RESOURCE_MAP
import com.stripe.android.view.CardNumberEditText
import com.stripe.android.view.ExpiryDateEditText
import io.tage.android.app.R
import io.tage.android.app.common.BankCard
import io.tage.android.app.common.gone
import io.tage.android.app.common.invisible
import io.tage.android.app.common.visible
import io.tage.android.app.databinding.RawProfilePaymentsItemsBinding
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.textColor


class ProfilePaymentsAdapter(val cardsList: ArrayList<BankCard> = ArrayList(), val fragment: ProfilePaymentsFragment) : RecyclerView.Adapter<ProfilePaymentsAdapter.PaymentsViewHolder>() {

    lateinit var onClickCardDetail: (BankCard) -> Unit
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentsViewHolder =
            PaymentsViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.raw_profile_payments_items, parent, false))
            { openCardDetail(it) }

    fun addData(data: Array<BankCard>) {
        cardsList.clear()
        cardsList.addAll(data)
        notifyDataSetChanged()
    }

    override fun getItemCount() = cardsList.size

    override fun onBindViewHolder(holder: PaymentsViewHolder, position: Int) = holder.bind(cardsList[position], fragment)


    class PaymentsViewHolder(val binding: RawProfilePaymentsItemsBinding, private val click: (BankCard) -> Unit) : RecyclerView.ViewHolder(binding.root) {

        private val textNumberCard = binding.root.findViewById<CardNumberEditText>(R.id.et_card_number)
        private val imageCardBrand = binding.root.findViewById<ImageView>(R.id.iv_card_icon)
        private val textMM_YY = binding.root.findViewById<ExpiryDateEditText>(R.id.et_expiry_date)
        private val imageDefault = binding.root.findViewById<ImageView>(R.id.image_is_default)
        private val layoutCard = binding.root.findViewById<LinearLayout>(R.id.layout_card)

        @SuppressLint("ClickableViewAccessibility")
        fun bind(cardsList: BankCard, fragment: ProfilePaymentsFragment) {
            textMM_YY.gone()
            textNumberCard.setText("**** **** **** " + cardsList.last4)
            textNumberCard.isEnabled = false
            textNumberCard.textColor = fragment.resources.getColor(R.color.colorPrimary)
            chooseIconBrand(cardsList, fragment)
            if (cardsList.isDefault) imageDefault.visible()
            else imageDefault.invisible()
            layoutCard.onClick {
                click(cardsList)
            }
            binding.card = cardsList
            binding.executePendingBindings()
        }

        private fun chooseIconBrand(card: BankCard, fragment: ProfilePaymentsFragment) {
            if (Card.UNKNOWN == card.brand) {
                val icon = fragment.resources.getDrawable(com.stripe.android.R.drawable.ic_unknown)
                imageCardBrand.setImageDrawable(icon)
                // applyTint(false)
            } else {
                imageCardBrand.setImageResource(BRAND_RESOURCE_MAP[card.brand]!!)
            }
        }
    }

    private fun openCardDetail(card: BankCard) {
        onClickCardDetail(card)
    }
}