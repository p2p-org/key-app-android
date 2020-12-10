package com.p2p.wowlet.appbase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel


abstract class FragmentBaseMVVM<ViewModel : BaseViewModel, DataBinding : ViewDataBinding> :
    Fragment() {
    abstract val viewModel: ViewModel
    abstract val binding: DataBinding
    private lateinit var navControler: NavController
    private val navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this) {
            navigateUp()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        retainInstance = true
        initData()
        return binding.also { it.lifecycleOwner = viewLifecycleOwner }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navControler = Navigation.findNavController(view)
        observe(viewModel.command) { processViewCommandGeneral(it) }
        observes()
        initView()
    }

    protected open fun initView() {}
    protected open fun initData() {}
    protected open fun observes() {}

    protected fun <T> observe(liveData: LiveData<T>, action: (T) -> Unit) = view?.run {
        if (!this@FragmentBaseMVVM.isAdded) return@run
        liveData.observe(viewLifecycleOwner, Observer { action(it ?: return@Observer) })
    }

    private fun processViewCommandGeneral(command: ViewCommand) = when (command) {
        is Command.NetworkErrorViewCommand -> {

        }
        is Command.FinishAppViewCommand -> {
            activity?.finish()
        }
        else -> processViewCommand(command)
    }

    protected open fun processViewCommand(command: ViewCommand) {}
    protected open fun navigateUp() {}
    protected open fun navigateBackStack() {
        navControler.popBackStack()
    }
    protected fun navigateFragment(destinationId: Int, arg: Bundle? = null) {
        navControler.navigate(destinationId, arg, navOptions)
    }

}