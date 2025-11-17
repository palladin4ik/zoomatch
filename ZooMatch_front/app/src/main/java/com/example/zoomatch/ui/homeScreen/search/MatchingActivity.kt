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
import com.example.zoomatch.R
import com.example.zoomatch.data.homeScreen.profile.ImageUtils
import com.example.zoomatch.data.homeScreen.search.PetLongResponse
import com.example.zoomatch.databinding.ActivityMatchingBinding
import com.example.zoomatch.databinding.CardPetBinding
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

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    _binding = ActivityMatchingBinding.inflate(layoutInflater)
    setContentView(binding.root)

    cardStack = mutableListOf()

    val petFrom = intent.getIntExtra("PET_ID", 0)
    val animalTypeId = intent.getIntExtra("ANIMAL_TYPE_ID", 0)
    viewModel.loadPets(animalTypeId)

    binding.dislikeButton.setOnClickListener { swipeCard(false, petFrom) }
    binding.likeButton.setOnClickListener { swipeCard(true, petFrom) }

    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.shuffledPets.collect { pets ->
          if (pets.isNotEmpty()) {
            displayCards(pets)
          }
        }
      }
    }
  }

  private fun displayCards(pets: List<PetLongResponse>) {
    binding.cardContainer.removeAllViews()
    cardStack.clear()
    currentIndex = 0

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

  private fun createPetCard(pet: PetLongResponse): View {
    val cardBinding = CardPetBinding.inflate(layoutInflater)

    cardBinding.petName.text = pet.name
    cardBinding.petInfo.text = "${pet.breed.name}, ${pet.age} ${pluralYears(pet.age)}"
    cardBinding.petLocation.text = pet.location ?: "Не указано"

    ImageUtils.base64ToBitmap(pet.avatar)?.let { bitmap ->
      cardBinding.petPhoto.setImageBitmap(bitmap)
    } ?: cardBinding.petPhoto.setImageResource(R.drawable.test_avatar)

    return cardBinding.root
  }

  private fun swipeCard(isLike: Boolean, petFrom: Int) {
    if (cardStack.isEmpty()) return

    val petTo = viewModel.shuffledPets.value[currentIndex].id
    if (isLike) {
      viewModel.createMatch(petFrom, petTo)
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