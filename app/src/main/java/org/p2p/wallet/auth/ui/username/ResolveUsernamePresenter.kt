package org.p2p.wallet.auth.ui.username

import org.p2p.wallet.common.mvp.BasePresenter

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.interactor.UsernameInteractor
import retrofit2.HttpException

class ResolveUsernamePresenter(
    private val usernameInteractor: UsernameInteractor,
) :
    BasePresenter<ResolveUsernameContract.View>(), ResolveUsernameContract.Presenter {

    private var resolveUsernameJob: Job? = null
    override fun resolveUsername(name: String) {
        resolveUsernameJob?.cancel()
        resolveUsernameJob = launch {
            try {
                val result = usernameInteractor.resolveUsername(name)
                view?.showUsernameResult(result)
            } catch (e: HttpException) {
                view?.showErrorMessage(e)
            }
        }
    }
}