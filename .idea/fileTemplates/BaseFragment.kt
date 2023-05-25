#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME};#end

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject

interface ${NAME}Contract {
    interface View : MvpView
    interface Presenter : MvpPresenter<View>
}

class ${NAME}Presenter :
    BasePresenter<${NAME}Contract.View>(),
    ${NAME}Contract.Presenter

class ${NAME}Fragment :
    BaseMvpFragment<${NAME}Contract.View, ${NAME}Contract.Presenter>(),
    ${NAME}Contract.View {

    override val presenter: ${NAME}Contract.Presenter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
    }
}