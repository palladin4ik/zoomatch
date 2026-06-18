package com.example.zoomatch.ui.homeScreen.pets

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.zoomatch.R
import com.example.zoomatch.data.db.AppDatabase
import com.example.zoomatch.data.homeScreen.pets.PetUI
import com.example.zoomatch.databinding.HomeFragmentPetsBinding
import com.example.zoomatch.databinding.ItemPetBinding
import com.example.zoomatch.ui.homeScreen.HomeViewModelFactory
import com.example.zoomatch.ui.homeScreen.settings.SettingsActivity
import com.google.android.material.chip.Chip
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PetsFragment : Fragment() {

  private var _binding: HomeFragmentPetsBinding? = null
  private val binding get() = _binding!!

  private val viewModel: PetsViewModel by viewModels {
    HomeViewModelFactory(requireActivity().application)
  }

  private lateinit var adapter: PetsAdapter
  private var isGrid = true

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View {
    _binding = HomeFragmentPetsBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    adapter = PetsAdapter(emptyList(), isGrid = true) { petId ->
      viewModel.onPetClick(petId.toInt())
    }
    binding.petsRecyclerView.adapter = adapter
    updateLayoutManager()

    binding.viewToggleGroup.check(binding.gridButton.id)

    binding.gridButton.setOnClickListener {
      isGrid = true
      adapter.setGridMode(true)
      updateLayoutManager()
    }

    binding.listButton.setOnClickListener {
      isGrid = false
      adapter.setGridMode(false)
      updateLayoutManager()
    }

    setupFilterChips()

    binding.searchEditText.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
      override fun afterTextChanged(s: Editable?) {
        viewModel.searchQuery.value = s.toString()
      }
    })

    binding.addPetFab.setOnClickListener {
      viewModel.onAddPetClick()
    }

    binding.settingsIcon.setOnClickListener {
      startActivity(Intent(requireContext(), SettingsActivity::class.java))
    }

    observeData()
  }

  private fun setupFilterChips() {
    viewLifecycleOwner.lifecycleScope.launch {
      val db = AppDatabase.getDatabase(requireActivity().application)
      val userId = db.userDao().getCurrentUserFlow().first()?.id ?: return@launch

      val pets = db.petDao().getPetsFlow().first()
      val distinctTypeIds = pets.mapNotNull { it.animal_type_id }.distinct()

      if (distinctTypeIds.size <= 1) {
        binding.filterScrollView.visibility = View.GONE
        return@launch
      }

      val animalTypes = db.animalTypeDao().getAllFlow().first()
      val typeNameMap = animalTypes.associate { it.id to it.name }

      binding.filterChipGroup.removeAllViews()

      val allChip = createFilterChip("Все", isChecked = true)
      allChip.tag = null
      binding.filterChipGroup.addView(allChip)

      for (typeId in distinctTypeIds) {
        val typeName = typeNameMap[typeId] ?: continue
        val chip = createFilterChip(typeName, isChecked = false)
        chip.tag = typeId
        binding.filterChipGroup.addView(chip)
      }

      binding.filterScrollView.visibility = View.VISIBLE

      binding.filterChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
        val chipId = checkedIds.firstOrNull()
        val chip = chipId?.let { binding.filterChipGroup.findViewById<Chip>(it) }
        val typeId = chip?.tag as? Int
        viewModel.selectedTypeId.value = typeId
      }
    }
  }

  private fun createFilterChip(text: String, isChecked: Boolean): Chip {
    return Chip(requireContext()).apply {
      this.text = text
      isCheckable = true
      this.isChecked = isChecked
      setTextColor(Color.parseColor("#1F2937"))
      chipBackgroundColor = android.content.res.ColorStateList.valueOf(Color.WHITE)
      val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked))
      val colors = intArrayOf(Color.parseColor("#7C3AED"), Color.parseColor("#D1D5DB"))
      chipStrokeColor = android.content.res.ColorStateList(states, colors)
      chipStrokeWidth = resources.displayMetrics.density * 1
      id = View.generateViewId()
    }
  }

  private fun observeData() {
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch {
          viewModel.filteredPets.collect { list ->
            adapter.update(list)
            val isEmpty = list.isEmpty()
            binding.petsRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
            binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
          }
        }
        launch {
          viewModel.openAddPet.collect {
            startActivity(Intent(requireContext(), EditPetActivity::class.java))
          }
        }
        launch {
          viewModel.openEditPet.collect { petId ->
            val intent = Intent(requireContext(), EditPetActivity::class.java).apply {
              putExtra("PET_ID", petId)
            }
            startActivity(intent)
          }
        }
      }
    }
  }

  private fun updateLayoutManager() {
    val spanCount = if (isGrid) 2 else 1
    binding.petsRecyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}

class PetsAdapter(
  private var pets: List<PetUI>,
  private var isGrid: Boolean = true,
  private val onPetClick: (String) -> Unit
) : RecyclerView.Adapter<PetsAdapter.PetViewHolder>() {

  @SuppressLint("NotifyDataSetChanged")
  fun update(newList: List<PetUI>) {
    pets = newList
    notifyDataSetChanged()
  }

  @SuppressLint("NotifyDataSetChanged")
  fun setGridMode(grid: Boolean) {
    isGrid = grid
    notifyDataSetChanged()
  }

  inner class PetViewHolder(val binding: ItemPetBinding) : RecyclerView.ViewHolder(binding.root) {
    @SuppressLint("SetTextI18n")
    fun bind(pet: PetUI) {
      val dp = binding.root.resources.displayMetrics.density
      val imageHeight = if (isGrid) (160 * dp).toInt() else (320 * dp).toInt()
      binding.imageContainer.layoutParams = binding.imageContainer.layoutParams.apply {
        height = imageHeight
      }
      binding.petName.text = pet.name
      binding.petInfo.text = pet.breed

      binding.petGender.text = if (pet.isMale) "♂" else "♀"
      binding.petGender.setTextColor(
        if (pet.isMale) Color.parseColor("#7C3AED") else Color.parseColor("#EC4899")
      )

      val (statusText, statusBgColor, statusTextColor) = when (pet.moderationStatus) {
        "pending" -> Triple("НА МОДЕРАЦИИ", Color.parseColor("#FEF3C7"), Color.parseColor("#D97706"))
        "rejected" -> Triple("ОТКЛОНЕНО", Color.parseColor("#FEE2E2"), Color.parseColor("#DC2626"))
        else -> {
          if (pet.isActive) Triple("В АКТИВНОМ ПОИСКЕ", Color.parseColor("#DCFCE7"), Color.parseColor("#16A34A"))
          else Triple("НЕ ИЩЕТ", Color.parseColor("#F3F4F6"), Color.parseColor("#6B7280"))
        }
      }
      binding.petStatus.text = statusText
      val statusBg = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = 20f
        setColor(statusBgColor)
      }
      binding.petStatus.background = statusBg
      binding.petStatus.setTextColor(statusTextColor)

      val avatarUrl = pet.avatar
      if (!avatarUrl.isNullOrBlank()) {
        val fullUrl = if (avatarUrl.startsWith("http")) avatarUrl
        else "https://zoomatch.ru$avatarUrl"
        Glide.with(binding.petPhoto.context)
          .load(fullUrl)
          .placeholder(R.drawable.ic_paw_print_light)
          .error(R.drawable.ic_paw_print_light)
          .centerCrop()
          .into(binding.petPhoto)
        binding.petPhoto.alpha = 1.0f
      } else {
        binding.petPhoto.setImageResource(R.drawable.ic_paw_print_light)
        binding.petPhoto.alpha = 0.5f
      }

      binding.root.setOnClickListener {
        onPetClick(pet.id)
      }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
    val binding = ItemPetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return PetViewHolder(binding)
  }

  override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
    holder.bind(pets[position])
  }

  override fun getItemCount() = pets.size
}
