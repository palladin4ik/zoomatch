package com.example.zoomatch.ui.homeScreen.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.zoomatch.R
import com.example.zoomatch.data.db.AppDatabase
import com.example.zoomatch.data.db.UserDao
import com.example.zoomatch.data.db.UserEntity
import com.example.zoomatch.data.homeScreen.home.RecType
import com.example.zoomatch.data.homeScreen.home.Recommendation
import com.example.zoomatch.databinding.HomeFragmentHomeBinding
import com.example.zoomatch.databinding.ItemRecommendationBinding
import com.example.zoomatch.ui.homeScreen.pets.EditPetActivity
import com.example.zoomatch.ui.homeScreen.profile.EditProfileActivity
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

  private var _binding: HomeFragmentHomeBinding? = null
  private val binding get() = _binding!!
  private lateinit var recAdapter: RecommendationAdapter
  private val userDao: UserDao by lazy { AppDatabase.getDatabase(requireContext()).userDao() }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

    _binding = HomeFragmentHomeBinding.inflate(inflater, container, false)
    binding.activityFilterGroup.check(binding.tabDay.id)

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    recAdapter = RecommendationAdapter(emptyList()) { type ->
      when (type) {
        RecType.PROFILE_INCOMPLETE -> startActivity(
          Intent(
            requireContext(),
            EditProfileActivity::class.java
          )
        )

        RecType.NO_PETS -> startActivity(Intent(requireContext(), EditPetActivity::class.java))
      }
    }
    binding.recommendationsRecyclerView.adapter = recAdapter
    binding.recommendationsRecyclerView.layoutManager =
      LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

    observeRecommendations()
  }

  private fun observeRecommendations() {
    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        combine(
          userDao.getCurrentUserFlow(),
          userDao.getPetCountForCurrentUser()
        ) { user, petCount -> user to petCount }
          .collect { (user, petCount) ->
            val recs = mutableListOf<Recommendation>()
            if (user == null) {
              recAdapter = RecommendationAdapter(emptyList()) {}
              binding.recommendationsRecyclerView.adapter = recAdapter
              return@collect
            }
            if (!isProfileComplete(user)) {
              recs += Recommendation(
                id = "profile",
                name = "Завершите профиль",
                photoUrl = null,
                btnText = "Заполнить",
                type = RecType.PROFILE_INCOMPLETE
              )
            }

            if (petCount == 0) {
              recs += Recommendation(
                id = "pet",
                name = "Добавьте питомца",
                photoUrl = null,
                btnText = "Добавить",
                type = RecType.NO_PETS
              )
            }

            recAdapter = RecommendationAdapter(recs) { type ->
              when (type) {
                RecType.PROFILE_INCOMPLETE -> startActivity(
                  Intent(
                    requireContext(),
                    EditProfileActivity::class.java
                  )
                )

                RecType.NO_PETS -> startActivity(
                  Intent(
                    requireContext(),
                    EditPetActivity::class.java
                  )
                )
              }
            }
            binding.recommendationsRecyclerView.adapter = recAdapter
          }
      }
    }
  }

  private fun isProfileComplete(user: UserEntity): Boolean {
    return !user.name.isBlank() &&
        !user.email.isBlank() &&
        !user.location?.isBlank()!! &&
        !user.phone_number?.isBlank()!! &&
        user.avatar != null &&
        !user.status?.isBlank()!!
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}


class RecommendationAdapter(
  private val items: List<Recommendation>,
  private val onAction: (RecType) -> Unit
) : RecyclerView.Adapter<RecommendationAdapter.ViewHolder>() {

  class ViewHolder(private val binding: ItemRecommendationBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: Recommendation, onAction: (RecType) -> Unit) {
      binding.recName.text = item.name
      Glide.with(binding.root.context)
        .load(item.photoUrl ?: R.drawable.test_avatar)
        .placeholder(R.drawable.test_avatar)
        .error(R.drawable.test_avatar)
        .centerCrop()
        .into(binding.recPhoto)
      binding.recButton.text = item.btnText
      binding.recButton.setOnClickListener { onAction(item.type) }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val binding =
      ItemRecommendationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return ViewHolder(binding)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(items[position], onAction)
  }

  override fun getItemCount() = items.size
}