package com.example.bookshelf.ui.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.bookshelf.R
import com.example.bookshelf.databinding.FragmentAddEditBookBinding
import com.example.bookshelf.ui.BookViewModel
import android.view.View as AndroidView
import android.view.ViewGroup as AndroidViewGroup
import android.widget.TextView

class AddEditBookFragment : Fragment() {
    private var _binding: FragmentAddEditBookBinding? = null
    private val binding get() = _binding!!
    private val vm: BookViewModel by viewModels()
    private var editingId: Int? = null

    // Simple genre model: value (stored) + label (shown) + emoji
    data class GenreItem(val value: String, val label: String, val emoji: String)

    private val genres by lazy {
        listOf(
            GenreItem(getString(R.string.g_general),   "General",    "📖"),
            GenreItem(getString(R.string.g_fantasy),   "Fantasy",    "🐉"),
            GenreItem(getString(R.string.g_scifi),     "Sci-Fi",     "🚀"),
            GenreItem(getString(R.string.g_mystery),   "Mystery",    "🕵️"),
            GenreItem(getString(R.string.g_romance),   "Romance",    "💘"),
            GenreItem(getString(R.string.g_horror),    "Horror",     "👻"),
            GenreItem(getString(R.string.g_nonfiction),"Non-fiction","🧠"),
            GenreItem(getString(R.string.g_computing), "Computing",  "💻"),
            GenreItem(getString(R.string.g_history),   "History",    "📜"),
            GenreItem(getString(R.string.g_classics),  "Classics",   "🎭"),
            GenreItem(getString(R.string.g_science),   "Science",    "🧪"),
            GenreItem(getString(R.string.g_other),     "Other",      "📚")
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddEditBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupGenreDropdown()

        editingId = arguments?.getInt("bookId")?.takeIf { it > 0 }
        editingId?.let { id ->
            vm.load(id) { book ->
                book?.let {
                    requireActivity().runOnUiThread {
                        binding.etTitle.setText(it.title)
                        binding.etAuthor.setText(it.author)
                        binding.etNotes.setText(it.notes)
                        binding.editRating.rating = it.rating.toFloat()
                        binding.cbRead.isChecked = it.read

                        // set dropdown label for stored value
                        val match = genres.firstOrNull { g -> g.value.equals(it.genre, ignoreCase = true) }
                        if (match != null) {
                            binding.etGenre.setText("${match.emoji}  ${match.label}", false)
                        } else {
                            binding.etGenre.setText("", false)
                        }
                    }
                }
            }
        }

        binding.btnSave.setOnClickListener {
            // Clear previous errors
            binding.tilTitle.error = null
            binding.tilAuthor.error = null

            val title = binding.etTitle.text?.toString()?.trim().orEmpty()
            val author = binding.etAuthor.text?.toString()?.trim().orEmpty()

            var hasError = false
            if (title.isBlank()) {
                binding.tilTitle.error = getString(R.string.required) // add this string
                if (!hasError) binding.etTitle.requestFocus()
                hasError = true
            }
            if (author.isBlank()) {
                binding.tilAuthor.error = getString(R.string.required)
                if (!hasError) binding.etAuthor.requestFocus()
                hasError = true
            }
            if (hasError) return@setOnClickListener

            val notes = binding.etNotes.text?.toString()?.trim().orEmpty()
            val rating = binding.editRating.rating.toInt()
            val read = binding.cbRead.isChecked


            val chosenLabel = binding.etGenre.text?.toString()?.trim().orEmpty()
            val chosenValue = labelToValue(chosenLabel)

            vm.addOrUpdate(
                id = editingId,
                title = title,
                author = author,
                rating = rating,
                read = read,
                notes = notes,
                genre = chosenValue
            ) { newId ->
                requireActivity().runOnUiThread {
                    val b = Bundle().apply { putInt("bookId", newId) }
                    findNavController().navigate(R.id.bookDetailFragment, b)
                }
            }
        }
        binding.btnCancel?.setOnClickListener { findNavController().popBackStack() }
    }

    private fun labelToValue(label: String): String {
        // label is "🕵️  Mystery" or empty
        val stripped = label.replace(Regex("^\\p{So}\\s+"), "") // drop leading emoji+spaces if present
        return genres.firstOrNull { it.label.equals(stripped, true) }?.value ?: ""
    }

    private fun setupGenreDropdown() {
        val items = genres.map { "${it.emoji}  ${it.label}" }

        val adapter = object : ArrayAdapter<String>(requireContext(), 0, items) {
            override fun getView(position: Int, convertView: AndroidView?, parent: AndroidViewGroup): AndroidView {
                val v = layoutInflater.inflate(R.layout.item_genre_dropdown, parent, false)
                v.findViewById<TextView>(R.id.tvIcon).text = genres[position].emoji
                v.findViewById<TextView>(R.id.tvLabel).text = genres[position].label
                return v
            }
            override fun getDropDownView(position: Int, convertView: AndroidView?, parent: AndroidViewGroup): AndroidView {
                val v = layoutInflater.inflate(R.layout.item_genre_dropdown, parent, false)
                v.findViewById<TextView>(R.id.tvIcon).text = genres[position].emoji
                v.findViewById<TextView>(R.id.tvLabel).text = genres[position].label
                return v
            }
        }

        binding.etGenre.setAdapter(adapter)
        binding.etGenre.setOnItemClickListener { _, _, _, _ -> /* no-op */ }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
