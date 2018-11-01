package io.tage.android.app.modules.main.history


import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuInflater
import androidx.navigation.fragment.findNavController
import io.reactivex.internal.disposables.DisposableContainer
import io.tage.android.app.R
import io.tage.android.app.common.*
import io.tage.android.app.common.ProgressStateHelper.*
import io.tage.android.app.modules.main.MainActivity
import kotlinx.android.synthetic.main.fragment_history.*

class HistoryFragment : BaseFragment() {
    override val layout: Int get() = R.layout.fragment_history
    override val title: Int get() = R.string.menu_history

    lateinit var historyAdapter: HistoryAdapter
    val viewModel = HistoryViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        historyAdapter = HistoryAdapter(arrayListOf(), context!!)
        historyAdapter.onClickHistoryDetail = { rentId, itemTitle, renterReview ->
            hideKeyboard(view!!)
            // findNavController().navigate(R.id.historyDetails)
            val bundle = Bundle()
            bundle.putString(RENT_ID, rentId)
            bundle.putString(ITEM_TITLE, itemTitle)
            bundle.putParcelable(ITEM_REVIEW, renterReview)
            findNavController().navigate(R.id.action_historyFragment_to_itemReviewFragment, bundle)
        }
    }

    override fun setUi() {
        recycler_view_history.apply {
            layoutManager = LinearLayoutManager(context!!)
            adapter = historyAdapter
        }
    }

    override fun setObservers(disposableBag: DisposableContainer) {
        disposableBag.add(
                viewModel.listHistory.subscribe {
                    when (it.state) {
                        START -> {
                            list_is_empty.invisible()
                            activity<MainActivity> { it.globalProgress(true) }
                        }
                        SUCCESS -> {
                            historyAdapter.addData(it.history!!)
                            historyAdapter.getFilter().filter("")
                            when (historyAdapter.historyList.isEmpty()) {
                                true -> list_is_empty.visible()
                                else -> list_is_empty.invisible()
                            }
                            activity<MainActivity> { it.globalProgress(false) }
                        }
                        ERROR -> {
                            activity<MainActivity> { it.globalProgress(false) }
                            showErrorDialog(it.msg!!) { viewModel.getListHistory(100, 0) }
                        }
                    }
                }
        )
        viewModel.getListHistory(100, 0)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.toolbar_history, menu)
        val search = menu.findItem(R.id.search_history)
        val searchView = search.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(newText: String?): Boolean {
                historyAdapter.getFilter().filter(newText)
                return true
            }
        })
    }
}
