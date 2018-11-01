package io.tage.android.app.modules.main.things.confirm


import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.navigation.fragment.findNavController
import io.reactivex.internal.disposables.DisposableContainer
import io.tage.android.app.R
import io.tage.android.app.common.*
import io.tage.android.app.modules.main.MainActivity
import kotlinx.android.synthetic.main.fragment_confirmation.*
import org.jetbrains.anko.sdk25.coroutines.onClick


class ConfirmationFragment : BaseFragment() {
    private val viewModel = ConfirmationFragmentViewModel()

    lateinit var adapterCardsSpinner: CardsSpinnerAdapter

    override val layout: Int
        get() = R.layout.fragment_confirmation
    override val title: Int
        get() = R.string.confirmation


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapterCardsSpinner = CardsSpinnerAdapter(context!!, arrayListOf())
        viewModel.rentalItem = arguments!!.getParcelable(THING_INSTANCE)
    }

    override fun setUi() {
        spinner_bank_cards.adapter = adapterCardsSpinner
        activity<MainActivity> {
            it.enableBackBtn(true)
            tv_terms_text.text = String.format(getString(R.string.text_confirmation), getPriceText(it, viewModel.rentalItem, false))
        }
    }

    override fun setObservers(disposableBag: DisposableContainer) {
        viewModel.setDisposableBag(disposableBag)
        checkbox_agree.onClick {
            if (checkbox_agree.isChecked) {
                button_confirm.isEnabled = true
                button_confirm.setTextColor(Color.WHITE)
            } else {
                button_confirm.setTextColor(Color.GRAY)
                button_confirm.isEnabled = false
            }
            // button_confirm.isEnabled = checkbox_agree.isChecked
        }
        button_cancel.onClick {
            activity<MainActivity> { it.onBackPressed() }
        }
        button_confirm.onClick { viewModel.confirmRent(viewModel.rentalItem.id) }
        disposableBag.add(viewModel.confirm.subscribe {
            when (it.state) {
                ProgressStateHelper.START -> {
                    activity<MainActivity> { it.globalProgress(true) }
                }
                ProgressStateHelper.SUCCESS -> {
                    activity<MainActivity> { it.globalProgress(false) }
                    val bundle = Bundle()
                    bundle.putString(RENT_ID, it.res!!)
                    findNavController().navigate(R.id.action_confirmationFragment_to_rentDetailFragment, bundle)
                }
                ProgressStateHelper.ERROR -> {
                    toast(getString(R.string.error))
                    activity<MainActivity> {
                        it.globalProgress(false)
                        it.onBackPressed()
                    }
                    //may invoke adding duplicate rent
//                    showErrorDialog(it.msg!!, { viewModel.confirmRent(viewModel.rentalItem.id) }, {})

                }
            }
        })

        disposableBag.add(
                viewModel.usersCard.subscribe {
                    when (it.state) {
                        ProgressStateHelper.START -> {
                            spinner_bank_cards.gone()
                            button_add_card.gone()
                            card_progress.visible()
                            button_confirm.isEnabled = false
                        }
                        ProgressStateHelper.SUCCESS -> {
                            if (it.card!!.isNotEmpty()) {
                                if (checkbox_agree.isChecked) button_confirm.isEnabled = true
                                else {
                                    button_confirm.isEnabled = false
                                    button_confirm.setTextColor(Color.GRAY)
                                }
                                spinner_bank_cards.visible()
                            } else {
                                button_add_card.visible()
                            }
                            card_progress.gone()
                            adapterCardsSpinner.addCards(it.card)
                            spinner_bank_cards.setSelection(0)
                        }
                        ProgressStateHelper.ERROR -> {
                            card_progress.gone()
                            showErrorDialog(it.msg!!, { viewModel.getUsersCards() }, {})
                        }

                    }
                }
        )
        spinner_bank_cards.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (adapterCardsSpinner.count > 1 && position + 1 == adapterCardsSpinner.count) {
                    findNavController().navigate(R.id.action_confirmationFragment_to_addBankCardFragment)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        button_add_card.onClick { findNavController().navigate(R.id.action_confirmationFragment_to_addBankCardFragment) }
    }

    override fun onStart() {
        super.onStart()
        viewModel.getUsersCards()
    }
}