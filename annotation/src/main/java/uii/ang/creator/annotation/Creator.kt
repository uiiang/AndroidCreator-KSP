package uii.ang.creator.annotation

enum class AnnotationRetrofitService {
  POST, GET
}

const val requestMethodPost = "POST"
const val requestMethodGet = "GET"


@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Creator(
  // 是否生成apiModel类
  val generateApiModel: Boolean = false,
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

  /**
   * 发送请求中包含的参数
   */
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
