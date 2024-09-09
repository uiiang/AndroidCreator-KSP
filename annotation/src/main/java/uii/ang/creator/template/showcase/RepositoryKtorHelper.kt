package uii.ang.creator.template.showcase

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import uii.ang.creator.annotation.requestMethodGet
import uii.ang.creator.annotation.requestMethodPost
import uii.ang.creator.processor.Const.baseNetworkCallResultClassName
import uii.ang.creator.processor.Const.intClassName
import uii.ang.creator.processor.Const.kotlinFlowFlowClassName
import uii.ang.creator.processor.Const.pagingDataClassName
import uii.ang.creator.processor.Const.stringClassName
import uii.ang.creator.processor.CreatorData
import uii.ang.creator.processor.ProcessorHelper
import uii.ang.creator.processor.Utils.findParseReturnChain
import uii.ang.creator.processor.Utils.getGenerics
import uii.ang.creator.processor.Utils.getRequestParamWithoutBody
import uii.ang.creator.processor.Utils.requestParamHasBody
import uii.ang.creator.processor.Utils.requestParamHasMap

class RepositoryKtorHelper(
  logger: KSPLogger,
  data: CreatorData
) : ProcessorHelper(logger, data) {

  fun genClassBuilder(): TypeSpec.Builder {
    val classBuilder = TypeSpec.interfaceBuilder(repositoryInterfaceClassName)
//      .addModifiers(KModifier.INTERNAL)
    val repositoryFuncCode = genRepositoryFuncCode()
    classBuilder.addFunction(repositoryFuncCode.build())
    return classBuilder
  }

  fun genRepositoryFuncCode(): FunSpec.Builder {
    val anno = data.annotationData
    val methodName = anno.methodName
    val generateParameters = anno.parameters
    // 生成如下代码
//   interface MoviesRepository {
//    suspend fun fetchPopularMovies(lang: String):
//      Flow<NetworkCallResult<List<MovieModel>, CallFailure>>
//    }
    val noBodyParamList = getRequestParamWithoutBody(generateParameters)
    val hasBody = requestParamHasBody(generateParameters)
    val hasMap = requestParamHasMap(generateParameters)

    val genFunction = FunSpec.builder("invoke")
      .addModifiers(KModifier.ABSTRACT, KModifier.SUSPEND, KModifier.OPERATOR)

    if (anno.isDynamicBaseUrl) {
      genFunction
        .addParameter(
          ParameterSpec
            .builder("serverUrl", stringClassName)
            .defaultValue("\"\"")
            .build()
        )
    }

    if (anno.method == requestMethodPost) {
      genFunction.addParameter(ParameterSpec.builder("body", requestBodyClassName).build())
    }
    if (anno.method == requestMethodGet) {
      generateParameters.onEach { para ->
        val paramTypeClassName = when (para.paramType) {
          "String" -> stringClassName
          "Int" -> intClassName
          else -> stringClassName
        }
        genFunction.addParameter(
          ParameterSpec.builder(para.paramName, paramTypeClassName).build()
        )
//        if (data.annotationData.isSupportPage) {
//          genFunction.addParameter(
//            ParameterSpec.builder(data.annotationData.pageParamName, stringClassName).build()
//          )
//        }
      }
    }

    val returnChain = findParseReturnChain(data.sourceClassDeclaration, logger)
    val retCallResult = if (returnChain.values.isNotEmpty()) {
      baseNetworkCallResultClassName
        .parameterizedBy(listOf(returnChain.values.last(), callFailureClassName))
    } else {
      baseNetworkCallResultClassName
        .parameterizedBy(listOf(data.sourceClassDeclaration.toClassName(), callFailureClassName))
    }
    if (data.annotationData.isSupportPage) {
      val pageDataClassName = getGenerics(logger, data)
      genFunction.returns(kotlinFlowFlowClassName
        .parameterizedBy(pagingDataClassName
          .parameterizedBy(pageDataClassName)))
//      genFunction.returns(kotlinFlowFlowClassName.parameterizedBy(pagingDataClassName.parameterizedBy(returnChain.values.last())))
    } else {
      genFunction.returns(kotlinFlowFlowClassName.parameterizedBy(retCallResult))
    }
    return genFunction
  }
}