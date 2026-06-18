package com.example.zoomatch.ui.homeScreen.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.zoomatch.R
import com.example.zoomatch.data.db.Network
import com.example.zoomatch.data.db.TokenManager
import com.example.zoomatch.data.homeScreen.search.PetShortRecommendation
import com.example.zoomatch.databinding.ActivityOwnerProfileBinding
import com.example.zoomatch.databinding.ItemPetSmallBinding
import com.example.zoomatch.ui.applySystemBarsPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OwnerProfileActivity : AppCompatActivity() {

  private var _binding: ActivityOwnerProfileBinding? = null
  private val binding get() = _binding!!

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    _binding = ActivityOwnerProfileBinding.inflate(layoutInflater)
    setContentView(binding.root)
    binding.root.applySystemBarsPadding()

    val petId = intent.getIntExtra("PET_ID", -1)
    if (petId == -1) {
      finish()
      return
    }

    binding.backButton.setOnClickListener { finish() }

    loadPetWithOwner(petId)
  }

  private fun loadPetWithOwner(petId: Int) {
    val tokenManager = TokenManager(this)

    lifecycleScope.launch {
      val token = tokenManager.getAccessToken() ?: return@launch
      try {
        val response = withContext(Dispatchers.IO) {
          Network.zooMatchApi.getPetById("Bearer $token", petId)
        }
        if (response.isSuccessful) {
          val pet = response.body() ?: return@launch
          val owner = pet.owner

          binding.ownerName.text = listOfNotNull(
            owner.firstname, owner.lastname
          ).joinToString(" ").ifBlank { "Владелец" }

          val bio = owner.description
          if (!bio.isNullOrBlank()) {
            binding.ownerBio.text = bio
            binding.ownerBio.visibility = View.VISIBLE
          } else {
            binding.ownerBio.visibility = View.GONE
          }

          val avatarUrl = owner.avatar
          if (!avatarUrl.isNullOrBlank()) {
            val fullUrl = if (avatarUrl.startsWith("http")) avatarUrl
            else "https://zoomatch.ru$avatarUrl"
            Glide.with(this@OwnerProfileActivity)
              .load(fullUrl)
              .placeholder(R.drawable.test_avatar)
              .error(R.drawable.test_avatar)
              .centerCrop()
              .into(binding.ownerAvatar)
          }

          binding.petsCount.text = owner.pets?.size?.toString() ?: "0"

          val petShortList = owner.pets?.map { short ->
            PetShortRecommendation(
              id = short.id,
              name = short.name,
              isMale = short.is_male,
              age = short.age,
              avatar = short.avatar,
              isActive = short.is_active,
              animal_type = short.animal_type,
              breed = short.breed,
              distanceKm = short.distance_km,
              location = short.location,
              animalTypeCustom = short.animal_type_custom,
              breedCustom = short.breed_custom,
              moderationStatus = short.moderation_status,
              owner = null
            )
          } ?: emptyList()

          if (petShortList.isNotEmpty()) {
            val adapter = OwnerPetAdapter()
            binding.ownerPetsRecyclerView.layoutManager =
              GridLayoutManager(this@OwnerProfileActivity, 2)
            binding.ownerPetsRecyclerView.adapter = adapter
            adapter.submitList(petShortList)
          }
        }
      } catch (_: Exception) {
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    _binding = null
  }
}

class OwnerPetAdapter :
  ListAdapter<PetShortRecommendation, OwnerPetViewHolder>(OwnerPetDiff) {

  object OwnerPetDiff : DiffUtil.ItemCallback<PetShortRecommendation>() {
    override fun areItemsTheSame(old: PetShortRecommendation, new: PetShortRecommendation) =
      old.id == new.id
    override fun areContentsTheSame(old: PetShortRecommendation, new: PetShortRecommendation) =
      old == new
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OwnerPetViewHolder {
    val bind = ItemPetSmallBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return OwnerPetViewHolder(bind)
  }

  override fun onBindViewHolder(holder: OwnerPetViewHolder, position: Int) {
    holder.bind(getItem(position))
  }
}

class OwnerPetViewHolder(private val binding: ItemPetSmallBinding) :
  RecyclerView.ViewHolder(binding.root) {

  fun bind(pet: PetShortRecommendation) {
    binding.petName.text = pet.name
    val genderIcon = if (pet.isMale) "\u2642" else "\u2640"
    binding.petInfo.text = "${pet.age} $genderIcon"

    val avatarUrl = pet.avatar
    if (!avatarUrl.isNullOrBlank()) {
      val fullUrl = if (avatarUrl.startsWith("http")) avatarUrl
      else "https://zoomatch.ru$avatarUrl"
      Glide.with(binding.root.context)
        .load(fullUrl)
        .placeholder(R.drawable.ic_paw_print_light)
        .error(R.drawable.ic_paw_print_light)
        .centerCrop()
        .into(binding.petPhoto)
    } else {
      binding.petPhoto.setImageResource(R.drawable.ic_paw_print_light)
    }
  }
}
