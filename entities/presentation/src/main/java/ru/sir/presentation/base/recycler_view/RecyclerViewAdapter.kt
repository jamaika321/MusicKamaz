package ru.sir.presentation.base.recycler_view

import android.util.SparseArray
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.flow.StateFlow
import ru.sir.presentation.extensions.launchWhenStarted
import java.util.*
import kotlin.collections.ArrayList

class RecyclerViewAdapter<D>(private val producers: SparseArray<ViewHolderProducer<in Any, *, *>>,
                             data: StateFlow<D>,
                             parent: Fragment,
                             transform: (D) -> List<RecyclerViewBaseDataModel>) : RecyclerView.Adapter<BaseViewHolder<in Any, *, *>>(){
    private var _data = mutableListOf<RecyclerViewBaseDataModel>()
    init {
        data.launchWhenStarted(parent.lifecycleScope) {
            _data.clear()
            _data.addAll(transform(it))
            notifyDataSetChanged()
        }
    }


    class Builder<VM : Fragment, D>(private val parent: VM, private val dataFlow: StateFlow<D>) {
        private val producers = SparseArray<ViewHolderProducer<*, *, *>>()

        fun <M : Any, I : RecyclerViewBaseItem<M, B>, B : ViewBinding> addProducer(producer: ViewHolderProducer<M, I, B>): Builder<VM, D> {
            producer.setParent(parent)
            producers.put(producer.getViewType(), producer)
            return this
        }

        @Suppress("UNCHECKED_CAST")
        fun build(transform: (D) -> List<RecyclerViewBaseDataModel>) = RecyclerViewAdapter(producers as SparseArray<ViewHolderProducer<in Any, *, *>>, dataFlow, parent, transform)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<in Any, *, *> {
        return producers[viewType]?.produce(parent)
            ?: throw IllegalStateException("View Holder Producer not found!")
    }

    override fun onBindViewHolder(holder: BaseViewHolder<in Any, *, *>, position: Int) {
        holder.bindData(_data[position].getData())
    }

    override fun getItemCount(): Int = _data.size

    override fun getItemViewType(position: Int): Int {
        return _data[position].getType()
    }
}