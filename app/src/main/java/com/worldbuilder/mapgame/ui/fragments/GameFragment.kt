package com.worldbuilder.mapgame.ui.fragments

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.worldbuilder.mapgame.R
import com.worldbuilder.mapgame.databinding.FragmentGameBinding
import com.worldbuilder.mapgame.extensions.showSnackbar
import com.worldbuilder.mapgame.extensions.viewModelFactory
import com.worldbuilder.mapgame.viewmodels.GameViewModel

class GameFragment : Fragment() {
    private lateinit var binding: FragmentGameBinding
    private val viewModel: GameViewModel by viewModels(factoryProducer = viewModelFactory {
        //TODO: add repository assigning here
        GameViewModel()
    })

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

        bindUI()

        viewModel.load(requireContext())
    }

    private fun bindUI() {
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            showSnackbar(it)
        }

        viewModel.loading.observe(viewLifecycleOwner) {
            binding.loadingAnim.setIsVisible(it)

            binding.dpoints.setIsVisible(!it)
            binding.resetButton.setIsVisible(!it)
            binding.layoutForClicklistener.setIsVisible(!it)
            binding.lifeFormContainer.setIsVisible(!it)
        }

        //bind map
        viewModel.bitmap.observe(viewLifecycleOwner) {
            val map: Drawable = BitmapDrawable(resources, it)

            val lp: FrameLayout.LayoutParams = FrameLayout.LayoutParams(it.width, it.height)
            binding.lifeFormContainer.layoutParams = lp
            binding.lifeFormContainer.background = map
        }
    }
}