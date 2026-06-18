package com.example.zoomatch.ui.homeScreen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zoomatch.data.db.UserDao
import com.example.zoomatch.data.db.ZooMatchApi
import com.example.zoomatch.data.homeScreen.chats.CurrentUserProvider
import com.example.zoomatch.data.homeScreen.search.MatchResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class ChartPeriod(
  val labels: List<String>,
  val values: List<Float>
)

@HiltViewModel
class HomeViewModel @Inject constructor(
  private val userDao: UserDao,
  private val api: ZooMatchApi,
  private val currentUserProvider: CurrentUserProvider
) : ViewModel() {

  val userName = userDao.getCurrentUserFlow()
    .map { user -> user?.firstname ?: "друг" }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

  val petCount = userDao.getPetCountForCurrentUser()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  val activePetCount = userDao.getActivePetCountForCurrentUser()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  private val _matchCount = MutableStateFlow(0)
  val matchCount: StateFlow<Int> = _matchCount

  private val _chatCount = MutableStateFlow(0)
  val chatCount: StateFlow<Int> = _chatCount

  private val _viewCount = MutableStateFlow(0)
  val viewCount: StateFlow<Int> = _viewCount

  private val _ratingCount = MutableStateFlow(0)
  val ratingCount: StateFlow<Int> = _ratingCount

  private var allMatches: List<MatchResponse> = emptyList()

  init {
    fetchStats()
  }

  fun fetchStats() {
    viewModelScope.launch {
      try {
        val token = currentUserProvider.getAccessToken() ?: return@launch

        val matchesResponse = api.getMatchesList("Bearer $token")
        if (matchesResponse.isSuccessful) {
          allMatches = matchesResponse.body() ?: emptyList()
          _matchCount.value = allMatches.size

          _viewCount.value = 0
          _ratingCount.value = 0
        }

        val chatsResponse = api.getChats("Bearer $token")
        if (chatsResponse.isSuccessful) {
          val chats = chatsResponse.body() ?: emptyList()
          _chatCount.value = chats.size
        }
      } catch (_: Exception) {
        _matchCount.value = 0
        _chatCount.value = 0
        _viewCount.value = 0
        _ratingCount.value = 0
      }
    }
  }

  fun getChartPeriod(period: String): ChartPeriod {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()

    val parsedMatches = allMatches.mapNotNull { match ->
      try {
        dateFormat.parse(match.created_at.substring(0, 10))
      } catch (_: Exception) {
        null
      }
    }

    return when (period) {
      "day" -> {
        val labels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
        val now = Calendar.getInstance()
        val dayOfWeek = now.get(Calendar.DAY_OF_WEEK)
        val startOfWeek = now.clone() as Calendar
        startOfWeek.add(Calendar.DAY_OF_YEAR, -(dayOfWeek - Calendar.MONDAY + 7) % 7)
        startOfWeek.set(Calendar.HOUR_OF_DAY, 0)
        startOfWeek.set(Calendar.MINUTE, 0)
        startOfWeek.set(Calendar.SECOND, 0)
        startOfWeek.set(Calendar.MILLISECOND, 0)

        val values = (0..6).map { i ->
          val targetDay = startOfWeek.clone() as Calendar
          targetDay.add(Calendar.DAY_OF_YEAR, i)
          val targetDayStart = targetDay.clone() as Calendar
          targetDayStart.set(Calendar.HOUR_OF_DAY, 0)
          targetDayStart.set(Calendar.MINUTE, 0)
          targetDayStart.set(Calendar.SECOND, 0)
          targetDayStart.set(Calendar.MILLISECOND, 0)
          val targetDayEnd = targetDay.clone() as Calendar
          targetDayEnd.set(Calendar.HOUR_OF_DAY, 23)
          targetDayEnd.set(Calendar.MINUTE, 59)
          targetDayEnd.set(Calendar.SECOND, 59)

          parsedMatches.count { date ->
            !date.before(targetDayStart.time) && !date.after(targetDayEnd.time)
          }.toFloat()
        }
        ChartPeriod(labels, values)
      }

      "week" -> {
        val now = Calendar.getInstance()
        val currentMonth = now.get(Calendar.MONTH)
        val currentYear = now.get(Calendar.YEAR)

        val startOfMonth = Calendar.getInstance()
        startOfMonth.set(currentYear, currentMonth, 1, 0, 0, 0)
        startOfMonth.set(Calendar.MILLISECOND, 0)

        val weeksInMonth = now.getActualMaximum(Calendar.WEEK_OF_MONTH)
        val labels = (1..weeksInMonth).map { "Нед $it" }

        val values = (0 until weeksInMonth).map { weekIndex ->
          val weekStart = startOfMonth.clone() as Calendar
          weekStart.add(Calendar.WEEK_OF_MONTH, weekIndex)
          weekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
          weekStart.set(Calendar.HOUR_OF_DAY, 0)
          weekStart.set(Calendar.MINUTE, 0)
          weekStart.set(Calendar.SECOND, 0)
          weekStart.set(Calendar.MILLISECOND, 0)

          val weekEnd = weekStart.clone() as Calendar
          weekEnd.add(Calendar.DAY_OF_YEAR, 6)
          weekEnd.set(Calendar.HOUR_OF_DAY, 23)
          weekEnd.set(Calendar.MINUTE, 59)
          weekEnd.set(Calendar.SECOND, 59)

          parsedMatches.count { date ->
            !date.before(weekStart.time) && !date.after(weekEnd.time)
          }.toFloat()
        }
        ChartPeriod(labels, values)
      }

      "month" -> {
        val labels = listOf(
          "Янв", "Фев", "Мар", "Апр", "Май", "Июн",
          "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек"
        )
        val now = Calendar.getInstance()
        val currentYear = now.get(Calendar.YEAR)

        val values = (0..11).map { month ->
          val monthStart = Calendar.getInstance()
          monthStart.set(currentYear, month, 1, 0, 0, 0)
          monthStart.set(Calendar.MILLISECOND, 0)

          val monthEnd = Calendar.getInstance()
          monthEnd.set(currentYear, month, monthStart.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)

          parsedMatches.count { date ->
            !date.before(monthStart.time) && !date.after(monthEnd.time)
          }.toFloat()
        }
        ChartPeriod(labels, values)
      }

      else -> ChartPeriod(emptyList(), emptyList())
    }
  }
}
