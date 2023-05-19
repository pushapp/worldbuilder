package com.worldbuilder.mapgame.ui.fragments

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.worldbuilder.mapgame.Lifeform
import com.worldbuilder.mapgame.R
import com.worldbuilder.mapgame.databinding.FragmentGameBinding
import com.worldbuilder.mapgame.extensions.showSnackbar
import com.worldbuilder.mapgame.extensions.viewModelFactory
import com.worldbuilder.mapgame.models.Position
import com.worldbuilder.mapgame.models.lifeform.LifeformChangeListener
import com.worldbuilder.mapgame.repositories.LocalSessionRepository
import com.worldbuilder.mapgame.repositories.SessionRepository
import com.worldbuilder.mapgame.utils.LifeformUtils.createLifeformImageView
import com.worldbuilder.mapgame.viewmodels.GameViewModel

class GameFragment : Fragment(), LifeformChangeListener {
    private lateinit var binding: FragmentGameBinding
    private val viewModel: GameViewModel by viewModels(factoryProducer = viewModelFactory {
        val repo: SessionRepository = LocalSessionRepository(requireContext().applicationContext)
        GameViewModel(repo)
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

        viewModel.load()
    }

    override fun onResume() {
        super.onResume()
        viewModel.setLifeformChangeListener(this)
        viewModel.startTimer()
    }

    override fun onPause() {
        viewModel.setLifeformChangeListener(null)
        viewModel.stopTimer()
        super.onPause()

    }

    override fun onLifeFormCreated(lifeform: Lifeform) {
        val image = createLifeformImageView(lifeform, requireContext())
        binding.lifeFormContainer.addView(image)
    }

    override fun onLifeformRemoved(lifeform: Lifeform) {
        lifeform.imageView?.let {
            binding.lifeFormContainer.removeView(it)
        }
    }

    override fun onLifeformMoved(lifeform: Lifeform, newPosition: Position) {
        (lifeform.imageView.layoutParams as? RelativeLayout.LayoutParams)?.apply {
            leftMargin = newPosition.x
            topMargin = newPosition.y
        }
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

        //bind darwin points
        viewModel.darwinPoints.observe(viewLifecycleOwner) {
            val darwinPointsString = getString(R.string.darwin_points, it)
            binding.dpoints.text = darwinPointsString
        }

        //bind map
        viewModel.bitmap.observe(viewLifecycleOwner) {
            val map: Drawable = BitmapDrawable(resources, it)

            val lp: FrameLayout.LayoutParams = FrameLayout.LayoutParams(it.width, it.height)
            binding.lifeFormContainer.layoutParams = lp
            binding.lifeFormContainer.background = map
        }
        //bind world
        viewModel.world.observe(viewLifecycleOwner) { world ->
            //remove all old views
            //TODO: rework lifeFormContainer to pure canvas
            binding.lifeFormContainer.removeAllViews()

            //add plant views
            world.plants
                .map { p -> createLifeformImageView(p, requireContext()) }
                .forEach { imageView ->
                    binding.lifeFormContainer.addView(imageView)
                }
            //add animal views
            world.animals.map { a -> createLifeformImageView(a, requireContext()) }
                .forEach { imageView ->
                    binding.lifeFormContainer.addView(imageView)
                }
        }
    }
}