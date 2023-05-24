#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME};#end

import android.os.Bundle
import android.view.View

class NewNoOpBottomSheet : BaseMvpBottomSheet<MvpView, NoOpPresenter<MvpView>>() {

    override val presenter = NoOpPresenter<MvpView>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}