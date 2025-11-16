package com.example.zoomatch.ui.homeScreen.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.zoomatch.R
import com.example.zoomatch.databinding.HomeFragmentHomeBinding
import com.example.zoomatch.databinding.ItemRecommendationBinding

class HomeFragment : Fragment() {

  private var _binding: HomeFragmentHomeBinding? = null
  private val binding get() = _binding!!

  private lateinit var recAdapter: RecommendationAdapter

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

    // Данные (заглушка)
    val recList = listOf(
      Recommendation("1", "Рекс", R.drawable.test_avatar.toString(), "Do it"),
      Recommendation("2", "Мурка", R.drawable.test_avatar.toString(), "Do it"),
      Recommendation("3", "Барсик", R.drawable.test_avatar.toString(), "Do it"),
      Recommendation("4", "Пушок", R.drawable.test_avatar.toString(), "Do it")
    )

    // Адаптер
    recAdapter = RecommendationAdapter(recList)
    binding.recommendationsRecyclerView.adapter = recAdapter
    binding.recommendationsRecyclerView.layoutManager =
      LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}

// Модель
data class Recommendation(val id: String, val name: String, val photoUrl: String?, val btntext: String)

// Адаптер
class RecommendationAdapter(private val items: List<Recommendation>) :
  RecyclerView.Adapter<RecommendationAdapter.ViewHolder>() {

  class ViewHolder(private val binding: ItemRecommendationBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: Recommendation) {
      binding.recName.text = item.name
      Glide.with(binding.root.context)
        .load(item.photoUrl ?: R.drawable.test_avatar)
        .placeholder(R.drawable.test_avatar)
        .error(R.drawable.test_avatar)
        .centerCrop()
        .into(binding.recPhoto)
      binding.recButton.text = item.btntext
      binding.recButton.setOnClickListener {  }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val binding =
      ItemRecommendationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return ViewHolder(binding)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(items[position])
  }

  override fun getItemCount() = items.size
}