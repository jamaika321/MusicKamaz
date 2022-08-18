package ru.kamaz.music.ui.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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

    private var mode = ListState.CATEGORY

    companion object {
        const val RV_ITEM_MUSIC_ARTIST = 0
        const val RV_ITEM_MUSIC_ALBUMS = 2
        const val RV_ITEM_MUSIC_PLAYLIST = 3
        const val RV_ITEM_MUSIC_FAVORITE = 4
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
        }
        categoryItemClicked(RV_ITEM)
    }

    override fun onBackPressed() {
        when (mode) {
            ListState.PLAYLIST -> {
                backToPlayer()
            }
            ListState.FOLDER -> {
                backToPlayer()
            }
            ListState.CATEGORY -> {
                backToPlayer()
            }
            ListState.FOLDPLAYLIST -> {
                categoryItemClicked(RV_ITEM_MUSIC_FOLDER)
            }
            ListState.PLAYLISTMUSIC -> {
                categoryItemClicked(RV_ITEM_MUSIC_PLAYLIST)
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
        PLAYLIST(0),
        CATEGORY(1),
        CATPLAYLIST(2),
        FOLDER(3),
        FOLDPLAYLIST(4),
        PLAYLISTMUSIC(5),
        CATFAVORITES(6)
    }

    private fun initServiceVars() {
        viewModel.lastMusicChanged.launchOn(lifecycleScope) {
            viewModel.lastMusic(it, mode)
        }

        viewModel.allMusic.launchWhenStarted(lifecycleScope) {
            if (mode == ListState.PLAYLIST) categoryItemClicked(RV_ITEM)
        }

        viewModel.serviceTracks.launchWhenStarted(lifecycleScope) {
            viewModel.fillAllTracksList()
            viewModel.loadAllDBLists()
        }

        viewModel.playLists.launchWhenStarted(lifecycleScope) {
            viewModel.getPlayLists()
            if (mode == ListState.CATPLAYLIST) categoryItemClicked(RV_ITEM_MUSIC_PLAYLIST)
        }

        viewModel.foldersList.launchWhenStarted(lifecycleScope) {
            viewModel.getFoldersList()
            if (mode == ListState.FOLDER) {
                categoryItemClicked(RV_ITEM_MUSIC_FOLDER)
            }
        }

        viewModel.favoriteSongs.launchWhenStarted(lifecycleScope) {
            if (mode == ListState.CATFAVORITES) categoryItemClicked(RV_ITEM_MUSIC_FAVORITE)
        }

        viewModel.folderMusicPlaylist.launchWhenStarted(lifecycleScope) {
            if (mode == ListState.FOLDPLAYLIST) categoryItemClicked(RV_ITEM_FOLDER_PLAYLIST)
        }

        viewModel.usbConnected.launchWhenStarted(lifecycleScope) {
            if (mode == ListState.FOLDPLAYLIST) {
                viewModel.foldersList.value.find { it.dir == viewModel.activeFolderName.value }.let {
                    if (it == null) onBackPressed()
                }
            }
        }

        binding.rvAllMusic.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                viewModel.rvScrollState.value += dy
                if (viewModel.rvScrollState.value > 5) {
                    binding.search.visibility = View.INVISIBLE
                } else {
                    binding.search.visibility = View.VISIBLE
                }
            }
        })
    }


    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentMainListMusicBinding.inflate(inflater, container, false)

    override fun setListeners() {
        binding.sourceSelection.listMusic.setOnClickListener {
            categoryItemClicked(RV_ITEM)
        }
        binding.sourceSelection.folderMusic.setOnClickListener {
            categoryItemClicked(RV_ITEM_MUSIC_FOLDER)
        }
        binding.sourceSelection.categoryMusic.setOnClickListener {
            categoryItemClicked(RV_ITEM_MUSIC_CATEGORY)
        }
        binding.search.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                binding.search.setIconified(false)
                searchActive()
            }
        })
        super.setListeners()
    }

    private fun recyclerViewAdapter(
        items: StateFlow<List<RecyclerViewBaseDataModel>>,
        id: Int
    ): RecyclerViewAdapter<List<RecyclerViewBaseDataModel>> {
        return when (id) {
            0 -> {//Artist
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

    override fun onResume() {
        super.onResume()
        (view as ViewGroup).removeAllViews()
    }

    override fun onPause() {
        super.onPause()
        (view as ViewGroup).removeAllViews()

    }

    private fun searchActive() {
        val searchView = binding.search
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
            ListState.PLAYLIST -> {
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
            ListState.CATFAVORITES -> {
                viewModel.onItemClick(track, track.data, PlayListSource("favorite", "Избранное"))
            }
        }
    }

    fun onLikeClicked(track: Track) {
        viewModel.onLikeClicked(track)
    }

    fun onOptionsItemClicked(position: Int, track: Track) {
        showOptionDialog(track)
    }

    override fun onStop() {
        super.onStop()
        binding.rvAllMusic.layoutManager = null
    }

    fun categoryItemClicked(id: Int) {
        when (id) {
            0 -> {//Artist
                viewModel.getCategoryList(id)
                binding.rvAllMusic.layoutManager = GridLayoutManager(context, 5)
                binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.categoryList, id)
                this.mode = ListState.CATPLAYLIST
            }
            2 -> {//Albums
                viewModel.getCategoryList(id)
                binding.rvAllMusic.layoutManager = GridLayoutManager(context, 5)
                binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.categoryList, id)
                this.mode = ListState.CATPLAYLIST
            }
            3 -> {//Playlists
                binding.rvAllMusic.layoutManager = GridLayoutManager(context, 5)
                binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.listPlayList, id)
                this.mode = ListState.CATPLAYLIST
            }
            4 -> {//Favorite
                binding.rvAllMusic.layoutManager = LinearLayoutManager(context)
                binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.favoriteSongs, id)
                binding.rvAllMusic.scrollToPosition(viewModel.rvPosition.value)
                this.mode = ListState.CATFAVORITES
            }
            5 -> {//RV_ITEM
                binding.rvAllMusic.layoutManager = LinearLayoutManager(context)
                binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.allMusic, id)
                binding.rvAllMusic.scrollToPosition(viewModel.rvPosition.value)
                this.mode = ListState.PLAYLIST
            }
            6 -> {//MusicCategory
                binding.rvAllMusic.layoutManager = GridLayoutManager(context, 5)
                binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.categoryOfMusic, id)
                this.mode = ListState.CATEGORY
            }
            7 -> {//Folders
                binding.rvAllMusic.layoutManager = GridLayoutManager(context, 5)
                binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.foldersLists, id)
                this.mode = ListState.FOLDER
            }
            8 -> {//PlaylistMusic
                binding.rvAllMusic.layoutManager = LinearLayoutManager(context)
                binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.playListMusic, id)
                binding.rvAllMusic.scrollToPosition(viewModel.rvPosition.value)
                this.mode = ListState.PLAYLISTMUSIC
            }
            9 -> {//FolderPlayLists
                binding.rvAllMusic.layoutManager = LinearLayoutManager(context)
                binding.rvAllMusic.adapter = recyclerViewAdapter(viewModel.folderMusicPlaylist, id)
                binding.rvAllMusic.scrollToPosition(viewModel.rvPosition.value)
                this.mode = ListState.FOLDPLAYLIST
            }
        }
        searchViewVisibility()
    }

    private fun searchViewVisibility() {
        if (mode == ListState.PLAYLIST /*|| mode == ListState.PLAYLISTMUSIC || mode == ListState.FOLDPLAYLIST*/) {
            binding.search.visibility = View.VISIBLE
            binding.rvAllMusic.setPadding(0, 60, 0, 0)
        } else {
            viewModel.rvScrollState.value = 0
            binding.search.visibility = View.INVISIBLE
            binding.rvAllMusic.setPadding(0, 30, 0, 0)
        }

    }

    fun playListSelected(name: String) {
        viewModel.activePlayListName.value = name
        viewModel.getPlayListMusic()
        categoryItemClicked(RV_ITEM_PLAYLIST)
    }


    fun getCategoryLists(category: String, playlist: List<Track>): List<Track> {
        val categoryList: MutableList<Track> = mutableListOf()
        playlist.forEach {
            if (it.artist == category && !categoryList.contains(it.artist)) {
                categoryList.add(it)
            }
        }
        return categoryList
    }

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
                        it?.trackDataList.remove(playlist)
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


}



