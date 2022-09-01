package ru.kamaz.music.ui.fragments

import android.app.Activity
import android.app.Dialog
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.eckom.xtlibrary.twproject.music.presenter.MusicPresenter
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import ru.kamaz.music.R
import ru.kamaz.music.databinding.FragmentPlayerBinding
import ru.kamaz.music.di.components.MusicComponent
import ru.kamaz.music.domain.GlobalConstants
import ru.kamaz.music.services.MusicService
import ru.kamaz.music.ui.NavAction.OPEN_DIALOG_ADD_TRACK
import ru.kamaz.music.ui.NavAction.OPEN_TRACK_LIST_FRAGMENT
import ru.kamaz.music.ui.enums.PlayListFlow
import ru.kamaz.music.view_models.fragments.MusicFragmentViewModel
import ru.kamaz.music_api.models.Track
import ru.sir.presentation.base.BaseApplication
import ru.sir.presentation.base.BaseFragment
import ru.sir.presentation.extensions.launchWhenStarted
import ru.sir.presentation.navigation.UiAction
import java.io.File


class MusicFragment :
    BaseFragment<MusicFragmentViewModel, FragmentPlayerBinding>(MusicFragmentViewModel::class.java) {

    override fun inject(app: BaseApplication) {
        app.getComponent<MusicComponent>().inject(this)
    }

    private var _binding: FragmentPlayerBinding? = null
    private val binDing get() = _binding!!

    override fun onPause() {
        viewModel.isSaveLastMusic()
        super.onPause()
    }

    override fun onDestroy() {
        viewModel.isSaveLastMusic()
        viewModel.appClosed()
        super.onDestroy()
    }

    override fun onResume() {
        viewModel.lastSavedState()
        val presenter = MusicPresenter(context)
        presenter.openUSBList()
        presenter.getRecord()
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun setListeners() {
        this.binDing.next.setOnClickListener {
            viewModel.nextTrack()
            addEvent2()
        }

        GestureDetector.SimpleOnGestureListener()

        this.binDing.controlPanel.playPause
            .setOnClickListener {
                viewModel.playOrPause()
            }
        this.binDing.controlPanel.rotate.setOnClickListener {
            viewModel.shuffleStatusChange()
        }
        this.binDing.prev.setOnClickListener {
            viewModel.previousTrack()
            addEvent()
        }
        this.binDing.openListFragment.setOnClickListener {
            openMainListMusicFragment()
        }
        this.binDing.changeSourceBtn.setOnClickListener {
            changeSourceViewButtons()
        }
        this.binDing.sourceSelection.btnBt.setOnClickListener {
            changeSourceViewButtons()
            viewModel.vmSourceSelection(MusicService.SourceEnum.BT)
        }
        this.binDing.sourceSelection.disk.setOnClickListener {
            changeSourceViewButtons()
            viewModel.vmSourceSelection(MusicService.SourceEnum.DISK)
        }
        this.binDing.sourceSelection.aux.setOnClickListener {
            changeSourceViewButtons()
            viewModel.vmSourceSelection(MusicService.SourceEnum.AUX)
        }

        this.binDing.sourceSelection.usb.setOnClickListener {
            changeSourceViewButtons()
            viewModel.vmSourceSelection(MusicService.SourceEnum.USB)
        }
        this.binDing.controlPanel.like.setOnClickListener {
            viewModel.isSaveFavoriteMusic(viewModel.data.value)
        }
        this.binDing.controlPanel.repeat.setOnClickListener {
            viewModel.repeatChange()

        }
        this.binDing.controlPanel.addToFolder.setOnClickListener {
            viewModel.fillSelectedTrack()
            navigator.navigateTo(
                UiAction(
                    OPEN_DIALOG_ADD_TRACK
                )
            )
        }

        this.binDing.seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (b) {
                    viewModel.checkPosition(i)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
        super.setListeners()
    }

    val WHERE_MY_CAT_ACTION = "ru.kamaz.musickamaz"
    val ALARM_MESSAGE = "Срочно пришлите кота!"
    val PREV = "com.tw.music.action.prev"

    private fun addEvent() {
        val intent = Intent()
        intent.action = WHERE_MY_CAT_ACTION
        intent.putExtra("ru.kamaz.musickamaz", ALARM_MESSAGE)
        intent.putExtra("com.tw.music.action.prev", ALARM_MESSAGE)
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        context?.sendBroadcast(intent)
    }

    private fun addEvent2() {
        val intent = Intent()
        intent.action = PREV
        intent.putExtra("com.tw.music.action.prev", ALARM_MESSAGE)
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        context?.sendBroadcast(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            228 -> if (resultCode == Activity.RESULT_OK) {
                data?.data?.let {
                    Log.i("onActivityResult", "onActivityResult: $it")
                }
            }
            else -> if (resultCode == Activity.RESULT_OK) {
                println("OK")
            }
        }
    }

    private fun openMainListMusicFragment() {
        navigator.navigateTo(
            UiAction(
                OPEN_TRACK_LIST_FRAGMENT,
                bundleOf(
                    GlobalConstants.MAIN to PlayListFlow.MAIN_WINDOW,
                    "source" to "usb"
                )
            )
        )
    }

    private fun changeSourceViewButtons() {
        changeSource(this.binDing.controlPanel.viewPlayPause)
        changeSource(this.binDing.sourceSelection.viewChangeSource)
    }

    private fun changeSource(view: View) {
        view.visibility = if (view.visibility == View.VISIBLE) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }
    }

    override fun initVars() {
        viewModel.service.launchWhenStarted(lifecycleScope) {
            if (it == null) return@launchWhenStarted
            initServiceVars()
        }
    }

    private fun initServiceVars() {
        viewModel.isPlay.launchWhenStarted(lifecycleScope) { isPlaying ->
            if (isPlaying) {
                this.binDing.controlPanel.playPause.setImageResource(R.drawable.pause_twix)
                this.binDing.controlPanel.playPause.setPadding(35, 35, 35, 35)
            } else {
                this.binDing.controlPanel.playPause.setImageResource(R.drawable.play_triangle)
                this.binDing.controlPanel.playPause.setPadding(37, 35, 33, 35)
            }
        }

        viewModel.sourceName.launchWhenStarted(lifecycleScope) {
            this.binDing.textUsb.text = it
        }

        viewModel.title.launchWhenStarted(lifecycleScope) {
            this.binDing.song.text = it
        }

        viewModel.artist.launchWhenStarted(lifecycleScope) {
            this.binDing.artist.text = it
        }

        viewModel.cover.launchWhenStarted(lifecycleScope) { updateTrackCover(it) }

        viewModel.duration.launchWhenStarted(lifecycleScope) {
            if (it != 0) {
                viewModel.musicPosition.launchWhenStarted(lifecycleScope) {
                    val currentPosition = if (it < 0) 0 else it
                    this.binDing.seek.progress = currentPosition
                    this.binDing.startTime.text = Track.convertDuration(currentPosition.toLong())
                }
                this.binDing.seek.max = it
                this.binDing.endTime.text = Track.convertDuration(it.toLong())
            }
        }

        viewModel.repeatHowModeNow.launchWhenStarted(lifecycleScope) {
            repeatIconChange(it)
        }

        viewModel.defaultModeOn.launchWhenStarted(lifecycleScope) {
            if (it) startDefaultMode()
        }

        viewModel.isMusicEmpty.launchWhenStarted(lifecycleScope) {
            if (it) {
                Toast.makeText(context, "Файлы не найдены", Toast.LENGTH_LONG).show()
                this.binDing.pictureBucket.visibility = View.GONE
                this.binDing.seek.progress = 0
            }
        }

//        viewModel.musicPosition.launchWhenStarted(lifecycleScope) {
//                val currentPosition = if (it < 0) 0 else it
//                binding.seek.progress = currentPosition
//                binding.startTime.text = Track.convertDuration(currentPosition.toLong())
//        }

        viewModel.playListScrolling.launchWhenStarted(lifecycleScope) {
            if (it) openMainListMusicFragment()
        }

//        viewModel.isNotConnected.launchWhenStarted(lifecycleScope) {
//            if (it) {
//                if (this.binDing != null) diskModeActivation()
//            } else {
//                viewModel.vmSourceSelection(MusicService.SourceEnum.BT)
//                btModeActivation()
//            }
//        }

        viewModel.isShuffleOn.launchWhenStarted(lifecycleScope) {
            randomSongStatus(it)
        }
        viewModel.isNotConnectedUsb.launchWhenStarted(lifecycleScope) {
            if (it) {
                usbModeActivation()
            } else {
                if (this.binDing != null) diskModeActivation()
            }
        }
        viewModel.isFavoriteMusic.launchWhenStarted(lifecycleScope) {
            likeStatus(it)
        }
        viewModel.isBtModeOn.launchWhenStarted(lifecycleScope) {
            if (it) btModeActivation()
        }
        viewModel.isPlayListModeOn.launchWhenStarted(lifecycleScope) {
            if (!viewModel.isBtModeOn.value && !viewModel.isUsbModeOn.value && !viewModel.isDiskModeOn.value && !viewModel.isAuxModeOn.value) {
                playListModeActivation()
            }
        }
        viewModel.isAuxModeOn.launchWhenStarted(lifecycleScope) {
            if (it) auxModeActivation()
        }
        viewModel.isDiskModeOn.launchWhenStarted(lifecycleScope) {
            if (it) diskModeActivation()
        }
        viewModel.isUsbModeOn.launchWhenStarted(lifecycleScope) {
            if (it) usbModeActivation()
        }
        viewModel.isDeviceNotConnectFromBt.launchWhenStarted(lifecycleScope) {
            if (it) showBtSettingsDialog()
        }
        viewModel.isPlayListModeOn.launchWhenStarted(lifecycleScope) {
            when (it) {
                "disk" -> {
                    this.binDing.textUsb.setPadding(0, 0, 0, 0)
                    this.binDing.sourceImage.visibility = View.INVISIBLE
                }
                "usb" -> {
                    this.binDing.textUsb.setPadding(0, 0, 0, 0)
                    this.binDing.sourceImage.visibility = View.INVISIBLE
                }
                "bt" -> {
                    this.binDing.textUsb.setPadding(0, 0, 0, 0)
                    this.binDing.sourceImage.visibility = View.INVISIBLE
                }
                "folder" -> {
                    this.binDing.textUsb.setPadding(35, 0, 0, 0)
                    this.binDing.sourceImage.visibility = View.VISIBLE
                    this.binDing.sourceImage.setImageResource(R.drawable.source_folder)
                }
                "playList" -> {
                    this.binDing.textUsb.setPadding(35, 0, 0, 0)
                    this.binDing.sourceImage.visibility = View.VISIBLE
                    this.binDing.sourceImage.setImageResource(R.drawable.source_playlist)
                }
                "favorite" -> {
                    this.binDing.textUsb.setPadding(35, 0, 0, 0)
                    this.binDing.sourceImage.visibility = View.VISIBLE
                    this.binDing.sourceImage.setImageResource(R.drawable.source_favorite)
                }
                else -> {
                    this.binDing.textUsb.setPadding(0, 0, 0, 0)
                    this.binDing.sourceImage.visibility = View.INVISIBLE
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        viewModel.appClosed()
    }

    private fun updateTrackCover(coverPath: String) {
        if (coverPath != "") {
            Picasso.with(context)
                .load(Uri.fromFile(File(coverPath.trim())))
                .into(this.binDing.picture)
            Picasso.with(context)
                .load(Uri.fromFile(File(coverPath.trim())))
                .transform(BlurTransformation(context, 50, 10))
                .resize(1024, 555)
                .into(this.binDing.pictureDevice)
            this.binDing.pictureBucket.visibility = View.VISIBLE
        } else {
            this.binDing.pictureBucket.visibility = View.INVISIBLE
            if (!viewModel.isBtModeOn.value) this.binDing.pictureDevice.setImageResource(R.drawable.music_png_bg)
        }
    }


    private fun repeatIconChange(repeat: Int) {
        when (repeat) {
            2 -> {
                this.binDing.controlPanel.repeat.setImageResource(R.drawable.repeat_mode_all)
                this.binDing.controlPanel.repeat.setPadding(22, 22, 22, 22)
            }

            1 -> {
                this.binDing.controlPanel.repeat.setImageResource(R.drawable.repeate_mode_single)
                this.binDing.controlPanel.repeat.setPadding(22, 14, 20, 22)
            }

        }

    }

    private fun randomSongStatus(random: Boolean) {
        if (random) this.binDing.controlPanel.rotate.setImageResource(R.drawable.shuffle_mode_true)
        else this.binDing.controlPanel.rotate.setImageResource(R.drawable.shuffle_mode_false)
    }

    private fun likeStatus(like: Boolean) {
        if (like) {
            this.binDing.controlPanel.like.setImageResource(R.drawable.like_true)
        } else {
            this.binDing.controlPanel.like.setImageResource(R.drawable.like_false)
        }
    }

    private fun startDefaultMode() {
        playListModeActivation()
        this.binDing.pictureBucket.visibility = View.GONE
        this.binDing.pictureDevice.setImageResource(R.drawable.music_png_bg)
        this.binDing.song.text = getString(R.string.default_title)
        this.binDing.artist.visibility = View.INVISIBLE
    }

    private fun playListModeActivation() {
        this.binDing.sourceSelection.disk.setBackgroundResource(R.drawable.back_item)
        this.binDing.sourceSelection.aux.setBackgroundResource(R.drawable.back_item)
        this.binDing.sourceSelection.btnBt.setBackgroundResource(R.drawable.back_item)
        this.binDing.sourceSelection.usb.setBackgroundResource(R.drawable.back_item)
    }

    private fun btModeActivation() {
        //Invisible
        this.binDing.sourceSelection.viewChangeSource.visibility = View.INVISIBLE
        this.binDing.controlPanel.repeat.visibility = View.INVISIBLE
        this.binDing.controlPanel.rotate.visibility = View.INVISIBLE
        this.binDing.controlPanel.like.visibility = View.INVISIBLE
        this.binDing.controlPanel.addToFolder.visibility = View.INVISIBLE
        this.binDing.openListFragment.visibility = View.INVISIBLE
        this.binDing.seekLayout.visibility = View.INVISIBLE
        this.binDing.times.visibility = View.INVISIBLE
        //Visible
        this.binDing.controlPanel.viewPlayPause.visibility = View.VISIBLE
        this.binDing.controlPanel.playPause.visibility = View.VISIBLE
        this.binDing.nextPrev.visibility = View.VISIBLE
        this.binDing.artist.visibility = View.VISIBLE
        this.binDing.song.visibility = View.VISIBLE
        this.binDing.pictureDevice.visibility = View.VISIBLE
        //Background
        this.binDing.sourceSelection.disk.setBackgroundResource(R.drawable.back_item)
        this.binDing.sourceSelection.aux.setBackgroundResource(R.drawable.back_item)
        this.binDing.sourceSelection.btnBt.setBackgroundResource(R.drawable.back_item_on)
        this.binDing.sourceSelection.usb.setBackgroundResource(R.drawable.back_item)
        this.binDing.pictureDevice.setImageResource(R.drawable.bluetooth_back)
        this.binDing.musicButtons.setBackgroundResource(R.color.test2)
        this.binDing.artist.setBackgroundResource(R.color.test2)

    }

    private fun diskModeActivation() {
        updateTrackCover(viewModel.cover.value)
        //Invisible
        this.binDing.sourceSelection.viewChangeSource.visibility = View.INVISIBLE
        //Visible
        this.binDing.controlPanel.viewPlayPause.visibility = View.VISIBLE
        this.binDing.controlPanel.addToFolder.visibility = View.VISIBLE
        this.binDing.controlPanel.repeat.visibility = View.VISIBLE
        this.binDing.controlPanel.rotate.visibility = View.VISIBLE
        this.binDing.controlPanel.like.visibility = View.VISIBLE
        this.binDing.controlPanel.playPause.visibility = View.VISIBLE
        this.binDing.openListFragment.visibility = View.VISIBLE
        this.binDing.controlPanel.addToFolder.visibility = View.VISIBLE
        this.binDing.seekLayout.visibility = View.VISIBLE
        this.binDing.nextPrev.visibility = View.VISIBLE
        this.binDing.artist.visibility = View.VISIBLE
        this.binDing.song.visibility = View.VISIBLE
        this.binDing.times.visibility = View.VISIBLE
//        binding.picture.visibility = View.VISIBLE
        this.binDing.pictureDevice.visibility = View.VISIBLE
        this.binDing.seek.visibility = View.VISIBLE
//        binding.pictureBucket.visibility = View.VISIBLE
        //Background
        this.binDing.sourceSelection.disk.setBackgroundResource(R.drawable.back_item_on)
        this.binDing.sourceSelection.aux.setBackgroundResource(R.drawable.back_item)
        this.binDing.sourceSelection.btnBt.setBackgroundResource(R.drawable.back_item)
        this.binDing.sourceSelection.usb.setBackgroundResource(R.drawable.back_item)
    }

    private fun auxModeActivation() {
        //Visible
        this.binDing.picture.visibility = View.VISIBLE
        this.binDing.controlPanel.viewPlayPause.visibility = View.VISIBLE
        //Invisible
        this.binDing.sourceSelection.viewChangeSource.visibility = View.INVISIBLE
        this.binDing.controlPanel.repeat.visibility = View.INVISIBLE
        this.binDing.controlPanel.rotate.visibility = View.INVISIBLE
        this.binDing.controlPanel.like.visibility = View.INVISIBLE
        this.binDing.controlPanel.playPause.visibility = View.INVISIBLE
        this.binDing.openListFragment.visibility = View.INVISIBLE
        this.binDing.controlPanel.addToFolder.visibility = View.INVISIBLE
        this.binDing.seek.visibility = View.INVISIBLE
        this.binDing.nextPrev.visibility = View.INVISIBLE
        this.binDing.artist.visibility = View.INVISIBLE
        this.binDing.song.visibility = View.INVISIBLE
        this.binDing.times.visibility = View.INVISIBLE
        this.binDing.pictureBucket.visibility = View.INVISIBLE
        //Background
        this.binDing.sourceSelection.disk.setBackgroundResource(R.drawable.back_item)
        this.binDing.sourceSelection.aux.setBackgroundResource(R.drawable.back_item_on)
        this.binDing.sourceSelection.btnBt.setBackgroundResource(R.drawable.back_item)
        this.binDing.sourceSelection.usb.setBackgroundResource(R.drawable.back_item)
        this.binDing.pictureDevice.setImageResource(R.drawable.auxx)

    }

    private fun usbModeActivation() {
        updateTrackCover(viewModel.cover.value)
        //Invisible
//        binding.picture.visibility = View.VISIBLE
        this.binDing.sourceSelection.viewChangeSource.visibility = View.INVISIBLE
        //Visible
        this.binDing.controlPanel.viewPlayPause.visibility = View.VISIBLE
        this.binDing.controlPanel.addToFolder.visibility = View.VISIBLE
        this.binDing.controlPanel.repeat.visibility = View.VISIBLE
        this.binDing.controlPanel.rotate.visibility = View.VISIBLE
        this.binDing.controlPanel.like.visibility = View.VISIBLE
        this.binDing.controlPanel.playPause.visibility = View.VISIBLE
        this.binDing.openListFragment.visibility = View.VISIBLE
        this.binDing.controlPanel.addToFolder.visibility = View.VISIBLE
        this.binDing.seekLayout.visibility = View.VISIBLE
        this.binDing.nextPrev.visibility = View.VISIBLE
        this.binDing.artist.visibility = View.VISIBLE
        this.binDing.song.visibility = View.VISIBLE
        this.binDing.times.visibility = View.VISIBLE
        this.binDing.pictureDevice.visibility = View.VISIBLE
        this.binDing.seek.visibility = View.VISIBLE
//        binding.pictureBucket.visibility = View.VISIBLE
        //Background
        this.binDing.sourceSelection.disk.setBackgroundResource(R.drawable.back_item)
        this.binDing.sourceSelection.usb.setBackgroundResource(R.drawable.back_item_on)
        this.binDing.sourceSelection.aux.setBackgroundResource(R.drawable.back_item)
        this.binDing.sourceSelection.btnBt.setBackgroundResource(R.drawable.back_item)
//        binding.pictureDevice.setImageResource(R.drawable.music_png_bg)
    }


    private fun showBtSettingsDialog() {
        val dialog = Dialog(requireContext())
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.bt_settings_alert)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)

        val playlistName: TextView = dialog.findViewById(R.id.tv_question_bt_connection)
        val deleteText: TextView = dialog.findViewById(R.id.delete_text)
        val close: TextView = dialog.findViewById(R.id.btn_close)
        val add: TextView = dialog.findViewById(R.id.btn_add_to_playlist)

        playlistName.text = getString(R.string.connect_bt)
        deleteText.text = getString(R.string.bt_question)
        close.text = getString(R.string.settings_close)
        add.text = getString(R.string.settings)
        add.setOnClickListener {
            openBluetoothSettings()
            dialog.dismiss()
        }
        close.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun openBluetoothSettings() {
        val intent = Intent(Intent.ACTION_MAIN, null)
        val componentName =
            ComponentName("ru.bis.settings", "ru.bis.settings.presentation.StartActivity")
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.component = componentName
        intent.putExtra("BluetoothSettings", true)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        startActivity(intent)
    }

    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentPlayerBinding {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binDing
    }


}



