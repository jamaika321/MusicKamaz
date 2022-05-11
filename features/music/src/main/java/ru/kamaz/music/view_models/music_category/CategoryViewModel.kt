package ru.kamaz.music.view_models.music_category

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.kamaz.music_api.interactor.*
import ru.kamaz.music_api.models.*
import ru.sir.core.None
import ru.sir.presentation.base.BaseViewModel
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseDataModel
import ru.sir.presentation.extensions.launchOn
import javax.inject.Inject

class CategoryViewModel @Inject constructor(
    application: Application,
    private val categoryData: CategoryLoadRV,
    private val favoriteData: FavoriteMusicRV,
    private val rvAllFolderWithMusic: AllFolderWithMusicRV,
    private val artistList: ArtistLoadRV,
    private val playList: PlayListRV
) : BaseViewModel(application) {

    companion object {
        private const val RV_ITEM = 2
        private const val RV_ITEM_MUSIC_CATEGORY = 3
        private const val RV_ITEM_MUSIC_FAVORITE = 4
        private const val RV_ITEM_MUSIC_FOLDER = 5
        private const val RV_ITEM_MUSIC_ARTIST = 6
        private const val RV_ITEM_MUSIC_PLAY_LIST = 7
        private const val RV_ITEM_MUSIC_PLAYLIST_ADD_NEW = 8
        const val RV_ITEM_MUSIC_GENRES = 9
        const val RV_ITEM_MUSIC_ALBUMS = 10
    }

    private val _itemClick = MutableStateFlow<Boolean>(false)
    val itemClick = _itemClick.asStateFlow()

    private val _closeBack = MutableStateFlow<Boolean>(false)
    val closeBack = _closeBack.asStateFlow()

    init {
        categoryData(None()) { it.either({}, ::onCategoryLoaded) }
        favoriteData(None()).launchOn(viewModelScope) {
            _favorite.value = it.toRecyclerViewItemsFavorite()
        }
        playList(None()).launchOn(viewModelScope) {
            _playlist.value = it.toRecyclerViewItemsPlayList()
        }
        rvAllFolderWithMusic(None()) { it.either({}, ::onAllFolderWithMusic) }
        artistList(None()) { it.either({}, ::onArtistListRV) }
        artistList(None()) { it.either({}, ::onGenresListRV) }
        artistList(None()) { it.either({}, ::onAlbumsListRV) }
    }

    fun onClickBack() {
        Log.i("resBack", "onClickBack")
        checkRootPathOfSource()
    }

    private fun checkRootPathOfSource() {
        when (itemClick.value) {
            true -> {
                Log.i("resBack", "checkRootPathOfSourcet$itemClick")
                _closeBack.value = true
            }
            false -> {
                Log.i("resBack", "checkRootPathOfSource$itemClick")
                _closeBack.value = false
            }
        }
    }

    private val _items = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    var items = _items.asStateFlow()

    private val _huitems = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    var huitems = _huitems.asStateFlow()

    private val _favorite = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    var favorite = _favorite.asStateFlow()

    private val _artist = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    var artist = _artist.asStateFlow()

    private val _playlist = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    var playlist = _playlist.asStateFlow()

    private val _albums = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    var albums = _albums.asStateFlow()

    private val _genres = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    var genres = _genres.asStateFlow()

    private fun List<CategoryMusicModel>.toRecyclerViewItemsCategory(): List<RecyclerViewBaseDataModel> {
        val newList = mutableListOf<RecyclerViewBaseDataModel>()
        this.forEach {
            newList.add(
                RecyclerViewBaseDataModel(
                    it,
                    RV_ITEM_MUSIC_CATEGORY
                )
            )
        }
        return newList
    }

    private fun onAllFolderWithMusic(folderMusic: List<AllFolderWithMusic>) {
        _items.value = folderMusic.toRecyclerViewItemsFolder()
        _itemClick.value = true
    }

    private fun onArtistListRV(artistMusic: List<Track>) {
        val pot = artistMusic.distinctBy { it.artist }
        _artist.value = pot.toRecyclerViewItemsArtist()
        _itemClick.value = true
    }

    private fun onGenresListRV(genresMusic: List<Track>) {
        val pot = genresMusic.distinctBy { it.album }
        _genres.value = pot.toRecyclerViewItemsGenres()
        _itemClick.value = true
    }

    private fun onAlbumsListRV(albumsMusic: List<Track>) {
        val pot = albumsMusic.distinctBy { it.album }
        _albums.value = pot.toRecyclerViewItemsAlbums()
        _itemClick.value = true
    }

    private fun onPlayListRV(playListMusic: List<PlayListModel>) {
        _playlist.value = playListMusic.toRecyclerViewItemsPlayList()
        _itemClick.value = true
    }

    private fun List<AllFolderWithMusic>.toRecyclerViewItemsFolder(): List<RecyclerViewBaseDataModel> {
        val newList = mutableListOf<RecyclerViewBaseDataModel>()
        this.forEach {
            newList.add(
                RecyclerViewBaseDataModel(
                    it, RV_ITEM_MUSIC_FOLDER
                )
            )
        }
        return newList
    }

    private fun List<PlayListModel>.toRecyclerViewItemsPlayList(): List<RecyclerViewBaseDataModel> {
        val newList = mutableListOf<RecyclerViewBaseDataModel>()
     /*   newList.add(
            0,
            RecyclerViewBaseDataModel(
                AddNewPlayListModel("Добавить новый Плейлист"),
                RV_ITEM_MUSIC_PLAYLIST_ADD_NEW
            )
        )*/
        this.forEach {
            newList.add(
                RecyclerViewBaseDataModel(
                    it
                    , RV_ITEM_MUSIC_PLAY_LIST
                )
            )
        }
        return newList
    }


    private fun List<FavoriteSongs>.toRecyclerViewItemsFavorite(): List<RecyclerViewBaseDataModel> {
        val newList = mutableListOf<RecyclerViewBaseDataModel>()
        this.forEach {
            newList.add(
                RecyclerViewBaseDataModel(
                    it,
                    RV_ITEM_MUSIC_FAVORITE
                )
            )
        }
        return newList
    }


    private fun List<Track>.toRecyclerViewItemsArtist(): List<RecyclerViewBaseDataModel> {
        val newList = mutableListOf<RecyclerViewBaseDataModel>()
        this.forEach {
            newList.add(
                RecyclerViewBaseDataModel(
                    it, RV_ITEM_MUSIC_ARTIST
                )
            )
        }
        return newList
    }

    private fun List<Track>.toRecyclerViewItemsGenres(): List<RecyclerViewBaseDataModel> {
        val newList = mutableListOf<RecyclerViewBaseDataModel>()
        this.forEach {
            newList.add(
                RecyclerViewBaseDataModel(
                    it, RV_ITEM_MUSIC_GENRES
                )
            )
        }
        return newList
    }

    private fun List<Track>.toRecyclerViewItemsAlbums(): List<RecyclerViewBaseDataModel> {
        val newList = mutableListOf<RecyclerViewBaseDataModel>()
        this.forEach {
            newList.add(
                RecyclerViewBaseDataModel(
                    it, RV_ITEM_MUSIC_ALBUMS
                )
            )
        }
        return newList
    }




    private fun onCategoryLoaded(category: List<CategoryMusicModel>) {
        _huitems.value = category.toRecyclerViewItemsCategory()
        _itemClick.value = true
    }

    /* private fun  onFavoriteLoaded(favorite: List<FavoriteSongs>){
         _favorite.value = favorite.toRecyclerViewItemsFavorite()
     }
 */
}