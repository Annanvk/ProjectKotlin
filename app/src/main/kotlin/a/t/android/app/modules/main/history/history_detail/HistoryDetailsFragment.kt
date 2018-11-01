package io.tage.android.app.modules.main.history.history_detail


import android.support.v7.app.AppCompatActivity
import io.reactivex.internal.disposables.DisposableContainer
import io.tage.android.app.R
import io.tage.android.app.common.BaseFragment
import io.tage.android.app.common.activity
import kotlinx.android.synthetic.main.app_bar_main.*

class HistoryDetailsFragment : BaseFragment() {
    override val layout: Int
        get() = R.layout.fragment_history_details
    override val title: Int
        get() = R.string.history_details

    override fun setUi() {
        activity<AppCompatActivity> {
            it.toolbar.setNavigationOnClickListener {
                activity<AppCompatActivity> {
                    it.onBackPressed()
                }
            }
        }
    }

    override fun setObservers(disposableBag: DisposableContainer) {
    }


}
