package com.example.bookshelf.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.bookshelf.R
import com.example.bookshelf.databinding.FragmentBookDetailBinding
import com.example.bookshelf.ui.BookViewModel

class BookDetailFragment : Fragment() {
    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!
    private val vm: BookViewModel by viewModels()
    private var id: Int = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    private fun decorateGenre(value: String): String {
        val map = mapOf(
            "General" to "📖", "Fantasy" to "🐉", "Sci-Fi" to "🚀", "Mystery" to "🕵️",
            "Romance" to "💘", "Horror" to "👻", "Non-fiction" to "🧠", "Computing" to "💻",
            "History" to "📜", "Classics" to "🎭", "Science" to "🧪", "Other" to "📚"
        )
        if (value.isBlank()) return ""
        val emoji = map[value] ?: "📚"
        return "$emoji  $value"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        id = requireArguments().getInt("bookId")
        vm.load(id) { book ->
            requireActivity().runOnUiThread {
                if (book == null) {
                    findNavController().popBackStack()
                } else {
                    binding.tvTitle.text = book.title
                    binding.tvAuthor.text = book.author
                    binding.detailRating.rating = book.rating.toFloat()
                    binding.tvRead.text = if (book.read) getString(R.string.read) else getString(R.string.unread)
                    binding.tvNotes.text = book.notes
                    binding.tvGenre.text = decorateGenre(book.genre)
                }
            }
        }

        binding.btnEdit.setOnClickListener {
            val b = Bundle().apply { putInt("bookId", id) }
            findNavController().navigate(R.id.addEditBookFragment, b)
        }
        binding.btnDelete.setOnClickListener {
            vm.deleteById(id) {
                requireActivity().runOnUiThread {
                    findNavController().popBackStack(R.id.bookListFragment, /*inclusive=*/false)
                }
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack(R.id.bookListFragment, /*inclusive=*/false)
        }

    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
