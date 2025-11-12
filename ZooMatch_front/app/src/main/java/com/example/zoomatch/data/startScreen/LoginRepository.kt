package com.example.zoomatch.data.startScreen

class LoginRepository(val dataSource: LoginDataSource) {
  var user: LoggedInUser? = null
    private set

  val isLoggedIn: Boolean
    get() = user != null

  init {
    user = null
  }

  fun login(email: String, password: String): Result<LoggedInUserView> {
    val result = dataSource.login(email, password)
    return when (result) {
      is Result.Success -> {
        setLoggedInUser(result.data)
        Result.Success(LoggedInUserView(result.data.displayName))
      }
      is Result.Error -> Result.Error(result.exception)
    }
  }

  private fun setLoggedInUser(loggedInUser: LoggedInUser) {
    this.user = loggedInUser
  }
}