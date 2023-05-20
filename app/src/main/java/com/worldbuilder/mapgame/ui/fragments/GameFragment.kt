package com.worldbuilder.mapgame.ui.fragments

import android.annotation.SuppressLint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.worldbuilder.mapgame.Lifeform
import com.worldbuilder.mapgame.MapUtils
import com.worldbuilder.mapgame.R
import com.worldbuilder.mapgame.databinding.FragmentGameBinding
import com.worldbuilder.mapgame.extensions.showSnackbar
import com.worldbuilder.mapgame.extensions.viewModelFactory
import com.worldbuilder.mapgame.models.Position
import com.worldbuilder.mapgame.models.lifeform.LifeformChangeListener
import com.worldbuilder.mapgame.models.lifeform.LifeformType
import com.worldbuilder.mapgame.repositories.LocalSessionRepository
import com.worldbuilder.mapgame.repositories.SessionRepository
import com.worldbuilder.mapgame.ui.dialogs.CustomizeWorldDialog
import com.worldbuilder.mapgame.ui.dialogs.CustomizeWorldDialogListener
import com.worldbuilder.mapgame.ui.dialogs.MapClickDialog
import com.worldbuilder.mapgame.ui.dialogs.SimpleAddLifeform
import com.worldbuilder.mapgame.utils.LifeformUtils.createLifeformImageView
import com.worldbuilder.mapgame.viewmodels.GameViewModel

class GameFragment : Fragment(), LifeformChangeListener,
    CustomizeWorldDialogListener {
    private lateinit var binding: FragmentGameBinding
    private val viewModel: GameViewModel by viewModels(factoryProducer = viewModelFactory {
        val repo: SessionRepository = LocalSessionRepository(requireContext().applicationContext)
        GameViewModel(repo)
    })

    private val mapClickDialog: MapClickDialog by lazy {
        MapClickDialog(
            context = requireContext(),
            onAddLifeFormClick = { onAddLifeFormClicked() },
            onViewLifeFormClick = { onViewLifeFormClick() }
        )
    }
    private val lastClickedPosition = Position()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.newWorldBt.setOnClickListener {
            val dialog = CustomizeWorldDialog()
            dialog.setOnCreateWorldListener(this)
            dialog.show(
                requireActivity().supportFragmentManager,
                "customize_world_dialog"
            )
        }

        binding.hudRl.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastClickedPosition.x = motionEvent.x.toInt()
                    lastClickedPosition.y = motionEvent.y.toInt()

                    if (mapClickDialog.isShown()) {
                        mapClickDialog.closePopup()
                    }
                }
            }
            false
        }

        binding.lifeFormContainer.setOnClickListener {
            mapClickDialog.showPopupWindow(lastClickedPosition)
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

    private fun onAddLifeFormClicked() {
        //TODO: rework on navController way
        val addLifeformDialog = SimpleAddLifeform(requireContext())
        addLifeformDialog.setLifeformClickListener { lifeformType: LifeformType ->
            Log.d("TAG", "on node type: $lifeformType requested to create")
            val tiles = viewModel.world.value?.map ?: emptyArray()
            val randomNearPositions = MapUtils.generateSurroundingPositions(lastClickedPosition, tiles, false, 1, 3)
            val selectedPositions = MapUtils.getRandomPositions(randomNearPositions, 5)

            viewModel.createLifeform(lifeformType, selectedPositions)
        }
        addLifeformDialog.show()
    }

    private fun onViewLifeFormClick() {

    }

    private fun bindUI() {
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            showSnackbar(it)
        }

        viewModel.loading.observe(viewLifecycleOwner) {
            binding.loadingAnim.setIsVisible(it)

            binding.hudRl.setIsVisible(!it)
            binding.lifeFormContainer.setIsVisible(!it)
        }

        //bind darwin points
        viewModel.darwinPoints.observe(viewLifecycleOwner) {
            val darwinPointsString = getString(R.string.darwin_points, it)
            binding.darwinPointsTv.text = darwinPointsString
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

    override fun onCreateWorld(waterFrequency: Float, mountainFrequency: Float) {
        viewModel.createWorld(2000, 2000, waterFrequency, mountainFrequency)
    }
}