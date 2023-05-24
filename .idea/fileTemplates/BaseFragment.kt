#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME};#end

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject

interface NewContract {
    interface View : MvpView
    interface Presenter : MvpPresenter<View>
}

class NewPresenter :
    BasePresenter<NewContract.View>(),
    NewContract.Presenter

class NewFragment :
    BaseMvpFragment<NewContract.View, NewContract.Presenter>(),
    NewContract.View {

    override val presenter: NewContract.Presenter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
    }
}