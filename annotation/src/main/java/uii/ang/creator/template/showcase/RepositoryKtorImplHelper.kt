package uii.ang.creator.template.showcase

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import uii.ang.creator.annotation.requestMethodGet
import uii.ang.creator.annotation.requestMethodPost
import uii.ang.creator.processor.Const.baseCallFailureClassName
import uii.ang.creator.processor.Const.baseErrorModelClassName
import uii.ang.creator.processor.Const.baseNetworkCallResultClassName
import uii.ang.creator.processor.Const.intClassName
import uii.ang.creator.processor.Const.koinNamedClassName
import uii.ang.creator.processor.Const.kotlinFlowCatchMemberName
import uii.ang.creator.processor.Const.kotlinFlowFlowClassName
import uii.ang.creator.processor.Const.kotlinFlowFlowMemberName
import uii.ang.creator.processor.Const.kotlinFlowFlowOnMemberName
import uii.ang.creator.processor.Const.kotlinxCoroutineDispatcherClassName
import uii.ang.creator.processor.Const.serialEncodeToStringMemberName
import uii.ang.creator.processor.Const.serializableJsonClassName
import uii.ang.creator.processor.Const.stringClassName
import uii.ang.creator.processor.CreatorData
import uii.ang.creator.processor.ProcessorHelper
import uii.ang.creator.processor.Utils.findParseReturnChain
import uii.ang.creator.processor.Utils.getRequestParamWithoutBody
import uii.ang.creator.processor.Utils.requestParamHasBody
import uii.ang.creator.processor.Utils.requestParamHasMap

class RepositoryKtorImplHelper(
  logger: KSPLogger,
  data: CreatorData
) : ProcessorHelper(logger, data) {
  fun genClassBuilder(): TypeSpec.Builder {
    val flux = FunSpec.constructorBuilder()
    flux.addParameter(
      ParameterSpec.builder("apiService", apiServiceClassName).build()
    ).addParameter(
      ParameterSpec.builder("dispatcher", kotlinxCoroutineDispatcherClassName).build()
    ).build()

    val apiServiceProp = PropertySpec.builder("apiService", apiServiceClassName)
      .addModifiers(KModifier.PRIVATE)
      .initializer("apiService")
    val kotlinxCoroutineDispatcherProp = PropertySpec.builder("dispatcher", kotlinxCoroutineDispatcherClassName)
      .addModifiers(KModifier.PRIVATE)
      .initializer("dispatcher")

    val classBuilder = TypeSpec.classBuilder(repositoryImplClassName)
//      .addTypeVariable(TypeVariableName("T"))
      .addSuperinterface(repositoryInterfaceClassName)
      .primaryConstructor(flux.build())
      .addProperty(apiServiceProp.build())
      .addProperty(kotlinxCoroutineDispatcherProp.build())
    classBuilder.addFunction(genRepositoryFuncCode().build())
    return classBuilder
  }

  fun genRepositoryFuncCode(): FunSpec.Builder {
    logger.warn("开始生成 RepositoryImpl方法")
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
      .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND, KModifier.OPERATOR)
//      .addTypeVariable(TypeVariableName("T"))
//    val parameterSpecList = getRequestParameterSpecList(noBodyParamList, true)
//    genFunction.addParameters(parameterSpecList)

    if (anno.isDynamicBaseUrl) {
      genFunction
        .addParameter(
          ParameterSpec
            .builder("serverUrl", stringClassName)
            .build()
        )
    }

    logger.warn("开始生成方法传入参数")
    if (anno.method == requestMethodPost) {
      logger.warn(" 当前为post请求，生成body")
      genFunction.addParameter(ParameterSpec.builder("body", requestBodyClassName).build())
    }
    if (anno.method == requestMethodGet) {
      logger.warn(" 当前为get请求，生成参数列表")
      generateParameters.onEach { para ->

        logger.warn("  ${para.paramName}: ${para.paramType}")
        val paramTypeClassName = when (para.paramType) {
          "String" -> stringClassName
          "Int" -> intClassName
          else -> stringClassName
        }
        genFunction.addParameter(
          ParameterSpec.builder(para.paramName, paramTypeClassName).build()
        )
      }
    }
//    if (hasBody) {
//      val bodyParamSpec = getRequestParameterSpecBody(methodName)
//      genFunction.addParameter(bodyParamSpec.build())
//    }
//    if (hasMap) {
//      val bodyParamSpec = getRequestParameterSpecBodyWithMap()
//      genFunction.addParameter(bodyParamSpec.build())
//    }
    val returnChain = findParseReturnChain(data.sourceClassDeclaration, logger)
    val retCallResult = if (returnChain.values.isNotEmpty()) {
      baseNetworkCallResultClassName
        .parameterizedBy(listOf(returnChain.values.last(), baseCallFailureClassName))
    } else {
      baseNetworkCallResultClassName.parameterizedBy(
        listOf(
          data.sourceClassDeclaration.toClassName(),
          baseCallFailureClassName
        )
      )
    }
    genFunction.returns(kotlinFlowFlowClassName.parameterizedBy(retCallResult))

//      Json { encodeDefaults = true }.encodeToString(body)
    val convertJsonCode = CodeBlock.builder()
    convertJsonCode.addStatement(
      "val jsonStr = %T { encodeDefaults = true }.%M(body)",
      serializableJsonClassName,
      serialEncodeToStringMemberName
    )
    val callApiService = CodeBlock.builder()

    val paramStr = when (anno.method) {
      requestMethodPost -> "jsonStr"
      else -> {
        generateParameters.joinToString(", ") { it.paramName }
      }
    }
    val serverUrlCode = if (anno.isDynamicBaseUrl) {
      "serverUrl, "
    }else ""
    callApiService.addStatement("\tval result = apiService($serverUrlCode$paramStr)")

    val toModelCode = CodeBlock.builder()
    returnChain.forEach { (t, u) ->
      toModelCode.add("\t\t\t\t\t.${t.getShortName()}")
    }
    val retCode = CodeBlock.builder()
      .addStatement("")
    retCode.addStatement("return %T {", kotlinFlowFlowMemberName.parameterizedBy(retCallResult))

    if (anno.method == requestMethodPost) {
      retCode.add(convertJsonCode.build())
    }
    retCode.add(callApiService.build())
      .addStatement("\tresult.let {")
      .addStatement("\t\tif (%M(it)) {", checkResponseSuccessFunc)
      .addStatement("\t\t\temit(")
      .addStatement("\t\t\t\t%T(", baseNetworkCallResultClassName)
      .addStatement("\t\t\t\t\tvalue = result")
      .add(toModelCode.build())
      .addStatement("")
      .addStatement("\t\t\t\t)")
      .addStatement("\t\t\t)")
      .addStatement("\t\t} else {")
//        .addStatement("} ?: run {")
      .addStatement("\t\t\temit(")
      .addStatement("\t\t\t\t%T(", baseNetworkCallResultClassName)
      .addStatement("\t\t\t\t\terror = %M(it)", getCallFailureFunc)
      .addStatement("\t\t\t\t)")
      .addStatement("\t\t\t)")
      .addStatement("\t\t}")
      .addStatement("\t}")
      .addStatement(
        "}.%M(dispatcher)",
        kotlinFlowFlowOnMemberName
      )
      .addStatement("\t.%M { e ->", kotlinFlowCatchMemberName)
      .addStatement("\t\temit(")
      .addStatement("\t\t\t%T(", baseNetworkCallResultClassName)
      .addStatement("\t\t\t\terror = %T(", baseCallFailureClassName)
      .addStatement("\t\t\t\t\t%T(", baseErrorModelClassName)
      .addStatement("\t\t\t\t\t\tcode = -1,")
      .addStatement("\t\t\t\t\t\terrorMessage = e.message ?: \"\"")
      .addStatement("\t\t\t\t\t)")
      .addStatement("\t\t\t\t)")
      .addStatement("\t\t\t)")
      .addStatement("\t\t)")
      .addStatement("}")
    genFunction.addCode(retCode.build())

    return genFunction
  }

  fun genKoinInjectionCode(): CodeBlock.Builder {
    return CodeBlock.builder()
      .addStatement(
        "\tsingle<%T> { %T(get(), get(%M(\"ioDispatcher\"))) }",
        repositoryInterfaceClassName,
        repositoryImplClassName,
        koinNamedClassName
      )
  }
}