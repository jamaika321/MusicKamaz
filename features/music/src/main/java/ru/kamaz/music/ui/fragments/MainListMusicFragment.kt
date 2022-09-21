package ru.kamaz.music.ui.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.StateFlow
import ru.kamaz.music.R
import ru.kamaz.music.databinding.FragmentMainListMusicBinding
import ru.kamaz.music.di.components.MusicComponent
import ru.kamaz.music.ui.NavAction
import ru.kamaz.music.ui.NavAction.OPEN_ADD_PLAY_LIST_DIALOG
import ru.kamaz.music.ui.NavAction.OPEN_MUSIC_FRAGMENT
import ru.kamaz.music.ui.producers.*
import ru.kamaz.music.ui.screenStates.MainListScreenState
import ru.kamaz.music.view_models.fragments.MainListMusicViewModel
import ru.kamaz.music_api.models.PlayListSource
import ru.kamaz.music_api.models.Track
import ru.sir.presentation.base.BaseApplication
import ru.sir.presentation.base.BaseFragment
import ru.sir.presentation.base.recycler_view.RecyclerViewAdapter
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseDataModel
import ru.sir.presentation.extensions.launchOn
import ru.sir.presentation.extensions.launchWhenStarted
import ru.sir.presentation.navigation.UiAction


class MainListMusicFragment
    :
    BaseFragment<MainListMusicViewModel, FragmentMainListMusicBinding>(MainListMusicViewModel::class.java) {
    override fun inject(app: BaseApplication) {
        app.getComponent<MusicComponent>().inject(this)
    }

    private var binDing : FragmentMainListMusicBinding? = null
    private val _binding get() = binDing!!

    private val layoutManager = LinearLayoutManager(context)

    private var mode = ListState.CATEGORY

    companion object {
        const val RV_ITEM_MUSIC_ARTIST = 1
        const val RV_ITEM_MUSIC_ALBUMS = 2
        const val RV_ITEM = 5
        const val RV_ITEM_MUSIC_CATEGORY = 6
        const val RV_ITEM_MUSIC_FOLDER = 7
        const val RV_ITEM_PLAYLIST = 8
        const val RV_ITEM_FOLDER_PLAYLIST = 9
    }

    override fun initVars() {
        viewModel.service.launchWhenStarted(lifecycleScope) {
            if (it == null) return@launchWhenStarted
            initServiceVars()
            viewModel.loadAllDBLists()
        }
//        categoryItemClicked(RV_ITEM)
    }

    override fun onBackPressed() {
        when (mode) {
            ListState.MAINPLAYLIST -> {
                backToPlayer()
            }
            ListState.FOLDER -> {
                backToPlayer()
            }
            ListState.CATEGORY -> {
                backToPlayer()
            }
            ListState.FOLDPLAYLIST -> {
                _binding.sourceSelection.folderMusic.performClick()
            }
            ListState.FAVORITEPLAYLIST, ListState.PLAYLISTMUSIC -> {
                _binding.sourceSelection.categoryMusic.performClick()
            }
            else -> {
                categoryItemClicked(RV_ITEM_MUSIC_CATEGORY)
            }
        }
        super.onBackPressed()
    }

    private fun backToPlayer() {
        navigator.navigateTo(
            UiAction(
                OPEN_MUSIC_FRAGMENT
            )
        )
    }

    enum class ListState(val value: Int) {
        MAINPLAYLIST(0),
        CATEGORY(1),
        CATPLAYLIST(2),
        FOLDER(3),
        FOLDPLAYLIST(4),
        PLAYLISTMUSIC(5),
        FAVORITEPLAYLIST(6),
        ARTISTPLAYLIST(7),
        ALBUMSPLAYLIST(8)
    }

    private fun initServiceVars() {
        viewModel.lastMusicChanged.launchOn(lifecycleScope) {
            viewModel.lastMusic(it, mode)
        }

        viewModel.allMusic.launchWhenStarted(lifecycleScope) {
            if (viewModel.mainListState.value == MainListScreenState.PLAYLIST) {
                _binding.rvAllMusic.layoutManager = LinearLayoutManager(context)
                _binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.allMusic, RV_ITEM)
            }
        }

        viewModel.categoryOfMusic.launchWhenStarted(lifecycleScope) {
            if (viewModel.mainListState.value == MainListScreenState.CATEGORY) {
                _binding.rvAllMusic.layoutManager = GridLayoutManager(context, 5)
                _binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.categoryOfMusic, RV_ITEM_MUSIC_CATEGORY)
            }
        }

        viewModel.folders.launchWhenStarted(lifecycleScope) {
            if (viewModel.mainListState.value == MainListScreenState.FOLDER) {
                _binding.rvAllMusic.layoutManager = GridLayoutManager(context, 5)
                _binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.folders, RV_ITEM_MUSIC_FOLDER)
            }
        }

        viewModel.serviceTracks.launchWhenStarted(lifecycleScope) {
            if (mode == ListState.MAINPLAYLIST) {
                viewModel.fillAllTracksList(it)
                viewModel.loadAllDBLists()
                _binding.rvAllMusic.scrollToPosition(viewModel.rvPosition.value)
            }
        }

        viewModel.usbConnected.launchWhenStarted(lifecycleScope) {
            if (mode == ListState.FOLDPLAYLIST) {
                Log.i("Test_Folder", " : onBackPressed ")
                viewModel.foldersList.value.find { it.dir == viewModel.activeFolderName.value }.let {
                    if (it == null) {
                        onBackPressed()
                        Log.i("Test_Folder", " : onBackPressed ")
                        categoryItemClicked(RV_ITEM_MUSIC_FOLDER)
                    }
                }
            }
        }

        _binding.rvAllMusic.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (layoutManager.findFirstVisibleItemPosition() == 0) {
                    searchViewVisibility(true)
                } else {
                    searchViewVisibility(false)
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binDing = null
    }

    override fun setListeners() {
        super.setListeners()
        _binding.sourceSelection.listMusic.setOnClickListener {
            viewModel.changeScreenState(MainListScreenState.PLAYLIST)
        }
        _binding.sourceSelection.folderMusic.setOnClickListener {
            viewModel.changeScreenState(MainListScreenState.FOLDER)
        }
        _binding.sourceSelection.categoryMusic.setOnClickListener {
            viewModel.changeScreenState(MainListScreenState.CATEGORY)
        }
        _binding.search.setOnClickListener {
            _binding.search.isIconified = false
            searchActive()
        }

        viewModel.mainListState.launchWhenStarted(lifecycleScope) {
            searchViewVisibility(false)
            when (it) {
                MainListScreenState.CATEGORY -> {
                    viewModel.onCategoryLoaded(viewModel.loadCategoryItems())
                }
                MainListScreenState.PLAYLIST -> {
                    viewModel.fillAllTracksList(viewModel.serviceTracks.value)
                    searchViewVisibility(true)
                }
                MainListScreenState.FOLDER -> {
                    viewModel.fillFolderList()
                }
            }
        }
    }

    private fun recyclerViewAdapter(
        items: StateFlow<List<RecyclerViewBaseDataModel>>,
        id: Int
    ): RecyclerViewAdapter<List<RecyclerViewBaseDataModel>> {
        return when (id) {
            1 -> {//Artist
                RecyclerViewAdapter.Builder(this, items)
                    .addProducer(MusicArtistViewHolder())
                    .build { it }
            }
            2 -> {//Albums
                RecyclerViewAdapter.Builder(this, items)
                    .addProducer(MusicAlbumsViewHolder())
                    .build { it }
            }
            3 -> {//Playlists
                RecyclerViewAdapter.Builder(this, items)
                    .addProducer(AddTrackViewHolder())
                    .build { it }
            }
            6 -> {//CATEGORY
                RecyclerViewAdapter.Builder(this, items)
                    .addProducer(MusicCategoryViewHolder())
                    .build { it }
            }
            7 -> {//FOLDERS
                RecyclerViewAdapter.Builder(this, items)
                    .addProducer(MusicFoldersViewHolder())
                    .build { it }
            }
            else -> {
                RecyclerViewAdapter.Builder(this, items)
                    .addProducer(MusicListViewHolderProducer())
                    .build { it }
            }
        }
    }

    private fun searchActive() {
        val searchView = _binding.search
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(musicText: String): Boolean {
                viewModel.searchMusic(musicText)
                return false
            }
        }
        )
    }

    fun addNewPlaylist() {
        navigator.navigateTo(OPEN_ADD_PLAY_LIST_DIALOG)
    }

    fun onTrackClicked(track: Track, position: Int) {
        viewModel.rvPosition.value = position
        when (mode) {
            ListState.MAINPLAYLIST -> {
                viewModel.onItemClick(track, track.data, PlayListSource("all", "all"))
            }
            ListState.PLAYLISTMUSIC -> {
                viewModel.onItemClick(
                    track,
                    track.data,
                    PlayListSource("playList", viewModel.activePlayListName.value)
                )
            }
            ListState.FOLDPLAYLIST -> {
                viewModel.onItemClick(
                    track,
                    track.data,
                    PlayListSource("folder", viewModel.activeFolderName.value)
                )
            }
            ListState.FAVORITEPLAYLIST -> {
                viewModel.onItemClick(track, track.data, PlayListSource("favorite", "Избранное"))
            }
            ListState.ARTISTPLAYLIST -> {
                viewModel.onItemClick(track, track.data, PlayListSource("artists", track.artist))
            }
            ListState.ALBUMSPLAYLIST -> {
                viewModel.onItemClick(track, track.data, PlayListSource("albums", track.album))
            }
        }
    }

    fun openArtistPlayList(name: String){
        this.mode = ListState.ARTISTPLAYLIST
        viewModel.fillArtistsPlayList(name)
        viewModel.activeArtistName.value = name
    }

    fun openAlbumsPlayList(name: String){
        this.mode = ListState.ALBUMSPLAYLIST
        viewModel.fillAlbumsPlayList(name)
        viewModel.activeAlbumName.value = name
    }

    fun onLikeClicked(track: Track) {
        viewModel.onLikeClicked(track)
    }

    fun onOptionsItemClicked( track: Track) {
        showOptionDialog(track)
    }

    override fun onStop() {
        super.onStop()
        _binding.rvAllMusic.layoutManager = null
    }

    fun categoryItemClicked(id: Int) {
        when (id) {
            0 -> {//AllTracks
                viewModel.fillAllTracksList(viewModel.serviceTracks.value)
            }
            1 -> {//Artist
                viewModel.fillCategoryList(id)
            }
            2 -> {//Albums
                viewModel.fillCategoryList(id)
            }
            3 -> {//Playlists
                viewModel.fillCategoryList(id)
            }
            4 -> {//Favorite
                _binding.rvAllMusic.layoutManager = layoutManager
                viewModel.fillAllTracksList(viewModel.getFavoriteTracks())
                this.mode = ListState.FAVORITEPLAYLIST
            }
            5 -> {//DISK

            }
            6 -> {//USB

            }
        }
    }

    private fun searchViewVisibility(visible: Boolean) {
        if (visible) {
            _binding.search.visibility = View.VISIBLE
            _binding.rvAllMusic.setPadding(0, 60, 0, 0)
        } else {
            _binding.search.visibility = View.GONE
            _binding.rvAllMusic.setPadding(0, 30, 0, 0)
        }

    }

    fun playListSelected(name: String) {
        viewModel.activePlayListName.value = name
        viewModel.getPlayListMusic()
        categoryItemClicked(RV_ITEM_PLAYLIST)
    }


//    fun getCategoryLists(category: String, playlist: List<Track>): List<Track> {
//        val categoryList: MutableList<Track> = mutableListOf()
//        playlist.forEach {
//            if (it.artist == category && !categoryList.contains(it.artist)) {
//                categoryList.add(it)
//            }
//        }
//        return categoryList
//    }

    fun onFolderClicked(data: String, name: String) {
        viewModel.fillFolderPlaylist(data)
        viewModel.activeFolderName.value = name
        categoryItemClicked(RV_ITEM_FOLDER_PLAYLIST)
    }

    fun deletePlayList(name: String) {
        viewModel.deletePlaylist(name)
    }

    fun playListItemLongClickListener(name: String) {
        val dialog = Dialog(requireContext())
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.context_menu_alert)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)

        val titleText: TextView = dialog.findViewById(R.id.tv_question_bt_connection)
        val close: TextView = dialog.findViewById(R.id.btn_close)
        val add: TextView = dialog.findViewById(R.id.btn_add_to_playlist)
        titleText.text = name
        close.text = getString(R.string.rename)
        add.text = getString(R.string.delete)
        add.setOnClickListener {
            showDeleteAlertDialog(name)
            dialog.dismiss()
        }
        close.setOnClickListener {
            showRenameAlertDialog(name)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showRenameAlertDialog(name: String) {
        val dialog = Dialog(requireContext())
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.playlist_rename_alert_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)

        val titleText: TextView = dialog.findViewById(R.id.tv_question_bt_connection)
        val input: EditText = dialog.findViewById(R.id.et_add_play_list)
        val close: TextView = dialog.findViewById(R.id.btn_close)
        val add: TextView = dialog.findViewById(R.id.btn_add_to_playlist)
        titleText.text = getString(R.string.rename)
        close.text = getString(R.string.cancel_add)
        add.text = getString(R.string.create)
        add.setOnClickListener {
            viewModel.renamePlayList(name, input.text.toString())
            dialog.dismiss()
        }
        close.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showDeleteAlertDialog(name: String) {
        val dialog = Dialog(requireContext())
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.playlist_delete_alert_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)

        val playlistName: TextView = dialog.findViewById(R.id.tv_question_bt_connection)
        val deleteText: TextView = dialog.findViewById(R.id.delete_text)
        val close: TextView = dialog.findViewById(R.id.btn_close)
        val add: TextView = dialog.findViewById(R.id.btn_add_to_playlist)

        playlistName.text = name
        deleteText.text = getString(R.string.delete_text)
        close.text = getString(R.string.no)
        add.text = getString(R.string.yes)
        add.setOnClickListener {
            viewModel.deletePlaylist(name)
            dialog.dismiss()
        }
        close.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showOptionDialog(track: Track) {
        val dialog = Dialog(requireContext())
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.track_option_alert)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)

        val trackName: TextView = dialog.findViewById(R.id.tv_question_bt_connection)
        val addToPlaylist: TextView = dialog.findViewById(R.id.btn_close)
        val delete: TextView = dialog.findViewById(R.id.btn_add_to_playlist)

        trackName.text = track.title
        delete.text = getString(R.string.delete)
        addToPlaylist.text = getString(R.string.add_to_playlist)
        addToPlaylist.setOnClickListener {
            navigator.navigateTo(
                UiAction(
                    NavAction.OPEN_DIALOG_ADD_TRACK
                )
            )
            dialog.dismiss()
        }
        delete.setOnClickListener {
            deleteTrack(track)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun deleteTrack(track: Track){
        when (mode) {
            ListState.PLAYLISTMUSIC -> {
                showPlayListTrackDeleteAlertDialog(track)
            }
            else -> {
                showFileDeleteAlertDialog(track)
            }
        }
    }

    private fun showPlayListTrackDeleteAlertDialog(data: Track){
        val dialog = Dialog(requireContext())
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.playlist_delete_alert_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)

        val playlistName: TextView = dialog.findViewById(R.id.tv_question_bt_connection)
        val deleteText: TextView = dialog.findViewById(R.id.delete_text)
        val close: TextView = dialog.findViewById(R.id.btn_close)
        val add: TextView = dialog.findViewById(R.id.btn_add_to_playlist)

        playlistName.text = data.title
        deleteText.text = getString(R.string.delete_from_playlist) + " ${viewModel.activePlayListName.value}?"
        close.text = getString(R.string.no)
        add.text = getString(R.string.yes)
        add.setOnClickListener {
            viewModel.playLists.value.find { it.title == viewModel.activePlayListName.value }.let {
                it?.trackDataList?.forEach { playlist ->
                    if (playlist == data.data){
                        it.trackDataList.remove(playlist)
                    }
                }
            }
            dialog.dismiss()
        }
        close.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showFileDeleteAlertDialog(data: Track) {
        val dialog = Dialog(requireContext())
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.playlist_delete_alert_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)

        val playlistName: TextView = dialog.findViewById(R.id.tv_question_bt_connection)
        val deleteText: TextView = dialog.findViewById(R.id.delete_text)
        val close: TextView = dialog.findViewById(R.id.btn_close)
        val add: TextView = dialog.findViewById(R.id.btn_add_to_playlist)

        playlistName.text = data.title
        deleteText.text = getString(R.string.file_delete)
        close.text = getString(R.string.no)
        add.text = getString(R.string.yes)
        add.setOnClickListener {
            viewModel.deleteTrackFromMemory(data.data)
            dialog.dismiss()
        }
        close.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentMainListMusicBinding {
        binDing = FragmentMainListMusicBinding.inflate(inflater, container, false)
        return _binding
    }


}



