package uii.ang.ivisitor.base.data.ktor

open class CallFailure(val errorModel: ErrorModel) : Throwable()

fun getCallFailure(result: BaseResponseObj): CallFailure {
  return CallFailure(
    ErrorModel(
      code = result.error,
      errorMessage = "ApiServer error"
    )
  )
}