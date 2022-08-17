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

    override fun onPause() {
        viewModel.isSaveLastMusic()
        super.onPause()
    }

    override fun onDestroy() {
        viewModel.isSaveLastMusic()
        viewModel.appClosed()
        super.onDestroy()
    }

    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentPlayerBinding.inflate(inflater, container, false)

    override fun onResume() {
        viewModel.lastSavedState()
        val presenter = MusicPresenter(context)
        presenter.openUSBList()
        presenter.getRecord()
        super.onResume()
    }

    override fun setListeners() {
        binding.next.setOnClickListener {
            viewModel.nextTrack()
            addEvent2()
        }

        GestureDetector.SimpleOnGestureListener()

        binding.controlPanel.playPause
            .setOnClickListener {
                viewModel.playOrPause()
            }
        binding.controlPanel.rotate.setOnClickListener {
            viewModel.shuffleStatusChange()
        }
        binding.prev.setOnClickListener {
            viewModel.previousTrack()
            addEvent()
        }
        binding.openListFragment.setOnClickListener {
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
        binding.changeSourceBtn.setOnClickListener {
            changeSourceViewButtons()
        }
        binding.sourceSelection.btnBt.setOnClickListener {
            changeSourceViewButtons()
            viewModel.vmSourceSelection(MusicService.SourceEnum.BT)
        }
        binding.sourceSelection.disk.setOnClickListener {
            changeSourceViewButtons()
            viewModel.vmSourceSelection(MusicService.SourceEnum.DISK)
        }
        binding.sourceSelection.aux.setOnClickListener {
            changeSourceViewButtons()
            viewModel.vmSourceSelection(MusicService.SourceEnum.AUX)
        }

        binding.sourceSelection.usb.setOnClickListener {
            changeSourceViewButtons()
            viewModel.vmSourceSelection(MusicService.SourceEnum.USB)
        }
        binding.controlPanel.like.setOnClickListener {
            viewModel.isSaveFavoriteMusic(viewModel.data.value)
        }
        binding.controlPanel.repeat.setOnClickListener {
            viewModel.repeatChange()

        }
        binding.controlPanel.addToFolder.setOnClickListener {
            viewModel.fillSelectedTrack()
            navigator.navigateTo(
                UiAction(
                    OPEN_DIALOG_ADD_TRACK
                )
            )
        }

        binding.seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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

    private fun changeSourceViewButtons() {
        changeSource(binding.controlPanel.viewPlayPause)
        changeSource(binding.sourceSelection.viewChangeSource)
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
                binding.controlPanel.playPause.setImageResource(R.drawable.pause_twix)
                binding.controlPanel.playPause.setPadding(35, 35,35,35)
            }
            else {
                binding.controlPanel.playPause.setImageResource(R.drawable.play_triangle)
                binding.controlPanel.playPause.setPadding(37, 35,33,35)
            }
        }

        viewModel.sourceName.launchWhenStarted(lifecycleScope) {
            binding.textUsb.text = it
        }

        viewModel.title.launchWhenStarted(lifecycleScope) {
            binding.song.text = it
        }

        viewModel.artist.launchWhenStarted(lifecycleScope) {
            binding.artist.text = it
        }

        viewModel.cover.launchWhenStarted(lifecycleScope) { updateTrackCover(it) }

        viewModel.duration.launchWhenStarted(lifecycleScope) {
            if (it != 0){
                viewModel.musicPosition.launchWhenStarted(lifecycleScope) {
                    val currentPosition = if (it < 0) 0 else it
                    binding.seek.progress = currentPosition
                    binding.startTime.text = Track.convertDuration(currentPosition.toLong())
                }
                binding.seek.max = it
                binding.endTime.text = Track.convertDuration(it.toLong())
            }
        }

        viewModel.repeatHowModeNow.launchWhenStarted(lifecycleScope) {
            repeatIconChange(it)
        }

        viewModel.isMusicEmpty.launchWhenStarted(lifecycleScope) {
            if (it) {
                Toast.makeText(context, "Файлы не найдены", Toast.LENGTH_LONG).show()
                binding.pictureBucket.visibility = View.GONE
                binding.seek.progress = 0
            }
        }

//        viewModel.musicPosition.launchWhenStarted(lifecycleScope) {
//                val currentPosition = if (it < 0) 0 else it
//                binding.seek.progress = currentPosition
//                binding.startTime.text = Track.convertDuration(currentPosition.toLong())
//        }


        viewModel.isNotConnected.launchWhenStarted(lifecycleScope) {
            if (it) {
                diskModeActivation()
            } else {
                viewModel.vmSourceSelection(MusicService.SourceEnum.BT)
                btModeActivation()
            }
        }

        viewModel.isShuffleOn.launchWhenStarted(lifecycleScope) {
            randomSongStatus(it)
        }
        viewModel.isNotConnectedUsb.launchWhenStarted(lifecycleScope) {
            if (it) {
                usbModeActivation()
            } else {
                diskModeActivation()
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
            when (it){
                "disk" -> {
                    binding.textUsb.setPadding(0, 0, 0, 0)
                    binding.sourceImage.visibility = View.INVISIBLE
                }
                "usb" -> {
                    binding.textUsb.setPadding(0, 0, 0, 0)
                    binding.sourceImage.visibility = View.INVISIBLE
                }
                "bt" -> {
                    binding.textUsb.setPadding(0, 0, 0, 0)
                    binding.sourceImage.visibility = View.INVISIBLE
                }
                "folder" -> {
                    binding.textUsb.setPadding(35, 0, 0, 0)
                    binding.sourceImage.visibility = View.VISIBLE
                    binding.sourceImage.setImageResource(R.drawable.source_folder)
                }
                "playList" -> {
                    binding.textUsb.setPadding(35, 0, 0, 0)
                    binding.sourceImage.visibility = View.VISIBLE
                    binding.sourceImage.setImageResource(R.drawable.source_playlist)
                }
                "favorite" -> {
                    binding.textUsb.setPadding(35, 0, 0, 0)
                    binding.sourceImage.visibility = View.VISIBLE
                    binding.sourceImage.setImageResource(R.drawable.source_favorite)
                }
                else -> {
                    binding.textUsb.setPadding(0, 0, 0, 0)
                    binding.sourceImage.visibility = View.INVISIBLE
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
            Log.i("picasso", "updateTrackCover:$coverPath")
            Picasso.with(context)
                .load(Uri.fromFile(File(coverPath.trim())))
                .into(binding.picture)
            Picasso.with(context)
                .load(Uri.fromFile(File(coverPath.trim())))
                .transform(BlurTransformation(context, 50, 10))
                .resize(1024, 555)
                .into(binding.pictureDevice)
            binding.pictureBucket.visibility = View.VISIBLE
        } else {
            Log.i("picasso", "updateackCover:${coverPath.trim()}")
            binding.pictureBucket.visibility = View.INVISIBLE
            if (!viewModel.isBtModeOn.value) binding.pictureDevice.setImageResource(R.drawable.music_png_bg)
        }
    }


    private fun repeatIconChange(repeat: Int) {
        when (repeat) {
            2 -> {
                binding.controlPanel.repeat.setImageResource(R.drawable.repeat_mode_all)
                binding.controlPanel.repeat.setPadding(22, 22, 22, 22)
            }

            1 -> {
                binding.controlPanel.repeat.setImageResource(R.drawable.repeate_mode_single)
                binding.controlPanel.repeat.setPadding(22, 14, 20, 22)
            }

        }

    }

    private fun randomSongStatus(random: Boolean) {
        if (random) binding.controlPanel.rotate.setImageResource(R.drawable.shuffle_mode_true)
        else binding.controlPanel.rotate.setImageResource(R.drawable.shuffle_mode_false)
    }

    private fun likeStatus(like: Boolean) {
        if (like) {
            binding.controlPanel.like.setImageResource(R.drawable.like_true)
        } else {
            binding.controlPanel.like.setImageResource(R.drawable.like_false)
        }
    }


    private fun playListModeActivation() {
        binding.sourceSelection.disk.setBackgroundResource(R.drawable.back_item)
        binding.sourceSelection.aux.setBackgroundResource(R.drawable.back_item)
        binding.sourceSelection.btnBt.setBackgroundResource(R.drawable.back_item)
        binding.sourceSelection.usb.setBackgroundResource(R.drawable.back_item)
    }

    private fun btModeActivation() {
        //Invisible
        binding.sourceSelection.viewChangeSource.visibility = View.INVISIBLE
        binding.controlPanel.repeat.visibility = View.INVISIBLE
        binding.controlPanel.rotate.visibility = View.INVISIBLE
        binding.controlPanel.like.visibility = View.INVISIBLE
        binding.controlPanel.addToFolder.visibility = View.INVISIBLE
        binding.openListFragment.visibility = View.INVISIBLE
        binding.seekLayout.visibility = View.INVISIBLE
        binding.times.visibility = View.INVISIBLE
        //Visible
        binding.controlPanel.viewPlayPause.visibility = View.VISIBLE
        binding.controlPanel.playPause.visibility = View.VISIBLE
        binding.nextPrev.visibility = View.VISIBLE
        binding.artist.visibility = View.VISIBLE
        binding.song.visibility = View.VISIBLE
        binding.pictureDevice.visibility = View.VISIBLE
        //Background
        binding.sourceSelection.disk.setBackgroundResource(R.drawable.back_item)
        binding.sourceSelection.aux.setBackgroundResource(R.drawable.back_item)
        binding.sourceSelection.btnBt.setBackgroundResource(R.drawable.back_item_on)
        binding.sourceSelection.usb.setBackgroundResource(R.drawable.back_item)
        binding.pictureDevice.setImageResource(R.drawable.bluetooth_back)
        binding.musicButtons.setBackgroundResource(R.color.test2)
        binding.artist.setBackgroundResource(R.color.test2)

    }

    private fun diskModeActivation() {
        updateTrackCover(viewModel.cover.value)
        //Invisible
        binding.sourceSelection.viewChangeSource.visibility = View.INVISIBLE
        //Visible
        binding.controlPanel.viewPlayPause.visibility = View.VISIBLE
        binding.controlPanel.addToFolder.visibility = View.VISIBLE
        binding.controlPanel.repeat.visibility = View.VISIBLE
        binding.controlPanel.rotate.visibility = View.VISIBLE
        binding.controlPanel.like.visibility = View.VISIBLE
        binding.controlPanel.playPause.visibility = View.VISIBLE
        binding.openListFragment.visibility = View.VISIBLE
        binding.controlPanel.addToFolder.visibility = View.VISIBLE
        binding.seekLayout.visibility = View.VISIBLE
        binding.nextPrev.visibility = View.VISIBLE
        binding.artist.visibility = View.VISIBLE
        binding.song.visibility = View.VISIBLE
        binding.times.visibility = View.VISIBLE
//        binding.picture.visibility = View.VISIBLE
        binding.pictureDevice.visibility = View.VISIBLE
        binding.seek.visibility = View.VISIBLE
//        binding.pictureBucket.visibility = View.VISIBLE
        //Background
        binding.sourceSelection.disk.setBackgroundResource(R.drawable.back_item_on)
        binding.sourceSelection.aux.setBackgroundResource(R.drawable.back_item)
        binding.sourceSelection.btnBt.setBackgroundResource(R.drawable.back_item)
        binding.sourceSelection.usb.setBackgroundResource(R.drawable.back_item)
    }

    private fun auxModeActivation() {
        //Visible
        binding.picture.visibility = View.VISIBLE
        binding.controlPanel.viewPlayPause.visibility = View.VISIBLE
        //Invisible
        binding.sourceSelection.viewChangeSource.visibility = View.INVISIBLE
        binding.controlPanel.repeat.visibility = View.INVISIBLE
        binding.controlPanel.rotate.visibility = View.INVISIBLE
        binding.controlPanel.like.visibility = View.INVISIBLE
        binding.controlPanel.playPause.visibility = View.INVISIBLE
        binding.openListFragment.visibility = View.INVISIBLE
        binding.controlPanel.addToFolder.visibility = View.INVISIBLE
        binding.seek.visibility = View.INVISIBLE
        binding.nextPrev.visibility = View.INVISIBLE
        binding.artist.visibility = View.INVISIBLE
        binding.song.visibility = View.INVISIBLE
        binding.times.visibility = View.INVISIBLE
        binding.pictureBucket.visibility = View.INVISIBLE
        //Background
        binding.sourceSelection.disk.setBackgroundResource(R.drawable.back_item)
        binding.sourceSelection.aux.setBackgroundResource(R.drawable.back_item_on)
        binding.sourceSelection.btnBt.setBackgroundResource(R.drawable.back_item)
        binding.sourceSelection.usb.setBackgroundResource(R.drawable.back_item)
        binding.pictureDevice.setImageResource(R.drawable.auxx)

    }

    private fun usbModeActivation() {
        updateTrackCover(viewModel.cover.value)
        //Invisible
//        binding.picture.visibility = View.VISIBLE
        binding.sourceSelection.viewChangeSource.visibility = View.INVISIBLE
        //Visible
        binding.controlPanel.viewPlayPause.visibility = View.VISIBLE
        binding.controlPanel.addToFolder.visibility = View.VISIBLE
        binding.controlPanel.repeat.visibility = View.VISIBLE
        binding.controlPanel.rotate.visibility = View.VISIBLE
        binding.controlPanel.like.visibility = View.VISIBLE
        binding.controlPanel.playPause.visibility = View.VISIBLE
        binding.openListFragment.visibility = View.VISIBLE
        binding.controlPanel.addToFolder.visibility = View.VISIBLE
        binding.seekLayout.visibility = View.VISIBLE
        binding.nextPrev.visibility = View.VISIBLE
        binding.artist.visibility = View.VISIBLE
        binding.song.visibility = View.VISIBLE
        binding.times.visibility = View.VISIBLE
        binding.pictureDevice.visibility = View.VISIBLE
        binding.seek.visibility = View.VISIBLE
//        binding.pictureBucket.visibility = View.VISIBLE
        //Background
        binding.sourceSelection.disk.setBackgroundResource(R.drawable.back_item)
        binding.sourceSelection.usb.setBackgroundResource(R.drawable.back_item_on)
        binding.sourceSelection.aux.setBackgroundResource(R.drawable.back_item)
        binding.sourceSelection.btnBt.setBackgroundResource(R.drawable.back_item)
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
        val componentName = ComponentName("ru.bis.settings", "ru.bis.settings.presentation.StartActivity")
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.component = componentName
        intent.putExtra("BluetoothSettings", true)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        startActivity(intent)
    }


}



