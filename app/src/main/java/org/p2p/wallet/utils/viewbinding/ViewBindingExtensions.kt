package org.p2p.wallet.utils.viewbinding

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.viewbinding.ViewBinding
import timber.log.Timber
import java.lang.reflect.Method
import kotlin.reflect.KClass

val ViewBinding.context: Context
    get() = root.context

val ViewBinding.resources: Resources
    get() = context.resources

fun ViewBinding.getString(@StringRes stringId: Int, vararg formatArgs: Any): String =
    if (formatArgs.isNotEmpty()) {
        context.getString(stringId, *formatArgs)
    } else {
        context.getString(stringId)
    }

@ColorInt
fun ViewBinding.getColor(@ColorRes colorId: Int, theme: Resources.Theme? = context.theme): Int =
    ResourcesCompat.getColor(resources, colorId, theme)

fun ViewBinding.getDrawable(@DrawableRes id: Int, theme: Resources.Theme? = context.theme): Drawable? =
    ResourcesCompat.getDrawable(resources, id, theme)

operator fun ViewGroup.plusAssign(binding: ViewBinding) {
    addView(binding.root)
}

/**
 * Inflates ViewBinding with type [VB].
 *
 * **Important:** Views with root tag `merge` should always be attached to root, so better to
 * inflate them using this method.
 *
 * ```
 *  class ErrorView @JvmOverloads constructor(
 *      context: Context,
 *      attrs: AttributeSet? = null,
 *      defStyleAttr: Int = 0
 *  ) : LinearLayout(context, attrs, defStyleAttr) {
 *
 *      private val binding: ViewErrorBinding = inflateViewBinding()
 *  }
 * ```
 *
 * @param context The context used to obtain [LayoutInflater]. By default used `ViewGroup`'s context.
 * @param attachToRoot Whether the inflated hierarchy should be attached to the `ViewGroup`?
 *        If `false`, `ViewGroup` is only used to create the correct subclass of `LayoutParams`
 *        for the root view in the XML. By default is `true`.
 * @return Inflated ViewBinding.
 */
inline fun <reified VB : ViewBinding> ViewGroup.inflateViewBinding(
    context: Context = this.context,
    attachToRoot: Boolean = true,
): VB {
    return VB::class.inflate(LayoutInflater.from(context), this, attachToRoot)
}

/**
 * Inflates ViewBinding with type [VB].
 *
 * Use this method if you don't have non-null [ViewGroup] or [LayoutInflater].
 * ```
 *  fun createDetailsView(card: Card): MaterialTextView {
 *      return requireContext().inflateViewBinding<ViewCardDetailsBinding>().apply {
 *          name.text = card.name
 *      }
 *  }
 * ```
 *
 * @receiver The context used to obtain [LayoutInflater].
 * @param root If [attachToRoot] is `true` this view used to be parent of the generated hierarchy.
 *        Otherwise `root` used to provide a set of `LayoutParams` values for root of the returned
 *        hierarchy.
 * @param attachToRoot Whether the inflated hierarchy should be attached to the [root]?
 *        If `false`, `root` is only used to create the correct subclass of `LayoutParams`
 *        for the root view in the XML. By default is `true` if `root` is not null.
 * @return Inflated ViewBinding.
 */
inline fun <reified VB : ViewBinding> Context.inflateViewBinding(
    root: ViewGroup? = null,
    attachToRoot: Boolean = root != null,
): VB {
    return VB::class.inflate(LayoutInflater.from(this), root, attachToRoot)
}

/**
 * Inflates ViewBinding with type [VB] using given [LayoutInflater].
 *
 * ```
 *  class PickDateFragment : AppCompatDialogFragment() {
 *
 *      private lateinit val binding: DialogPickDateBinding
 *
 *      override fun onCreateView(
 *          inflater: LayoutInflater,
 *          container: ViewGroup?,
 *          savedInstanceState: Bundle?
 *      ): View? {
 *          binding = inflater.inflateViewBinding(container, attachToRoot = false)
 *          return binding.root
 *      }
 *  }
 * ```
 *
 * @param root If [attachToRoot] is `true` this view used to be parent of the generated hierarchy.
 *        Otherwise `root` used to provide a set of `LayoutParams` values for root of the returned
 *        hierarchy.
 * @param attachToRoot Whether the inflated hierarchy should be attached to the [root]?
 *        If `false`, `root` is only used to create the correct subclass of `LayoutParams`
 *        for the root view in the XML. By default is `true` if `root` is not null.
 * @return Inflated ViewBinding.
 */
inline fun <reified VB : ViewBinding> LayoutInflater.inflateViewBinding(
    root: ViewGroup? = null,
    attachToRoot: Boolean = root != null,
): VB {
    return VB::class.inflate(this, root, attachToRoot)
}

/**
 * Dynamically calls method `inflate` on ViewBinding class.
 * @see inflateViewBinding
 */
@PublishedApi
internal fun <VB : ViewBinding> KClass<VB>.inflate(
    inflater: LayoutInflater,
    root: ViewGroup?,
    attachToRoot: Boolean,
): VB {
    val inflateMethod = java.getInflateMethod()
    @Suppress("UNCHECKED_CAST")
    return if (inflateMethod.parameterTypes.size > 2) {
        inflateMethod.invoke(null, inflater, root, attachToRoot)
    } else {
        if (!attachToRoot) Timber.d("ViewBinding", "attachToRoot is always true for ${java.simpleName}.inflate")
        inflateMethod.invoke(null, inflater, root)
    } as VB
}

private val inflateMethodsCache = mutableMapOf<Class<out ViewBinding>, Method>()

private fun Class<out ViewBinding>.getInflateMethod(): Method {
    return inflateMethodsCache.getOrPut(this) {
        declaredMethods.find { method ->
            val parameterTypes = method.parameterTypes
            method.name == "inflate" &&
                parameterTypes[0] == LayoutInflater::class.java &&
                parameterTypes.getOrNull(1) == ViewGroup::class.java &&
                (parameterTypes.size == 2 || parameterTypes[2] == Boolean::class.javaPrimitiveType)
        } ?: error("Method ${this.simpleName}.inflate(LayoutInflater, ViewGroup[, boolean]) not found.")
    }
}

/**
 * Obtains ViewBinding with type [VB] from [View].
 *
 * ```
 *  class TransactionsItem : Item {
 *
 *      override val layoutId = R.layout.item_transaction
 *
 *      private lateinit var binding: ItemTransactionBinding
 *
 *      override fun bind(viewHolder: RecyclerView.ViewHolder) {
 *          binding = viewHolder.itemView.getBinding()
 *          with(binding) {
 *              // ...
 *          }
 *      }
 *  }
 * ```
 */
inline fun <reified VB : ViewBinding> View.getBinding(): VB = VB::class.bind(this)

/**
 * Dynamically calls method `bind` on ViewBinding class.
 * @see getBinding
 */
@PublishedApi
internal fun <VB : ViewBinding> KClass<VB>.bind(rootView: View): VB {
    val inflateMethod = java.getBindMethod()
    @Suppress("UNCHECKED_CAST")
    return inflateMethod.invoke(null, rootView) as VB
}

private val bindMethodsCache = mutableMapOf<Class<out ViewBinding>, Method>()

private fun Class<out ViewBinding>.getBindMethod(): Method {
    return bindMethodsCache.getOrPut(this) { getDeclaredMethod("bind", View::class.java) }
}
