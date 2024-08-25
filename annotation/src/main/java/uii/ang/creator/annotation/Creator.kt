package uii.ang.creator.annotation

enum class AnnotationRetrofitService {
  POST, GET
}

const val requestMethodPost = "POST"
const val requestMethodGet = "GET"

const val apiTypeKtor = "ktor"
const val apiTypeRetrofit = "retrofit"

const val requestParamTypePath = "Path"
const val requestParamTypeQuery = "Query"
const val requestParamTypeField = "Field"
const val requestParamTypeBody = "Body"
const val requestParamTypeMap = "Map"


const val requestParamDataTypeString = "String"
const val requestParamDataTypeInt = "Int"


@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Creator(
  /**
   * 网络框架使用ktor/retrofit
   */
  val generateApiType: String,
  /**
   * 是否生成apiService类
   */
  val generateApiService: Boolean = false,
  /**
   * 是否生成apiModel类
   */
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
   * 是否生成EntityModel类
   */
  val generatorEntityModel: Boolean = false,
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
  val methodName: String = "",
  /**
   * 返回的Response类名，会自动被ApiResult包裹
   */
  val returnResponseClassName: String = "",

  /**
   * 发送请求中包含的参数
   */
  val parameters: Array<Parameter> = [],

  /**
   * 数据对象的基类的package和类名import地址
   */
  val baseObjClassName: String = "",
  /**
   * 判断返回数据是否错误的方法import地址
   */
  val checkResponseSuccessFuncPath: String = "",
  /**
   * 包装错误类的方法import地址
   */
  val getCallFailureFuncPath: String = "",
)


@Retention(AnnotationRetention.SOURCE)
annotation class Parameter(
  val paramName: String,
  val paramType: String = requestParamDataTypeString,
  val paramDefault: String = "",
  val paramQueryType: String = requestParamTypePath
) {
  companion object
}
