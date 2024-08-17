package uii.ang.base.data.ktor

data class NetworkCallResult<T, E>(val value: T? = null, val error: E? = null)