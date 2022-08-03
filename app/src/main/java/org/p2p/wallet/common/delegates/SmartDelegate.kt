package org.p2p.wallet.common.delegates

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

/**
 * Base delegate which is able to auto detect class which is used for checking in [suitFor] (by reflection with caching).
 * Also supports ViewBinding inside of [SmartDelegate.ViewHolder]
 * So you can override only [onBindViewHolder] method in children of this class.
 */
@DataTypeUndeclared
abstract class SmartDelegate<T, B : ViewBinding>(
    private val bindingCreator: (parent: ViewGroup) -> B
) : RecyclerViewAdapterDelegate<T, SmartDelegate.ViewHolder<B>>() {

    private val typeClass by lazy {
        var clazz: Class<*> = javaClass
        var index = 0
        while (
            clazz.superclass?.getAnnotation(DataTypeUndeclared::class.java)
                ?.also { index = it.typeParamIndex } == null
        ) {
            clazz = clazz.superclass ?: throw IllegalStateException("Parent generic class not found")
        }
        (clazz.genericSuperclass as ParameterizedType).actualTypeArguments[index].let {
            when (it) {
                is Class<*> -> it
                else -> throw IllegalStateException(
                    "Class $it must be abstract and annotated by @" +
                        DataTypeUndeclared::class.java.simpleName
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup) = ViewHolder(bindingCreator(parent))

    override fun onBindViewHolder(holder: ViewHolder<B>, data: T) {
        // do nothing, for static layout for example
    }

    @Suppress("UNCHECKED_CAST")
    override fun suitFor(position: Int, data: Any) = typeClass.isInstance(data) && suitFor(data as T)

    /**
     * Check that current delegate is suit for concrete item.
     * Called in [suitFor] after type checking.
     *
     * @param data item
     * @return true if delegate suit for `data`
     */
    protected open fun suitFor(data: T): Boolean = true

    override fun onViewRecycled(holder: ViewHolder<B>) = Unit

    open class ViewHolder<B : ViewBinding>(val binding: B) : RecyclerView.ViewHolder(binding.root)
}

/**
 * Mark class as child of SmartDelegate who have type parameter for data type.
 *
 * Use it if you have 2 or more delegates with same logic but different data type and you want to extract common logic
 * to base class.
 *
 * For example: you have delegates for `Audio` and `Video` cards. You can create `BaseDelegate<T>` and children:
 * `AudioDelegate: BaseDelegate<Audio>` and `VideoDelegate: BaseDelegate<Video>`. In this case `BaseDelegate<T>`
 * should be marked with [DataTypeUndeclared] annotation or override [RecyclerViewAdapterDelegate.suitFor] method.
 *
 * @param typeParamIndex index of data type parameter in class declaration. (Optional)
 */
@Target(AnnotationTarget.CLASS)
annotation class DataTypeUndeclared(val typeParamIndex: Int = 0)
