package com.example.zoomatch.ui.homeScreen.search

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.zoomatch.R
import com.example.zoomatch.data.homeScreen.search.MatchPetData

class PetCardDialogFragment : DialogFragment() {

  companion object {
    private const val ARG_PET_NAME = "pet_name"
    private const val ARG_PET_AGE = "pet_age"
    private const val ARG_PET_IS_MALE = "pet_is_male"
    private const val ARG_PET_AVATAR = "pet_avatar"
    private const val ARG_PET_TYPE = "pet_type"
    private const val ARG_PET_BREED = "pet_breed"
    private const val ARG_PET_DISTANCE = "pet_distance"
    private const val ARG_OWNER_NAME = "owner_name"
    private const val ARG_OWNER_AVATAR = "owner_avatar"
    private const val ARG_PET_ID = "pet_id"
    private const val ARG_OWNER_PET_ID = "owner_pet_id"

    fun fromMatchPetData(pet: MatchPetData, distanceKm: Double? = null): PetCardDialogFragment {
      val fragment = PetCardDialogFragment()
      val args = Bundle().apply {
        putString(ARG_PET_NAME, pet.name)
        putInt(ARG_PET_AGE, pet.age)
        putBoolean(ARG_PET_IS_MALE, pet.is_male)
        putString(ARG_PET_AVATAR, pet.avatar)
        putString(ARG_PET_TYPE, pet.animal_type?.name)
        putString(ARG_PET_BREED, pet.breed?.name)
        if (distanceKm != null) putDouble(ARG_PET_DISTANCE, distanceKm)
        putString(ARG_OWNER_NAME, listOfNotNull(pet.owner?.firstname, pet.owner?.lastname).joinToString(" ").ifBlank { null })
        putString(ARG_OWNER_AVATAR, pet.owner?.avatar)
        putInt(ARG_PET_ID, pet.id)
        putInt(ARG_OWNER_PET_ID, pet.id)
      }
      fragment.arguments = args
      return fragment
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStyle(STYLE_NO_FRAME, R.style.FullScreenDialog)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return inflater.inflate(R.layout.fragment_pet_card_overlay, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val args = arguments ?: return

    val petName = args.getString(ARG_PET_NAME, "")
    val petAge = args.getInt(ARG_PET_AGE, 0)
    val petIsMale = args.getBoolean(ARG_PET_IS_MALE, false)
    val petAvatar = args.getString(ARG_PET_AVATAR)
    val petType = args.getString(ARG_PET_TYPE)
    val petBreed = args.getString(ARG_PET_BREED)
    val distance = if (args.containsKey(ARG_PET_DISTANCE)) args.getDouble(ARG_PET_DISTANCE) else null
    val ownerName = args.getString(ARG_OWNER_NAME)
    val ownerAvatar = args.getString(ARG_OWNER_AVATAR)
    val petId = args.getInt(ARG_PET_ID, -1)

    val genderIcon = if (petIsMale) "\u2642" else "\u2640"
    view.findViewById<TextView>(R.id.petName).text = "$petName, $petAge ${pluralYears(petAge)} $genderIcon"

    val typeBreed = listOfNotNull(petType, petBreed).joinToString(", ")
    val typeBreedView = view.findViewById<TextView>(R.id.petTypeBreed)
    if (typeBreed.isNotBlank()) {
      typeBreedView.text = typeBreed
      typeBreedView.visibility = View.VISIBLE
    } else {
      typeBreedView.visibility = View.GONE
    }

    val distanceView = view.findViewById<TextView>(R.id.petDistance)
    if (distance != null) {
      distanceView.text = "${distance.toInt()} км от вас"
      distanceView.visibility = View.VISIBLE
    } else {
      distanceView.visibility = View.GONE
    }

    val petPhoto = view.findViewById<ImageView>(R.id.petPhoto)
    if (!petAvatar.isNullOrBlank()) {
      val fullUrl = if (petAvatar.startsWith("http")) petAvatar
      else "https://zoomatch.ru$petAvatar"
      Glide.with(this)
        .load(fullUrl)
        .placeholder(R.drawable.test_avatar)
        .error(R.drawable.test_avatar)
        .centerCrop()
        .into(petPhoto)
    }

    val ownerNameView = view.findViewById<TextView>(R.id.ownerName)
    ownerNameView.text = ownerName ?: "Владелец"

    val ownerAvatarView = view.findViewById<ImageView>(R.id.ownerAvatar)
    if (!ownerAvatar.isNullOrBlank()) {
      val fullUrl = if (ownerAvatar.startsWith("http")) ownerAvatar
      else "https://zoomatch.ru$ownerAvatar"
      Glide.with(this)
        .load(fullUrl)
        .placeholder(R.drawable.test_avatar)
        .error(R.drawable.test_avatar)
        .centerCrop()
        .into(ownerAvatarView)
    }

    view.findViewById<ImageView>(R.id.btnViewFullOwner).setOnClickListener {
      if (petId != -1) {
        val intent = Intent(requireContext(), OwnerProfileActivity::class.java)
        intent.putExtra("PET_ID", petId)
        startActivity(intent)
      }
      dismiss()
    }

    view.findViewById<View>(R.id.petCard).setOnClickListener { }

    view.setOnClickListener { dismiss() }
  }

  private fun pluralYears(age: Int): String = when {
    age % 10 == 1 && age % 100 != 11 -> "год"
    age % 10 in 2..4 && age % 100 !in 12..14 -> "года"
    else -> "лет"
  }
}
