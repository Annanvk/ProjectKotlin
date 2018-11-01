package io.tage.android.app.modules.main.profile.payments.card_detail

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import io.reactivex.internal.disposables.DisposableContainer
import io.tage.android.app.R
import io.tage.android.app.common.*
import io.tage.android.app.common.ProgressStateHelper.*
import io.tage.android.app.modules.main.MainActivity
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_card_detail.*

class CardDetailFragment : BaseFragment() {
    override val layout: Int
        get() = R.layout.fragment_card_detail
    override val title: Int
        get() = R.string.text_payments

    val viewModel = StatusCardViewModel()
    var count: Int? = null
    lateinit var bankCard: BankCard

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        count = arguments?.getInt("cardList")
        bankCard = arguments?.getParcelable("bankCard")!!
    }

    override fun setUi() {
        card_number.text = "**** **** **** " + bankCard.last4
        expiry_date.text = "${bankCard.expMonth}/${bankCard.expYear}"
    }

    override fun setObservers(disposableBag: DisposableContainer) {
        disposableBag.add(
                viewModel.settingDefaultCard.subscribe {
                    when (it.state) {
                        START -> {
                            activity<MainActivity> { it.globalProgress(true, 0) }
                        }
                        SUCCESS -> {
                            activity<MainActivity> {
                                it.globalProgress(false)
                                it.toolbar.title = bankCard.brand + " " + getString(R.string.text_default)
                            }
                            toast("Card set by default")
                        }
                        ERROR -> {
                            activity<MainActivity> { it.globalProgress(false) }
                            showErrorDialog(it.msg!!, { viewModel.setDefaultCard(bankCard.id) }, {})
                        }
                    }
                }
        )

        disposableBag.add(
                viewModel.deletingCard.subscribe {
                    when (it.state) {
                        START -> {
                            activity<MainActivity> { it.globalProgress(true, 0) }
                        }
                        SUCCESS -> {
                            activity<MainActivity> {
                                it.onBackPressed()
                                it.globalProgress(false)
                            }
                        }

                        ERROR -> {
                            activity<MainActivity> { it.globalProgress(false) }
                            showErrorDialog(it.msg!!, { viewModel.deleteBankCard(bankCard.id) }, {})
                        }
                    }
                }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.toolbar_card_detail, menu)
        if (count!! > 1) {
            val showRemove = menu?.getItem(1)
            showRemove?.isVisible = true
        }
        if (!bankCard.isDefault) {
            val showSetDefault = menu?.getItem(0)
            showSetDefault?.isVisible = true
            activity<MainActivity> { it.toolbar.title = bankCard.brand }
        } else {
            activity<MainActivity> { it.toolbar.title = bankCard.brand + " " + getString(R.string.text_default) }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_set_default -> {
                viewModel.setDefaultCard(bankCard.id)
            }
            R.id.menu_remove -> {
                alert(getString(R.string.remove_card)) {
                    positiveButton(getString(R.string.yes)) { viewModel.deleteBankCard(bankCard.id) }
                    negativeButton(getString(R.string.no)) {}
                }?.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
