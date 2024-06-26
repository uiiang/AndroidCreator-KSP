package uii.ang.creator.template.showcase

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import uii.ang.creator.processor.CreatorData
import uii.ang.creator.processor.ProcessorHelper

class RetrofitServiceHelper(
  logger: KSPLogger,
  data: CreatorData
) : ProcessorHelper(logger, data) {

  fun genClassBuilder(): TypeSpec.Builder {
//    val apiModelClassName = apiModelClassName
//    val responseClassNameStr = responseClassName.simpleName
    val classBuilder = TypeSpec.interfaceBuilder(retrofitServiceClassName)
      .addModifiers(KModifier.INTERNAL)

    return classBuilder
  }

  fun genRetrofitServiceFuncCode(): FunSpec.Builder {

    val anno = data.annotationData
//    var responseClassName = anno.responseClassName
    var method = anno.method
    var url = anno.url
    var methodName = data.annotationData.methodName
    var generateParameters = anno.parameters
//    var requestParams: Array<String> = []

    // generateParameters.forEach {
    //   logger.warn("generaterParameters name ${it.paramName} type ${it.paramType}")
    // }
    // logger.warn(
    //   "packageName $responsePackageName" +
    //           " className ${classDeclaration.simpleName.asString()}"
    // )
    // 生成如下代码
    //        @POST("./?method=album.search")
    //    suspend fun searchAlbumAsync(
    //        @Query("album") phrase: String?,
    //        @Query("limit") limit: Int = 60,
    //    ): ApiResult<SearchAlbumResponse>
    // KotlinPoet class builder
//    val classBuilder = TypeSpec.interfaceBuilder(retrofitServiceClassName)
//      .addModifiers(KModifier.INTERNAL)

    val genFunction = FunSpec.builder(methodName+"Async")
      .addModifiers(KModifier.ABSTRACT, KModifier.SUSPEND)
    generateParameters.forEach { param ->
      val paramSpec = ParameterSpec.builder(param.paramName, convertType(param.paramType))
        .addAnnotation(
          AnnotationSpec
            .builder(getRetrofitClassName(param.paramQueryType))
            .addMember("\"${param.paramName}\"")
            .build()
        )
      if (param.paramDefault.isNotEmpty()) {
//          paramSpec.defaultValue("\"${param.paramDefault}\"${convertToType(param.paramType)}")
        if (param.paramType == "String") {
          paramSpec.defaultValue("\"${param.paramDefault}\"")
        } else {
          paramSpec.defaultValue(param.paramDefault)
        }
      }
      genFunction.addParameter(
        paramSpec.build()
      )
    }
    if (generateParameters.map { it.paramQueryType }.contains("Field")) {
      genFunction.addAnnotation(retrofitFormUrlEncodedClassName)
    }
    genFunction.addAnnotation(
      AnnotationSpec.builder(getRetrofitClassName(method))
        .addMember("\"$url\"").build()
    )
    genFunction.returns(baseRetrofitApiResultClassName.parameterizedBy(responseClassName))
//    classBuilder.addFunction(genFunction.build())
//    return FileSpec.builder(retrofitServicePackageName, retrofitServiceClassName)
//      .addType(classBuilder.build())
    return genFunction
  }

  private fun convertType(type: String) = when (type) {
    "String" -> String::class
    "Long" -> Long::class
    "Int" -> Int::class
    "Double" -> Double::class
    "Float" -> Float::class
    "Short" -> Short::class
    "Boolean" -> Boolean::class
    else -> String::class
  }

  private fun convertToType(type: String) = when (type) {
    "String" -> ""
    "Long" -> ".toLong()"
    "Int" -> ".toInt()"
    "Double" -> ".toDouble()"
    "Float" -> ".toFloat()"
    "Short" -> ".toShort()"
    "Boolean" -> ".Boolean()"
    else -> ""
  }

  private fun getRetrofitClassName(method: String) = when (method) {
    "POST" -> retrofitPostClassName
    "GET" -> retrofitGetClassName
    "Query" -> retrofitQueryClassName
    "Path" -> retrofitPostClassName
    "Field" -> retrofitFieldClassName
    else -> retrofitGetClassName
  }
}