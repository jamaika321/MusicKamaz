package ru.kamaz.music.ui.fragmentDialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.kamaz.music.R
import ru.kamaz.music.databinding.DialogAddTrackBinding
import ru.kamaz.music.di.components.MusicComponent
import ru.kamaz.music.services.MusicService
import ru.kamaz.music.services.MusicServiceInterface
import ru.kamaz.music.ui.NavAction
import ru.kamaz.music.ui.producers.AddTrackViewHolder
import ru.kamaz.music.ui.producers.ItemType
import ru.kamaz.music_api.interactor.DeletePlayList
import ru.kamaz.music_api.interactor.InsertPlayList
import ru.kamaz.music_api.interactor.PlayListRV
import ru.kamaz.music_api.interactor.UpdatePlayList
import ru.kamaz.music_api.models.PlayListModel
import ru.sir.core.None
import ru.sir.presentation.base.BaseActivity
import ru.sir.presentation.base.BaseApplication
import ru.sir.presentation.base.recycler_view.RecyclerViewAdapter
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseDataModel
import ru.sir.presentation.extensions.launchWhenStarted
import javax.inject.Inject

class DialogAddTrack : DialogFragment(), ServiceConnection, MusicServiceInterface.ViewModel {

    private var _binding: DialogAddTrackBinding? = null
    private val binding get() = _binding!!
    lateinit var navigator: BaseActivity

    fun inject(app: BaseApplication) {
        app.getComponent<MusicComponent>().inject(this)
    }
    @Inject
    lateinit var insertPlayList: InsertPlayList

    @Inject
    lateinit var loadAllPlayList: PlayListRV

    @Inject
    lateinit var deletePlayList: DeletePlayList

    @Inject
    lateinit var updatePlayList: UpdatePlayList

    private val _service = MutableStateFlow<MusicServiceInterface.Service?>(null)
    val service = _service.asStateFlow()

    private val playList = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    private var notFLowList: ArrayList<PlayListModel> =
        arrayListOf(PlayListModel(0L, "", "",  arrayListOf("")))
    private var selectedPlayList = ""
    private val musicData: StateFlow<String> by lazy {
        service.value?.getMusicData() ?: MutableStateFlow("Unknown")
    }
    private val musicName: StateFlow<String> by lazy {
        service.value?.getMusicName() ?: MutableStateFlow("Unknown")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddTrackBinding.inflate(layoutInflater, null, false)
        val dialog = AlertDialog.Builder(context).setView(binding.root).create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        inject(activity?.application as BaseApplication)
        return dialog
    }

    private fun initVars() {
        val intent = Intent(context, MusicService::class.java)
        context?.bindService(intent, this, Context.BIND_AUTO_CREATE)
        initServiceVars()
        service.launchWhenStarted(lifecycleScope) {
            if (it == null) return@launchWhenStarted
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.80).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.80).toInt()
        dialog!!.window?.setLayout(width, height)
        loadPlayLists()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        testPlayList()
        initVars()
        binding.rvAllMusic.layoutManager = GridLayoutManager(context, 4)
        binding.rvAllMusic.adapter = recyclerViewAdapter(playList)
        setListeners()
        navigator = requireActivity() as BaseActivity
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun initServiceVars() {
        musicName.launchWhenStarted(lifecycleScope) {
            binding.tvMusicTitle.text = it
        }
    }

    private fun setListeners() {
        binding.addButtons.cancelBtn.setOnClickListener {
            onDestroyView()
        }
        binding.addButtons.addBtn.setOnClickListener {
            openAddDialog()
            onDestroyView()
        }
    }

    private fun loadPlayLists() {
        CoroutineScope(Dispatchers.IO).launch {
            loadAllPlayList.run(None()).collect {
                playList.value = it.toRecyclerViewItems()
                notFLowList = it as ArrayList<PlayListModel>
            }
        }
    }

    private fun testPlayList(){
        CoroutineScope(Dispatchers.IO).launch {
            insertPlayList.run(
                InsertPlayList.Params(
                PlayListModel(0L, context?.resources!!.getString(R.string.create_playlist), "create_playlist",  arrayListOf(""))
            ))
        }
    }

    private fun openAddDialog() {
        var coincidence = false
        notFLowList.forEach {
            if (it.title == selectedPlayList) {
                it.trackDataList.forEach { data ->
                    if (data == musicData.value){
                        coincidence = true
                        return@forEach
                    }
                }
                if (musicData.value != "" && !coincidence) {
                    it.trackDataList.add(musicData.value)
                    CoroutineScope(Dispatchers.IO).launch {
                        updatePlayList.run(
                            UpdatePlayList.Params(
                                selectedPlayList,
                                it.trackDataList
                            )
                        )
                    }
                    Toast.makeText(context, context?.resources!!.getString(R.string.music_added) +" "+ selectedPlayList, Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    fun selectPlayList(id: Long, title: String) {
        notFLowList.forEach {
            it.selection = it.id == id && it.title == title
        }
        playList.value = notFLowList.toRecyclerViewItems()
        binding.rvAllMusic.adapter = recyclerViewAdapter(playList)
        selectedPlayList = title
    }

    fun addNewPlaylist() {
        navigator.navigateTo(NavAction.OPEN_ADD_PLAY_LIST_DIALOG)
    }

    fun deletePlaylist(name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            deletePlayList.run(DeletePlayList.Params(name))
        }
    }

    private fun recyclerViewAdapter(
        items: StateFlow<List<RecyclerViewBaseDataModel>>
    ): RecyclerViewAdapter<List<RecyclerViewBaseDataModel>> {
        return RecyclerViewAdapter.Builder(this, items)
            .addProducer(AddTrackViewHolder())
            .build { it }

    }

    private fun List<PlayListModel>.toRecyclerViewItems(): List<RecyclerViewBaseDataModel> {
        val newList = mutableListOf<RecyclerViewBaseDataModel>()
        this.forEach { newList.add(RecyclerViewBaseDataModel(it, ItemType.RV_ITEM_MUSIC_PLAYLIST)) }
        return newList
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        _service.value = (service as MusicService.MyBinder).getService()
        this.service.value?.setViewModel(this)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        _service.value = null
    }
}