package com.nextfaze.devfun.menu

import android.content.DialogInterface
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper.getMainLooper
import android.support.annotation.DrawableRes
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatDialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearLayoutManager.VERTICAL
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.LayoutInflater.from
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.nextfaze.devfun.core.*
import com.nextfaze.devfun.internal.*
import kotlinx.android.synthetic.main.df_menu_dialog_fragment.*

internal class DeveloperMenuDialogFragment : AppCompatDialogFragment() {
    companion object {
        fun show(activity: FragmentActivity) =
                DeveloperMenuDialogFragment().let { it.show(activity.supportFragmentManager, it.defaultTag) }

        fun hide(activity: FragmentActivity) {
            (activity.supportFragmentManager.findFragmentByTag(DeveloperMenuDialogFragment::class.defaultTag) as? DialogFragment)?.dismiss()
        }
    }

    private val log = logger()
    private val handler = Handler(getMainLooper())

    private val categories by lazy { devFun.categories }
    private var categoryItems = emptyList<Any>()
    private var selectedCategoryIdx = -1

    @get:DrawableRes
    private val selectableItemBackground by lazy {
        val typedArray = dialog.context.obtainStyledAttributes(intArrayOf(R.attr.selectableItemBackground))
        val resourceId = typedArray.getResourceId(0, R.drawable.df_menu_item_background)
        typedArray.recycle()
        return@lazy resourceId
    }

    data class MenuHeaderItem(val title: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setStyle(STYLE_NO_TITLE, R.style.Theme_AppCompat_Dialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.df_menu_dialog_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // title
        titleTextView.text = activity::class.splitSimpleName

        // header items
        crashButton.setOnClickListener { throw DebugException() }

        // categories
        categoriesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity, VERTICAL, false)

            class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)
            adapter = object : RecyclerView.Adapter<ViewHolder>() {
                override fun onBindViewHolder(holder: ViewHolder, position: Int) =
                        with(holder.textView) {
                            val category = categories[position]
                            text = category.name
                            setOnClickListener { onCategoryClick(category, position) }
                            isSelected = selectedCategoryIdx == position
                            typeface = if (isSelected) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                            lollipop {
                                when {
                                    isSelected -> setBackgroundResource(R.drawable.df_menu_item_background)
                                    else -> setBackgroundResource(selectableItemBackground)
                                }
                            }
                            isClickable = !isSelected
                        }

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                        (from(parent.context).inflate(R.layout.df_menu_item, parent, false) as TextView).let(::ViewHolder)

                override fun getItemCount() = categories.size
            }
        }

        categoryItemsRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity, VERTICAL, false)

            class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)
            adapter = object : RecyclerView.Adapter<ViewHolder>() {
                override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                    val item = categoryItems[position]
                    when (item) {
                        is FunctionItem -> {
                            holder.textView.text = item.name
                            holder.textView.setOnClickListener { onCategoryItemClick(item) }
                        }
                        is MenuHeaderItem -> {
                            holder.textView.text = item.title
                            holder.textView.setOnClickListener(null)
                            holder.textView.isClickable = false
                        }
                    }
                }

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                        when (viewType) {
                            1 -> (from(parent.context).inflate(R.layout.df_menu_item, parent, false) as TextView).let(::ViewHolder)
                            else -> (from(parent.context).inflate(R.layout.df_menu_item_header, parent, false) as TextView).let(::ViewHolder)
                        }

                override fun getItemViewType(position: Int) =
                        when (categoryItems[position]) {
                            is FunctionItem -> 1
                            else -> 0
                        }

                override fun getItemCount() = categoryItems.size
            }
        }

        versionTextView.apply {
            text = getString(R.string.df_menu_version, BuildConfig.VERSION_NAME)
        }

        handler.post { categories.firstOrNull()?.let { onCategoryClick(it, 0) } }
    }

    private fun onCategoryClick(category: CategoryItem, index: Int) {
        if (selectedCategoryIdx == index) return

        val prevSelected = selectedCategoryIdx
        selectedCategoryIdx = index

        categoriesRecyclerView.adapter.notifyItemChanged(index)
        if (prevSelected >= 0) {
            categoriesRecyclerView.adapter.notifyItemChanged(prevSelected)
        }

        categoryItems = run generateCategoryItems@ {
            // if no groups, then just sort and return
            if (category.items.all { it.group.isNullOrBlank() }) {
                return@generateCategoryItems category.items.sortedBy { it.name }
            }

            // create item group headers
            val groups = category.items.groupBy { it.group }
                    .mapKeys { MenuHeaderItem(it.key ?: "Misc") }
                    .toSortedMap(compareBy<MenuHeaderItem> { it.title })

            ArrayList<Any>().apply {
                groups.forEach {
                    add(it.key)
                    addAll(it.value.sortedBy { it.name })
                }
            }
        }
        categoryItemsRecyclerView.adapter.notifyDataSetChanged()
    }

    private fun onCategoryItemClick(functionItem: FunctionItem) {
        functionItem.callAndLog(logger = log)
        dismiss()
    }

    override fun onStart() {
        devFun.devMenu.onShown()
        super.onStart()
    }

    override fun onDismiss(dialog: DialogInterface) {
        devFun.devMenu.onDismissed()
        super.onDismiss(dialog)
    }

    override fun onResume() {
        super.onResume()
        updateWindowLayoutParams()
    }

    override fun onDestroyView() {
        categoriesRecyclerView.adapter = null
        categoryItemsRecyclerView.adapter = null

        // Fix bug https://code.google.com/p/android/issues/detail?id=17423
        val dialog = dialog
        if (dialog != null && retainInstance) {
            dialog.setDismissMessage(null)
        }

        super.onDestroyView()
    }

    /** Update dialog window width/height (needs to be done in onResume) as it doesn't use the XML values. */
    private fun updateWindowLayoutParams() {
        view?.layoutParams?.also {
            dialog?.window?.setLayout(it.width, it.height)
            dialog?.window?.setBackgroundDrawableResource(R.drawable.df_menu_dialog_background)
        }
    }
}

private inline fun lollipop(body: () -> Any) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        body.invoke()
    }
}
