package com.example.zoomatch.ui.homeScreen.search

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.zoomatch.R
import com.example.zoomatch.data.homeScreen.search.PetShortRecommendation
import com.example.zoomatch.databinding.ActivityMatchingBinding
import com.example.zoomatch.databinding.CardPetBinding
import com.example.zoomatch.ui.applySystemBarsPadding
import com.example.zoomatch.ui.homeScreen.HomeViewModelFactory
import kotlinx.coroutines.launch

class MatchingActivity : AppCompatActivity() {
  private var _binding: ActivityMatchingBinding? = null
  private val binding get() = _binding!!

  private val viewModel: MatchingViewModel by viewModels {
    HomeViewModelFactory(application)
  }

  private lateinit var cardStack: MutableList<View>
  private var currentIndex = 0
  private var lastDisplayedPetIds: List<Int> = emptyList()

  private var petFromId: Int = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    _binding = ActivityMatchingBinding.inflate(layoutInflater)
    setContentView(binding.root)
    binding.root.applySystemBarsPadding()

    cardStack = mutableListOf()

    petFromId = intent.getIntExtra("PET_ID", 0)

    binding.filterButton.setOnClickListener {
      showFilterDialog()
    }

    binding.dislikeButton.setOnClickListener { swipeCard(false) }
    binding.likeButton.setOnClickListener { swipeCard(true) }

    binding.infoButton.setOnClickListener {
      val currentPet = viewModel.pets.value.getOrNull(currentIndex) ?: return@setOnClickListener
      val intent = android.content.Intent(this, OwnerProfileActivity::class.java)
      intent.putExtra("PET_ID", currentPet.id)
      startActivity(intent)
    }

    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch {
          viewModel.pets.collect { pets ->
            if (pets.isNotEmpty()) {
              displayCards(pets)
            }
          }
        }
        launch {
          viewModel.isLoading.collect { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
          }
        }
        launch {
          viewModel.error.collect { error ->
            error?.let {
              Toast.makeText(this@MatchingActivity, it, Toast.LENGTH_LONG).show()
            }
          }
        }
      }
    }

    showFilterDialog()
  }

  private fun showFilterDialog() {
    val dialog = FilterDialogFragment.newInstance()
    dialog.setOnFilterApplied { params ->
      lastDisplayedPetIds = emptyList()
      viewModel.applyFilter(params, petFromId)
    }
    dialog.show(supportFragmentManager, "filter")
  }

  private fun displayCards(pets: List<PetShortRecommendation>) {
    val newIds = pets.map { it.id }
    if (newIds == lastDisplayedPetIds) return

    binding.cardContainer.removeAllViews()
    cardStack.clear()
    currentIndex = 0
    lastDisplayedPetIds = newIds

    val preloadCount = minOf(pets.size, 3)
    for (i in 0 until preloadCount) {
      val cardView = createPetCard(pets[i])
      if (i == 0) {
        binding.cardContainer.addView(cardView)
        cardStack.add(cardView)
      } else {
        cardView.visibility = View.INVISIBLE
        binding.cardContainer.addView(cardView)
        cardStack.add(cardView)
      }
    }
  }

  private fun createPetCard(pet: PetShortRecommendation): View {
    val cardBinding = CardPetBinding.inflate(layoutInflater)

    val typeName = pet.animal_type?.name ?: pet.animalTypeCustom ?: ""
    val breedName = pet.breed?.name ?: pet.breedCustom ?: ""
    val genderIcon = if (pet.isMale) "\u2642" else "\u2640"
    cardBinding.petName.text = "${pet.name}, ${pet.age} ${pluralYears(pet.age)} $genderIcon"

    val typeBreed = listOf(typeName, breedName).filter { it.isNotBlank() }
      .joinToString(", ")
    if (typeBreed.isNotBlank()) {
      cardBinding.petLocation.text = typeBreed
      cardBinding.petLocation.visibility = View.VISIBLE
    } else {
      cardBinding.petLocation.visibility = View.GONE
    }

    val distance = pet.distanceKm
    if (distance != null) {
      cardBinding.petDistance.text = "${distance.toInt()} км от вас"
      cardBinding.petDistance.visibility = View.VISIBLE
    } else {
      cardBinding.petDistance.visibility = View.GONE
    }

    val avatarUrl = pet.avatar
    if (!avatarUrl.isNullOrBlank()) {
      val fullUrl = if (avatarUrl.startsWith("http")) avatarUrl
      else "https://zoomatch.ru$avatarUrl"
      Glide.with(this@MatchingActivity)
        .load(fullUrl)
        .placeholder(R.drawable.test_avatar)
        .error(R.drawable.test_avatar)
        .centerCrop()
        .into(cardBinding.petPhoto)
    } else {
      cardBinding.petPhoto.setImageResource(R.drawable.test_avatar)
    }

    val owner = pet.owner
    if (owner != null) {
      val ownerFullName = listOfNotNull(owner.firstname, owner.lastname)
        .joinToString(" ").ifBlank { null }
      cardBinding.ownerName.text = ownerFullName ?: "Владелец"

      val ownerAvatarUrl = owner.avatar
      if (!ownerAvatarUrl.isNullOrBlank()) {
        val fullOwnerUrl = if (ownerAvatarUrl.startsWith("http")) ownerAvatarUrl
        else "https://zoomatch.ru$ownerAvatarUrl"
        Glide.with(this@MatchingActivity)
          .load(fullOwnerUrl)
          .placeholder(R.drawable.test_avatar)
          .error(R.drawable.test_avatar)
          .centerCrop()
          .into(cardBinding.ownerAvatar)
      }
    } else {
      cardBinding.ownerName.text = "Владелец"
    }

    return cardBinding.root
  }

  private fun swipeCard(isLike: Boolean) {
    if (cardStack.isEmpty()) return

    val petTo = viewModel.pets.value.getOrNull(currentIndex)?.id ?: return
    if (isLike) {
      viewModel.createMatch(petFromId, petTo)
    }

    val topCard = cardStack[0]
    val targetX = if (isLike) 1500f else -1500f
    val rotation = if (isLike) 30f else -30f

    topCard.animate()
      .translationX(targetX)
      .rotation(rotation)
      .setDuration(400)
      .withEndAction {
        binding.cardContainer.removeView(topCard)
        cardStack.removeAt(0)
        currentIndex++

        if (cardStack.isNotEmpty()) {
          val nextCard = cardStack[0]
          nextCard.visibility = View.VISIBLE
        }

        val nextPet = viewModel.getPetAt(currentIndex + 2)
        nextPet?.let {
          val newCard = createPetCard(it)
          newCard.visibility = View.INVISIBLE
          binding.cardContainer.addView(newCard)
          cardStack.add(newCard)
        }

        if (cardStack.isEmpty()) {
          Toast.makeText(this@MatchingActivity, "Это все доступные питомцы", Toast.LENGTH_SHORT).show()
          finish()
        }
      }
      .start()
  }

  private fun pluralYears(age: Int): String = when {
    age % 10 == 1 && age % 100 != 11 -> "год"
    age % 10 in 2..4 && age % 100 !in 12..14 -> "года"
    else -> "лет"
  }

  override fun onDestroy() {
    super.onDestroy()
    _binding = null
  }
}
