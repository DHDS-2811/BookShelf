package com.example.bookshelf.ui.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.bookshelf.databinding.FragmentAddEditBookBinding
import com.example.bookshelf.ui.BookViewModel

class AddEditBookFragment : Fragment() {
    private var _binding: FragmentAddEditBookBinding? = null
    private val binding get() = _binding!!
    private val vm: BookViewModel by viewModels()
    private var editingId: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddEditBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        editingId = arguments?.getInt("bookId")?.takeIf { it > 0 }
        editingId?.let { id ->
            vm.load(id) { book ->
                book?.let {
                    requireActivity().runOnUiThread {
                        binding.etTitle.setText(it.title)
                        binding.etAuthor.setText(it.author)
                        binding.etNotes.setText(it.notes)
                        binding.editRating.rating = it.rating.toFloat()
                        binding.switchRead.isChecked = it.read
                    }
                }
            }
        }

        binding.btnSave.setOnClickListener {
            vm.addOrUpdate(
                id = editingId,
                title = binding.etTitle.text?.toString()?.trim().orEmpty(),
                author = binding.etAuthor.text?.toString()?.trim().orEmpty(),
                rating = binding.editRating.rating.toInt(),
                read = binding.switchRead.isChecked,
                notes = binding.etNotes.text?.toString()?.trim().orEmpty()
            ) { newId ->
                requireActivity().runOnUiThread {
                    val b = Bundle().apply { putInt("bookId", newId) }
                    findNavController().navigate(com.example.bookshelf.R.id.bookDetailFragment, b)
                }
            }
        }
        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()  // back to list
        }

    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
