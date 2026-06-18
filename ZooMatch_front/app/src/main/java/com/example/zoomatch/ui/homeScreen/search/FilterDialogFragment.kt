package com.example.zoomatch.ui.homeScreen.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.zoomatch.R
import com.example.zoomatch.data.homeScreen.search.SearchFilterParams
import com.example.zoomatch.databinding.FragmentFilterBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FilterDialogFragment : BottomSheetDialogFragment() {

  private var _binding: FragmentFilterBinding? = null
  private val binding get() = _binding!!

  private var onFilterApplied: ((SearchFilterParams) -> Unit)? = null

  fun setOnFilterApplied(listener: (SearchFilterParams) -> Unit) {
    onFilterApplied = listener
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentFilterBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.applyButton.setOnClickListener {
      val params = collectParams()
      onFilterApplied?.invoke(params)
      dismiss()
    }

    binding.resetButton.setOnClickListener {
      resetFields()
    }
  }

  private fun collectParams(): SearchFilterParams {
    val radiusKm = when (binding.radiusChipGroup.checkedChipId) {
      R.id.radius50 -> 50
      R.id.radius150 -> 150
      R.id.radius300 -> 300
      else -> null
    }

    val requiresPedigree = binding.pedigreeSwitch.isChecked

    val minAge = binding.minAgeField.text.toString().toIntOrNull()
    val maxAge = binding.maxAgeField.text.toString().toIntOrNull()
    val maxMonths = binding.matingMonthsField.text.toString().toIntOrNull()

    return SearchFilterParams(
      radiusKm = radiusKm,
      requiresPedigree = requiresPedigree,
      minAge = minAge,
      maxAge = maxAge,
      maxMonthsSinceMating = maxMonths
    )
  }

  private fun resetFields() {
    binding.radiusChipGroup.check(R.id.radiusAny)
    binding.pedigreeSwitch.isChecked = false
    binding.minAgeField.text?.clear()
    binding.maxAgeField.text?.clear()
    binding.matingMonthsField.text?.clear()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  companion object {
    fun newInstance(): FilterDialogFragment = FilterDialogFragment()
  }
}
