package uii.ang.creator.template.showcase

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import uii.ang.creator.annotation.*
import uii.ang.creator.processor.Const.baseRetrofitApiResultClassName
import uii.ang.creator.processor.Const.hashMapClassName
import uii.ang.creator.processor.Const.requestBodyPackageName
import uii.ang.creator.processor.Const.retrofitBodyClassName
import uii.ang.creator.processor.Const.retrofitClassName
import uii.ang.creator.processor.Const.retrofitFieldClassName
import uii.ang.creator.processor.Const.retrofitFormUrlEncodedClassName
import uii.ang.creator.processor.Const.retrofitGetClassName
import uii.ang.creator.processor.Const.retrofitPathClassName
import uii.ang.creator.processor.Const.retrofitPostClassName
import uii.ang.creator.processor.Const.retrofitQueryClassName
import uii.ang.creator.processor.CreatorData
import uii.ang.creator.processor.ProcessorHelper
import uii.ang.creator.processor.Utils.convertType
import uii.ang.creator.processor.Utils.getRequestParamWithoutBody
import uii.ang.creator.processor.Utils.getRequestParameterSpecBodyWithMap
import uii.ang.creator.processor.Utils.getRetrofitClassName
import uii.ang.creator.processor.Utils.requestParamHasBody
import uii.ang.creator.processor.Utils.requestParamHasMap
import uii.ang.creator.tools.firstCharLowerCase
import uii.ang.creator.tools.firstCharUpperCase

class RetrofitServiceHelper(
  logger: KSPLogger,
  data: CreatorData
) : ProcessorHelper(logger, data) {

  fun genClassBuilder(): TypeSpec.Builder {
    val classBuilder = TypeSpec.interfaceBuilder(retrofitServiceClassName)
      .addModifiers(KModifier.INTERNAL)

    return classBuilder
  }

  fun genRetrofitServiceFuncCode(): FunSpec.Builder {

    val anno = data.annotationData
//    var responseClassName = anno.responseClassName
    val method = anno.method
    val url = anno.url
    val methodName = data.annotationData.methodName
    val generateParameters = anno.parameters

    // 生成如下代码
    //        @POST("./?method=album.search")
    //    suspend fun searchAlbumAsync(
    //        @Query("album") phrase: String?,
    //        @Query("limit") limit: Int = 60,
    //    ): ApiResult<SearchAlbumResponse>
    // KotlinPoet class builder
//    val classBuilder = TypeSpec.interfaceBuilder(retrofitServiceClassName)
//      .addModifiers(KModifier.INTERNAL)

    val genFunction = FunSpec.builder(methodName + "Async")
      .addModifiers(KModifier.ABSTRACT, KModifier.SUSPEND)

    getRequestParamWithoutBody(generateParameters).forEach { param ->
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
    // 如果有使用body注解的参数，方法的最后一个参数为 @Body paramMap: [methodName]QueryBody
    val hasBody = requestParamHasBody(generateParameters)
    val hasMap = requestParamHasMap(generateParameters)
    if (hasBody) {
      val queryObjName = "${methodName}QueryBody"
      val bodyParamSpec =
        ParameterSpec.builder("paramData", ClassName(requestBodyPackageName, queryObjName.firstCharUpperCase()))
          .addAnnotation(
            AnnotationSpec.builder(getRetrofitClassName(requestParamTypeBody))
//              .addMember("\"${queryObjName.firstCharLowerCase()}\"")
              .build()
          )
      genFunction.addParameter(bodyParamSpec.build())
    }
    if (hasMap) {
      val bodyParamSpec =
        getRequestParameterSpecBodyWithMap()
          .addAnnotation(
            AnnotationSpec.builder(getRetrofitClassName(requestParamTypeBody))
//              .addMember("\"${queryObjName.firstCharLowerCase()}\"")
              .build()
          )
      genFunction.addParameter(bodyParamSpec.build())
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


  fun genKoinInjectionCode(): CodeBlock.Builder {
    //  single { get<Retrofit>().create(AlbumRetrofitService::class.java) }
    return CodeBlock.builder()
      .addStatement("\tsingle { get<%T>().create(%T::class.java) }", retrofitClassName, retrofitServiceClassName)
  }
}