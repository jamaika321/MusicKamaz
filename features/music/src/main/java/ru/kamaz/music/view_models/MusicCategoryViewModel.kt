package ru.kamaz.music.view_models

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.kamaz.music.databinding.MainCategoryItemBinding
import ru.kamaz.music.services.MusicService
import ru.kamaz.music.services.MusicServiceInterface
import ru.kamaz.music.ui.category.CategoryFragment
import ru.kamaz.music_api.models.CategoryMusicModel
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseItem
import ru.sir.presentation.extensions.launchWhenStarted

class MusicCategoryViewModel :RecyclerViewBaseItem<CategoryMusicModel, MainCategoryItemBinding>(), MusicServiceInterface.ViewModel,
    ServiceConnection {
    private val img = MutableStateFlow(0)
    private val category = MutableStateFlow("")
    private lateinit var data: CategoryMusicModel
    private lateinit var context: Context
    private val _service = MutableStateFlow<MusicServiceInterface.Service?>(null)
    val service = _service.asStateFlow()

    override fun initVars() {
        img.launchWhenStarted(parent.lifecycleScope){
            binding.imageCategory.setImageResource(it)
        }
        category.launchWhenStarted(parent.lifecycleScope){
            binding.textCategory.text = it
        }
        binding.clAllItem.setOnClickListener {
            data?.let { (parent as CategoryFragment).clickListener(it.id) }
        }

    }

    override fun bindData(data: CategoryMusicModel) {
        this.data = data
        img.value= data.img
        category.value= data.category

    }

    override fun addListener() {
        TODO("Not yet implemented")
    }

    override fun removeListener() {
        TODO("Not yet implemented")
    }

    override fun onCheckPosition(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onUpdateSeekBar(duration: Int) {
        TODO("Not yet implemented")
    }

    override fun selectBtMode() {
        TODO("Not yet implemented")
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        Log.d("testPlayTrack", "onServiceConnected")
        _service.value = (service as MusicService.MyBinder).getService()
        this.service.value?.setViewModel(this)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        _service.value = null
    }
}