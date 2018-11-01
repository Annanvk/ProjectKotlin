package io.tage.android.app.modules.main.profile.payments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.navigation.fragment.findNavController
import io.reactivex.internal.disposables.DisposableContainer
import io.tage.android.app.R
import io.tage.android.app.common.*
import io.tage.android.app.common.ProgressStateHelper.*
import io.tage.android.app.modules.main.MainActivity
import kotlinx.android.synthetic.main.fragment_profile_payments.*


class ProfilePaymentsFragment : BaseFragment() {
    override val layout: Int
        get() = R.layout.fragment_profile_payments
    override val title: Int
        get() = R.string.text_payments

    val viewModel = ProfilePaymentsViewModel()
    lateinit var paymentsAdapter: ProfilePaymentsAdapter
    var count: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        paymentsAdapter = ProfilePaymentsAdapter(arrayListOf(), this)
        paymentsAdapter.onClickCardDetail = {
            val bundle = Bundle()
            bundle.putInt("cardList", count!!)
            bundle.putParcelable("bankCard", it)
            findNavController().navigate(R.id.action_profilePaymentsFragment_to_cardDetailFragment, bundle)
        }
    }

    override fun setUi() {
        activity<MainActivity> { it.enableBackBtn(true) }
        recycler_view.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = paymentsAdapter
        }
    }

    override fun setObservers(disposableBag: DisposableContainer) {
        disposableBag.add(
                viewModel.gettingUserBankCard.subscribe {
                    when (it.state) {
                        START -> {
                            activity<MainActivity> { it.globalProgress(true) }
                            list_is_empty.invisible()
                        }
                        SUCCESS -> {
                            activity<MainActivity> { it.globalProgress(false) }
                            paymentsAdapter.addData(it.card!!)
                            count = paymentsAdapter.itemCount
                            when (paymentsAdapter.cardsList.isEmpty()) {
                                true -> list_is_empty.visible()
                                else -> list_is_empty.invisible()
                            }
                        }
                        ERROR -> {
                            activity<MainActivity> { it.globalProgress(false) }
                            showErrorDialog(it.msg!!, { viewModel.getUserBankCard() }, {})
                        }
                    }
                }
        )
        viewModel.getUserBankCard()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.toolbar_payments, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.btn_add_card -> {
                findNavController().navigate(R.id.action_profilePaymentsFragment_to_addBankCardFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
