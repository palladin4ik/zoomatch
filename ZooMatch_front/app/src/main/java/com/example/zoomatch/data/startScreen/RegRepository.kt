package com.example.zoomatch.data.startScreen


class RegRepository(val dataSource: RegDataSource) {
  var user: RegUser? = null
    private set

  init {
    user = null
  }

  fun register(email: String, password: String, username: String): Result<RegUserView> {
    val result = dataSource.register(email, password, username)
    return when (result) {
      is Result.Success -> {
        setRegUser(result.data)
        Result.Success(RegUserView(result.data.displayName))
      }
      is Result.Error -> Result.Error(result.exception)
    }
  }

  private fun setRegUser(regUser: RegUser) {
    this.user = regUser
  }
}