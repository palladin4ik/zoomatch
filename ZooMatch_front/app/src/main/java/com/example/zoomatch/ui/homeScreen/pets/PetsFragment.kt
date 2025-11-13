package com.example.zoomatch.ui.homeScreen.pets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.zoomatch.R
import com.example.zoomatch.databinding.HomeFragmentPetsBinding
import com.example.zoomatch.databinding.ItemPetBinding

class PetsFragment : Fragment() {

  private var _binding: HomeFragmentPetsBinding? = null

  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  val petList = listOf(
    PetsAdapter.Pet("1", "Барсик", "Британская", 2, "В активном поиске"),
    PetsAdapter.Pet("2", "Мурка", "Мейн-кун", 1, "Не ищет пару"),
    PetsAdapter.Pet("3", "Пушок", "Сибирская", 3, "В активном поиске"),
    PetsAdapter.Pet("4", "Рекс", "Золотистый ретривер", 4, "На вязке")
  )

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val petsViewModel =
      ViewModelProvider(this)[PetsViewModel::class.java]

    _binding = HomeFragmentPetsBinding.inflate(inflater, container, false)
    val root: View = binding.root


    return root
  }

  private var isGrid = true

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val adapter = PetsAdapter(petList)
    binding.petsRecyclerView.adapter = adapter

    updateLayoutManager()

    binding.viewChangeButton.setOnClickListener {
      isGrid = !isGrid
      updateLayoutManager()
      updateIcon()
    }
  }

  private fun updateLayoutManager() {
    val spanCount = if (isGrid) 2 else 1
    binding.petsRecyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)
    binding.petsRecyclerView.adapter?.notifyDataSetChanged()
  }

  private fun updateIcon() {
    val icon = if (isGrid) R.drawable.test_avatar else R.drawable.test_avatar
    binding.viewChangeButton.setIconResource(icon)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}

class PetsAdapter(
  private val pets: List<Pet>
) : RecyclerView.Adapter<PetsAdapter.PetViewHolder>() {

  // Модель данных
  data class Pet(
    val id: String,
    val name: String,
    val breed: String,
    val age: Int,
    val status: String,
    val imageUrl: String? = null
  )

  class PetViewHolder(private val binding: ItemPetBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(pet: Pet) {
      binding.petName.text = pet.name
      binding.petInfo.text = "${pet.breed}, ${pet.age} ${pluralYears(pet.age)}"
      binding.petStatus.text = pet.status
      binding.petStatus.setTextColor(getStatusColor(pet.status))

      // Фото — Glide
      Glide.with(binding.root.context)
        .load(pet.imageUrl ?: R.drawable.test_avatar)
        .placeholder(R.drawable.test_avatar)
        .error(R.drawable.test_avatar)
        .centerCrop()
        .into(binding.petPhoto)
    }

    private fun pluralYears(age: Int): String {
      return when {
        age % 10 == 1 && age % 100 != 11 -> "год"
        age % 10 in 2..4 && age % 100 !in 12..14 -> "года"
        else -> "лет"
      }
    }

    private fun getStatusColor(status: String): Int {
      val context = binding.root.context
      return when {
        status.contains("поиск") -> ContextCompat.getColor(context, R.color.green)
        status.contains("вязк") -> ContextCompat.getColor(context, R.color.orange)
        else -> ContextCompat.getColor(context, R.color.gray)
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