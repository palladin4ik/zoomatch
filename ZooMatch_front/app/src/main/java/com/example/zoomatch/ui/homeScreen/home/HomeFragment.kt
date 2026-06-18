package com.example.zoomatch.ui.homeScreen.home

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.zoomatch.R
import com.example.zoomatch.databinding.HomeFragmentHomeBinding
import com.example.zoomatch.ui.homeScreen.pets.EditPetActivity
import com.example.zoomatch.ui.homeScreen.settings.SettingsActivity
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

  private var _binding: HomeFragmentHomeBinding? = null
  private val binding get() = _binding!!
  private val viewModel: HomeViewModel by viewModels()

  private var currentPeriod = "day"

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = HomeFragmentHomeBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupSettingsIcon()
    setupAddPetButton()
    setupFilterTabs()
    setupChart()
    observeViewModel()
  }

  private fun setupSettingsIcon() {
    binding.settingsIcon.setOnClickListener {
      startActivity(Intent(requireContext(), SettingsActivity::class.java))
    }
  }

  private fun setupAddPetButton() {
    binding.boostProfileButton.setOnClickListener {
      val activeCount = viewModel.activePetCount.value
      if (activeCount > 0) {
        requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
          R.id.nav_view
        ).selectedItemId = R.id.navigation_search
      } else {
        startActivity(Intent(requireContext(), EditPetActivity::class.java))
      }
    }
  }

  private fun setupFilterTabs() {
    binding.activityFilterGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
      if (isChecked) {
        currentPeriod = when (checkedId) {
          R.id.tabDay -> "day"
          R.id.tabWeek -> "week"
          R.id.tabMonth -> "month"
          else -> "day"
        }
        updateChartFromViewModel()
      }
    }
  }

  private fun updateChartFromViewModel() {
    val period = viewModel.getChartPeriod(currentPeriod)
    updateChart(period.labels, period.values)
  }

  private fun setupChart() {
    val chart = binding.barChart
    chart.description.isEnabled = false
    chart.legend.isEnabled = false
    chart.setFitBars(true)
    chart.setDrawGridBackground(false)
    chart.setDrawBarShadow(false)
    chart.setTouchEnabled(false)
    chart.setScaleEnabled(false)
    chart.setPinchZoom(false)

    val xAxis = chart.xAxis
    xAxis.position = XAxis.XAxisPosition.BOTTOM
    xAxis.setDrawGridLines(false)
    xAxis.granularity = 1f
    xAxis.textColor = Color.GRAY
    xAxis.textSize = 10f

    chart.axisLeft.setDrawGridLines(true)
    chart.axisLeft.gridColor = Color.parseColor("#F0F0F0")
    chart.axisLeft.axisMinimum = 0f
    chart.axisLeft.textColor = Color.GRAY
    chart.axisLeft.textSize = 10f

    chart.axisRight.isEnabled = false

    updateChartFromViewModel()
  }

  private fun updateChart(labels: List<String>, values: List<Float>) {
    val entries = values.mapIndexed { index, value ->
      BarEntry(index.toFloat(), value)
    }

    val dataSet = BarDataSet(entries, "Совпадения").apply {
      color = Color.parseColor("#7C3AED")
      setDrawValues(false)
    }

    binding.barChart.data = BarData(dataSet)
    binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
    binding.barChart.xAxis.labelCount = labels.size
    binding.barChart.invalidate()
  }

  private fun petDeclension(count: Int): String {
    val absCount = kotlin.math.abs(count)
    val mod100 = absCount % 100
    val mod10 = absCount % 10
    return when {
      mod100 in 11..19 -> "питомцев"
      mod10 == 1 -> "питомец"
      mod10 in 2..4 -> "питомца"
      else -> "питомцев"
    }
  }

  private fun moderationDeclension(count: Int): String {
    val absCount = kotlin.math.abs(count)
    val mod100 = absCount % 100
    val mod10 = absCount % 10
    return when {
      mod100 in 11..19 -> "питомцев"
      mod10 == 1 -> "питомец"
      mod10 in 2..4 -> "питомца"
      else -> "питомцев"
    }
  }

  private fun observeViewModel() {
    viewLifecycleOwner.lifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch {
          viewModel.userName.collect { name ->
            binding.greetingText.text = "Привет, $name!"
          }
        }
        launch {
          combine(viewModel.petCount, viewModel.activePetCount) { total, active ->
            total to active
          }.collect { (totalCount, activeCount) ->
            when {
              totalCount == 0 -> {
                binding.recPetName.text = "Добавьте питомца"
                binding.recPetStatus.text = "Начало"
                binding.recPetDescription.text = "Добавьте своего первого питомца, чтобы начать поиск идеальной пары."
                binding.boostProfileButton.text = "Добавить питомца"
                binding.boostProfileButton.isEnabled = true
              }
              activeCount == 0 -> {
                val pendingCount = totalCount
                val pendingWord = moderationDeclension(pendingCount)
                binding.recPetName.text = "Питомцы на модерации"
                binding.recPetStatus.text = "Ожидание"
                binding.recPetDescription.text = "Ваш${if (pendingCount == 1) "" else "и"} $pendingCount $pendingWord проходят модерацию. После одобрения вы сможете начать поиск."
                binding.boostProfileButton.text = "Ожидание модерации"
                binding.boostProfileButton.isEnabled = false
              }
              else -> {
                val petWord = petDeclension(activeCount)
                binding.recPetName.text = "Найдите пару"
                binding.recPetStatus.text = "Поиск"
                binding.recPetDescription.text = "У вас $activeCount $petWord. Начните поиск идеальной пары!"
                binding.boostProfileButton.text = "Начать поиск"
                binding.boostProfileButton.isEnabled = true
              }
            }
          }
        }
        launch {
          viewModel.matchCount.collect { count ->
            binding.matchesCount.text = count.toString()
          }
        }
        launch {
          viewModel.chatCount.collect { count ->
            binding.newChatsCount.text = count.toString()
          }
        }
        launch {
          viewModel.viewCount.collect { count ->
            binding.viewsCount.text = count.toString()
          }
        }
        launch {
          viewModel.ratingCount.collect { count ->
            binding.ratingCount.text = count.toString()
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
