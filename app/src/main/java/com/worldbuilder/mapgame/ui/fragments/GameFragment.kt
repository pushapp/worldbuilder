package com.worldbuilder.mapgame.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.worldbuilder.mapgame.R
import com.worldbuilder.mapgame.databinding.FragmentGameBinding
import com.worldbuilder.mapgame.viewmodels.GameViewModel

class GameFragment : Fragment() {
    private lateinit var binding: FragmentGameBinding
    private val viewModel: GameViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.resetButton.setOnClickListener {
            findNavController().navigate(R.id.createNewWorld)
        }
    }
}