package io.tage.android.app.modules.main.things.my_thing


import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.navigation.fragment.findNavController
import io.reactivex.internal.disposables.DisposableContainer
import io.tage.android.app.R
import io.tage.android.app.common.*
import io.tage.android.app.common.ProgressStateHelper.*
import io.tage.android.app.modules.main.MainActivity
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_my_things_items.*


class ThingsFragment : BaseFragment() {
    val viewModel = MyThingsViewModel()

    override val layout: Int
        get() = R.layout.fragment_my_things_items
    override val title: Int
        get() = R.string.menu_my_things

    lateinit var myThingsAdapter: MyThingsAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        myThingsAdapter = MyThingsAdapter(arrayListOf(), this)
        myThingsAdapter.onClickThingDetail = {
            val bundle = Bundle()
            bundle.putParcelable(THING_INSTANCE, it)
            bundle.putBoolean(IS_MY_THING, true)
            findNavController().navigate(R.id.action_thingsFragment_to_detailsThings, bundle)
            hideKeyboard(view!!)
        }
    }

    override fun setUi() {
        activity<MainActivity> { it ->
            it.enableBackBtn(false)
            recycler_view_mythings.apply {
                layoutManager = LinearLayoutManager(context!!)
                adapter = myThingsAdapter
            }
        }
    }

    override fun setObservers(disposableBag: DisposableContainer) {
        disposableBag.add(
                viewModel.listMyThings.subscribe {
                    when (it.state) {
                        START -> {
                            list_is_empty.invisible()
                            activity<MainActivity> {
                                it.globalProgress(true)
                                it.toolbar.isEnabled = false
                            }
                        }
                        SUCCESS -> {
                            myThingsAdapter.addData(it.things!!)
                            myThingsAdapter.getFilter().filter("")
                            when (myThingsAdapter.thingsList.isEmpty()) {
                                true -> list_is_empty.visible()
                                else -> list_is_empty.invisible()
                            }
                            activity<MainActivity> { it.globalProgress(false) }
                        }
                        ERROR -> {
                            activity<MainActivity> { it.globalProgress(false) }
                            showErrorDialog(it.msg!!) { viewModel.getListMyThings(100, 0) }
                        }
                    }
                }
        )
        viewModel.getListMyThings(100, 0)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.add_new_things -> {
                findNavController().navigate(R.id.action_myThingsFragment_to_addNewThingFragment)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.toolbar_my_things, menu)
        val search = menu.findItem(R.id.search_things)
        val searchView = search.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(newText: String?): Boolean {
                myThingsAdapter.getFilter().filter(newText)
                return true
            }
        })
    }

}
