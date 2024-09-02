package uii.ang.base.data.ktor

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
abstract class BaseResponseObj {
  @SerialName("RequestID")
  open val requestID: String = ""
  @SerialName("error")
  open val error: Long = 0L
}

fun checkResponseSuccess(result: BaseResponseObj): Boolean {
  return (result.error == 0L) ?:false
}