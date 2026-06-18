package com.example.zoomatch.ui.homeScreen.search.Fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.zoomatch.R
import com.example.zoomatch.data.homeScreen.search.MatchPetData
import com.example.zoomatch.data.homeScreen.search.MatchResponse
import com.example.zoomatch.databinding.FragmentRequestBinding
import com.example.zoomatch.databinding.ItemMatchBinding
import com.example.zoomatch.ui.homeScreen.HomeViewModelFactory
import com.example.zoomatch.ui.homeScreen.search.OwnerProfileActivity
import com.example.zoomatch.ui.homeScreen.search.PetCardDialogFragment
import kotlinx.coroutines.launch

class RequestFragment : Fragment() {
  private var _binding: FragmentRequestBinding? = null
  private val binding get() = _binding!!
  private val viewModel: RequestViewModel by viewModels { HomeViewModelFactory(requireActivity().application) }
  private lateinit var adapter: MatchAdapter

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentRequestBinding.inflate(inflater, container, false)
    adapter = MatchAdapter(
      onAccept = { matchId -> viewModel.acceptMatch(matchId) },
      onReject = { matchId -> viewModel.rejectMatch(matchId) },
      onViewPet = { petFrom, petTo ->
        val dialog = PetCardDialogFragment.fromMatchPetData(petFrom)
        dialog.show(parentFragmentManager, "pet_card")
      },
      onViewOwner = { petId ->
        val intent = Intent(requireContext(), OwnerProfileActivity::class.java)
        intent.putExtra("PET_ID", petId)
        startActivity(intent)
      }
    )

    binding.requestsRecyclerView.apply {
      layoutManager = LinearLayoutManager(requireContext())
      adapter = this@RequestFragment.adapter
    }
    return binding.root
  }

  @SuppressLint("UnsafeRepeatOnLifecycleDetector")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.load()
    viewLifecycleOwner.lifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch {
          viewModel.matches.collect { list ->
            adapter.submitList(list)
          }
        }
        launch {
          viewModel.error.collect { error ->
            error?.let {
              Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
          }
        }
      }
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}

class MatchAdapter(
  private val onAccept: (Int) -> Unit,
  private val onReject: (Int) -> Unit,
  private val onViewPet: (MatchPetData, MatchPetData?) -> Unit,
  private val onViewOwner: (Int) -> Unit
) : ListAdapter<MatchResponse, MatchViewHolder>(Diff) {

  object Diff : DiffUtil.ItemCallback<MatchResponse>() {
    override fun areItemsTheSame(old: MatchResponse, new: MatchResponse) = old.id == new.id
    override fun areContentsTheSame(old: MatchResponse, new: MatchResponse) = old == new
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
    val bind = ItemMatchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return MatchViewHolder(bind, onAccept, onReject, onViewPet, onViewOwner)
  }

  override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
    holder.bind(getItem(position))
  }
}

class MatchViewHolder(
  private val binding: ItemMatchBinding,
  private val onAccept: (Int) -> Unit,
  private val onReject: (Int) -> Unit,
  private val onViewPet: (MatchPetData, MatchPetData?) -> Unit,
  private val onViewOwner: (Int) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

  fun bind(match: MatchResponse) {
    val petFrom = match.pet_from_data
    val petTo = match.pet_to_data

    bindPetPhoto(binding.petFromPhoto, petFrom?.avatar)
    binding.petFromName.text = petFrom?.name ?: "—"
    binding.petFromInfo.text = formatPetInfo(petFrom)

    bindPetPhoto(binding.petToPhoto, petTo?.avatar)
    binding.petToName.text = petTo?.name ?: "—"
    binding.petToInfo.text = formatPetInfo(petTo)

    when (match.status) {
      0 -> {
        binding.statusText.text = "ОЖИДАНИЕ"
        binding.statusText.setTextColor(Color.parseColor("#F59E0B"))
        binding.statusBadge.setBackgroundColor(Color.parseColor("#FEF3C7"))
        binding.buttonsRow.visibility = View.VISIBLE
      }
      1 -> {
        binding.statusText.text = "ПРИНЯТ"
        binding.statusText.setTextColor(Color.parseColor("#10B981"))
        binding.statusBadge.setBackgroundColor(Color.parseColor("#D1FAE5"))
        binding.buttonsRow.visibility = View.GONE
      }
      2 -> {
        binding.statusText.text = "ОТКЛОНЁН"
        binding.statusText.setTextColor(Color.parseColor("#EF4444"))
        binding.statusBadge.setBackgroundColor(Color.parseColor("#FEE2E2"))
        binding.buttonsRow.visibility = View.GONE
      }
      else -> {
        binding.statusText.text = ""
        binding.statusBadge.visibility = View.GONE
        binding.buttonsRow.visibility = View.GONE
      }
    }

    binding.btnViewPet.setOnClickListener {
      petFrom?.let { onViewPet(it, petTo) }
    }

    binding.btnViewOwner.setOnClickListener {
      petFrom?.let { onViewOwner(it.id) }
    }

    binding.btnAccept.setOnClickListener {
      onAccept(match.id)
    }

    binding.btnReject.setOnClickListener {
      onReject(match.id)
    }
  }

  private fun formatPetInfo(pet: MatchPetData?): String {
    if (pet == null) return ""
    val genderIcon = if (pet.is_male) "\u2642" else "\u2640"
    return "${pet.age} $genderIcon"
  }

  private fun bindPetPhoto(imageView: android.widget.ImageView, avatar: String?) {
    if (!avatar.isNullOrBlank()) {
      val fullUrl = if (avatar.startsWith("http")) avatar
      else "https://zoomatch.ru$avatar"
      Glide.with(imageView.context)
        .load(fullUrl)
        .placeholder(R.drawable.test_avatar)
        .error(R.drawable.test_avatar)
        .centerCrop()
        .into(imageView)
    } else {
      imageView.setImageResource(R.drawable.test_avatar)
    }
  }
}
