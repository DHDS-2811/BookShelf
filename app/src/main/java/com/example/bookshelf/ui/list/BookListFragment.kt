package com.example.bookshelf.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.bookshelf.R
import com.example.bookshelf.data.Book
import com.example.bookshelf.databinding.FragmentBookListBinding
import com.example.bookshelf.ui.BookViewModel

class BookListFragment : Fragment() {

    private var _binding: FragmentBookListBinding? = null
    private val binding get() = _binding!!
    private val vm: BookViewModel by viewModels()

    // null = All genres
    private var selectedGenre: String? = null

    // keep labels to rebuild UI text after navigation
    private lateinit var genreItems: List<Pair<String?, String>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = BookListAdapter(
            onClick = { book ->
                val b = Bundle().apply { putInt("bookId", book.id) }
                findNavController().navigate(R.id.bookDetailFragment, b)
            },
            onToggleRead = { book, isRead -> vm.setRead(book.id, isRead) }
        )
        binding.recycler.adapter = adapter

        // -------- GENRE DROPDOWN (non-filtering) --------
        genreItems = listOf(
            null to getString(R.string.all_genres),
            getString(R.string.g_general)    to "📖  General",
            getString(R.string.g_fantasy)    to "🐉  Fantasy",
            getString(R.string.g_scifi)      to "🚀  Sci-Fi",
            getString(R.string.g_mystery)    to "🕵️  Mystery",
            getString(R.string.g_romance)    to "💘  Romance",
            getString(R.string.g_horror)     to "👻  Horror",
            getString(R.string.g_nonfiction) to "🧠  Non-fiction",
            getString(R.string.g_computing)  to "💻  Computing",
            getString(R.string.g_history)    to "📜  History",
            getString(R.string.g_classics)   to "🎭  Classics",
            getString(R.string.g_science)    to "🧪  Science",
            getString(R.string.g_other)      to "📚  Other"
        )
        val genreLabels = genreItems.map { it.second }

        // Adapter whose Filter always returns the full list
        val nonFilteringAdapter = object : ArrayAdapter<String>(
            requireContext(), android.R.layout.simple_list_item_1, genreLabels
        ) {
            private val all = genreLabels.toList()
            override fun getFilter(): Filter = object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults =
                    FilterResults().apply { values = all; count = all.size }
                @Suppress("UNCHECKED_CAST")
                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    clear(); addAll(all); notifyDataSetChanged()
                }
                override fun convertResultToString(resultValue: Any): CharSequence =
                    resultValue as String
            }
        }

        binding.genreFilter.apply {
            setAdapter(nonFilteringAdapter)
            keyListener = null            // make it non-typable (acts like a spinner)
            setOnClickListener { showDropDown() }
            setOnFocusChangeListener { _, hasFocus -> if (hasFocus) showDropDown() }
            setOnItemClickListener { _, _, pos, _ ->
                selectedGenre = genreItems[pos].first
                vm.books.value?.let { list -> adapter.submitList(applyFilterSort(list)) }
                clearFocus()
            }
            // initial text
            setText(labelFor(selectedGenre), /*filter=*/false)
        }

        // -------- DATA --------
        vm.books.observe(viewLifecycleOwner) { list ->
            adapter.submitList(applyFilterSort(list))
        }

        // -------- OTHER UI --------
        binding.searchEdit.doOnTextChanged { text, _, _, _ ->
            vm.setQuery(text?.toString().orEmpty())
        }
        binding.chipAll.setOnClickListener { vm.setFilter(null) }
        binding.chipRead.setOnClickListener { vm.setFilter(true) }
        binding.chipUnread.setOnClickListener { vm.setFilter(false) }
        binding.fabAdd.setOnClickListener { findNavController().navigate(R.id.addEditBookFragment) }
    }

    override fun onResume() {
        super.onResume()
        // ensure the dropdown text is restored correctly after returning
        binding.genreFilter.setText(labelFor(selectedGenre), /*filter=*/false)
    }

    private fun labelFor(value: String?): String {
        return genreItems.firstOrNull { it.first.equals(value, ignoreCase = true) }?.second
            ?: getString(R.string.all_genres)
    }

    private fun applyFilterSort(src: List<Book>): List<Book> {
        val filtered = selectedGenre?.let { g ->
            src.filter { it.genre.equals(g, ignoreCase = true) }
        } ?: src
        // Always sort by genre (blank last) then title
        return filtered.sortedWith(compareBy({ it.genre.ifBlank { "zzz" } }, { it.title }))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
