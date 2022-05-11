import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import ru.kamaz.music.R
import ru.kamaz.music.view_models.DialogViewModel

class DialogWithData : DialogFragment() {

    companion object {
        const val TAG = "DialogWithData"
    }

    private lateinit var viewModel: DialogViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
      //  viewModel = ViewModelProvider(requireActivity()).get(DialogViewModel::class.java)
        setupClickListeners(view)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setupClickListeners(view: View) {
       /* view.btnSubmit.setOnClickListener {
            viewModel.sendName(view.)
            dismiss()
        }*/
    }

}