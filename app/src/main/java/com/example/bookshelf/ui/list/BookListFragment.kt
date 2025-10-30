package com.example.bookshelf.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.bookshelf.R
import com.example.bookshelf.databinding.FragmentBookListBinding
import com.example.bookshelf.ui.BookViewModel

class BookListFragment : Fragment() {
    private var _binding: FragmentBookListBinding? = null
    private val binding get() = _binding!!
    private val vm: BookViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBookListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = BookListAdapter(
            onClick = { book ->
                val action = android.os.Bundle().apply { putInt("bookId", book.id) }
                findNavController().navigate(R.id.bookDetailFragment, action)
            },
            onToggleRead = { book, isRead ->
                vm.setRead(book.id, isRead)
            }
        )

        binding.recycler.adapter = adapter

        vm.books.observe(viewLifecycleOwner) { adapter.submitList(it) }

        binding.searchEdit.doOnTextChanged { text, _, _, _ ->
            vm.setQuery(text?.toString().orEmpty())
        }

        binding.chipAll.setOnClickListener { vm.setFilter(null) }
        binding.chipRead.setOnClickListener { vm.setFilter(true) }
        binding.chipUnread.setOnClickListener { vm.setFilter(false) }

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.addEditBookFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
