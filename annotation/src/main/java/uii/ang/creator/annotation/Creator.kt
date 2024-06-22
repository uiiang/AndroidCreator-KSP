package uii.ang.creator.annotation

import com.google.devtools.ksp.symbol.KSAnnotation

enum class AnnotationRetrofitService {
  POST, GET
}

const val requestMethodPost = "POST"
const val requestMethodGet = "GET"


@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Creator(
  // 是否生成apiModel类
  val generateApiModel: Boolean = true,
  /**
  是否生成Response类
   */
  val generateResponse: Boolean = false,
  /**
   * response类名
   */
  val responseClassName: String = "",
  /**
   * 是否生成RetrofitService类
   */
  val generateRetrofitService: Boolean = false,
  /**
   * 指定retrofitService类名，被指定同一类名的方法会自动生成到一个类文件中
   */
  val retrofitServiceClassName: String = "",
  /**
   * url请求方法， POST, GET
   */
  val method: String = requestMethodGet,
  /**
   * 请求的url地址
   */
  val url: String = "",
  /**
   * 方法名
   */
  val methodName: String = "request",
  /**
   * 返回的Response类名，会自动被ApiResult包裹
   */
  val returnResponseClassName: String = "",

  val parameters: Array<Parameter> = [],
)


@Retention(AnnotationRetention.SOURCE)
annotation class Parameter(
  val paramName: String,
  val paramType: String,
  val paramDefault: String="",
  val paramQueryType: String="Path",
) {
  companion object
}

fun Parameter.Companion.from(annotation: KSAnnotation) = Parameter(
  paramName = annotation.arguments.first { it.name?.asString() == Parameter::paramName.name }.value as String,
  paramType = annotation.arguments.first { it.name?.asString() == Parameter::paramType.name }.value as String,
  paramDefault = annotation.arguments.first { it.name?.asString() == Parameter::paramDefault.name }.value as String,
  paramQueryType =  annotation.arguments.first { it.name?.asString() == Parameter::paramQueryType.name }.value as String,
)
