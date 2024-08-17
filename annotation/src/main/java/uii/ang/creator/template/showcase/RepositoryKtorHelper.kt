package uii.ang.creator.template.showcase

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import uii.ang.creator.codegen.CodeBuilder
import uii.ang.creator.processor.Const.baseCallFailureClassName
import uii.ang.creator.processor.Const.baseDomainResultClassName
import uii.ang.creator.processor.Const.baseNetworkCallResultClassName
import uii.ang.creator.processor.Const.kotlinFlowFlowClassName
import uii.ang.creator.processor.Const.kotlinFlowFlowMemberName
import uii.ang.creator.processor.CreatorData
import uii.ang.creator.processor.ProcessorHelper
import uii.ang.creator.processor.Utils.findParseReturnChain
import uii.ang.creator.processor.Utils.getRequestParamWithoutBody
import uii.ang.creator.processor.Utils.getRequestParameterSpecBody
import uii.ang.creator.processor.Utils.getRequestParameterSpecBodyWithMap
import uii.ang.creator.processor.Utils.getRequestParameterSpecList
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
//      .addTypeVariable(TypeVariableName("T"))
//    val parameterSpecList = getRequestParameterSpecList(noBodyParamList, true)
//    genFunction.addParameters(parameterSpecList)
    genFunction.addParameter(ParameterSpec.builder("body", requestBodyClassName).build())

//    if (hasBody) {
//      val bodyParamSpec = getRequestParameterSpecBody(methodName)
//      genFunction.addParameter(bodyParamSpec.build())
//    }
//    if (hasMap) {
//      val bodyParamSpec = getRequestParameterSpecBodyWithMap()
//      genFunction.addParameter(bodyParamSpec.build())
//    }
    val returnChain = findParseReturnChain(data.sourceClassDeclaration, logger)
    if (returnChain.values.isNotEmpty()) {
      val retCallResult = baseNetworkCallResultClassName
        .parameterizedBy(listOf(returnChain.values.last(), baseCallFailureClassName))
      genFunction.returns(kotlinFlowFlowClassName.parameterizedBy(retCallResult))
    }
    return genFunction
  }
}