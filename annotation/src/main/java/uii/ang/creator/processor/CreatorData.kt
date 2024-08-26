package uii.ang.creator.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import uii.ang.creator.annotation.Creator
import uii.ang.creator.annotation.Parameter
import uii.ang.creator.annotation.apiTypeRetrofit
import uii.ang.creator.annotation.requestMethodGet
import uii.ang.creator.processor.Utils.convertKSValueParameterToPropertyDescriptor
import uii.ang.creator.tools.from

class CreatorData(
  val annotationData: AnnotationData,
  val sourceClassDeclaration: KSClassDeclaration,
  val logger: KSPLogger
) : AnnotatedBaseData {
  val generateApiType: String = annotationData.generateApiType
  val generateApiService: Boolean = annotationData.generateApiService
  val generateApiModel: Boolean = annotationData.generateApiModel
  val generateResponse: Boolean = annotationData.generateResponse
  val generateRetrofitService: Boolean = annotationData.generateRetrofitService
  val generateEntityModel: Boolean = annotationData.generatorEntityModel

  /**
   * 数据对象的基类的package和类名import地址
   */
  val baseObjClassName: String = annotationData.baseObjClassName

  /**
   * 判断返回数据是否错误的方法import地址
   */
  val checkResponseSuccessFuncPath: String = annotationData.checkResponseSuccessFuncPath

  /**
   * 包装错误类的方法import地址
   */
  val getCallFailureFuncPath: String = annotationData.getCallFailureFuncPath
  /**
   * 是否动态baseUrl
   *
   * 如果是动态baseUrl，在useCase,Repository,apiServer的方法中，会增加url的传入参数
   * 如果不是，不会传入参数，直接使用ktor配置的地址
   */
  val isDynamicBaseUrl: Boolean = false

  // 使用primaryConstructor来转换构造函数中的参数，可以获得给参数标注的注解
  val primaryConstructorParameters: List<PropertyDescriptor> =
    sourceClassDeclaration.primaryConstructor?.parameters?.map {
      val propertyDescriptor = convertKSValueParameterToPropertyDescriptor(it, sourceClassDeclaration, logger)
      logger.warn(propertyDescriptor.toString())
      propertyDescriptor
    }?.toList() ?: emptyList()

  data class AnnotationData(
    /**
     * 网络框架使用ktor/retrofit
     */
    val generateApiType: String,
    /**
     * 是否生成apiModel类
     */
    val generateApiModel: Boolean,
    /**
     * 是否生成apiService类
     */
    val generateApiService: Boolean,
    /**
    是否生成Response类
     */
    val generateResponse: Boolean,
    /**
     * 是否生成RetrofitService类
     */
    val generateRetrofitService: Boolean,
    /**
     * 是否生成EntityModel类
     */
    val generatorEntityModel: Boolean,
    /**
     * response类名
     */
    val responseClassName: String,
    /**
     * 指定retrofitService类名，被指定同一类名的方法会自动生成到一个类文件中
     */
    val retrofitServiceClassName: String,
    /**
     * url请求方法， POST, GET
     */
    val method: String = requestMethodGet,
    /**
     * 请求的url地址
     */
    val url: String,
    /**
     * 代码中的方法名
     */
    val methodName: String,
    /**
     * 返回的Response类名，会自动被ApiResult包裹
     */
    val returnResponseClassName: String,
    /**
     * 发送请求中包含的参数
     */
    val parameters: List<Parameter>,
    /**
     * 数据对象的基类的package和类名import地址
     */
    val baseObjClassName: String,
    /**
     * 判断返回数据是否错误的方法import地址
     */
    val checkResponseSuccessFuncPath: String,
    /**
     * 包装错误类的方法import地址
     */
    val getCallFailureFuncPath: String,
    /**
     * 是否动态baseUrl
     *
     * 如果是动态baseUrl，在useCase,Repository,apiServer的方法中，会增加url的传入参数
     * 如果不是，不会传入参数，直接使用ktor配置的地址
     */
    val isDynamicBaseUrl: Boolean = false,
  ) {
    companion object {
      fun from(annotation: KSAnnotation): AnnotationData {
        return AnnotationData(
          generateApiType = annotation.arguments.first { it.name?.asString() == Creator::generateApiType.name }.value as String,
          generateApiModel = annotation.arguments.first { it.name?.asString() == Creator::generateApiModel.name }.value as Boolean,
          generateApiService = annotation.arguments.first { it.name?.asString() == Creator::generateApiService.name }.value as Boolean,
          generateResponse = annotation.arguments.first { it.name?.asString() == Creator::generateResponse.name }.value as Boolean,
          generateRetrofitService = annotation.arguments.first { it.name?.asString() == Creator::generateRetrofitService.name }.value as Boolean,
          generatorEntityModel = annotation.arguments.first { it.name?.asString() == Creator::generatorEntityModel.name }.value as Boolean,
          responseClassName = annotation.arguments.first { it.name?.asString() == Creator::responseClassName.name }.value as String,
          retrofitServiceClassName = annotation.arguments.first { it.name?.asString() == Creator::retrofitServiceClassName.name }.value as String,
          method = annotation.arguments.first { it.name?.asString() == Creator::method.name }.value as String,
          methodName = annotation.arguments.first { it.name?.asString() == Creator::methodName.name }.value as String,
          url = annotation.arguments.first { it.name?.asString() == Creator::url.name }.value as String,
          returnResponseClassName = annotation.arguments.first { it.name?.asString() == Creator::returnResponseClassName.name }.value as String,
          parameters = (annotation.arguments.first { it.name?.asString() == Creator::parameters.name }.value as List<*>)
            .filterIsInstance<KSAnnotation>()
            .map { Parameter.from(it) },
          baseObjClassName = annotation.arguments.first { it.name?.asString() == Creator::baseObjClassName.name }.value as String,
          checkResponseSuccessFuncPath = annotation.arguments.first { it.name?.asString() == Creator::checkResponseSuccessFuncPath.name }.value as String,
          getCallFailureFuncPath = annotation.arguments.first { it.name?.asString() == Creator::getCallFailureFuncPath.name }.value as String,
          isDynamicBaseUrl = annotation.arguments.first { it.name?.asString() == Creator::isDynamicBaseUrl.name }.value as Boolean,

        )
      }
    }
  }
}