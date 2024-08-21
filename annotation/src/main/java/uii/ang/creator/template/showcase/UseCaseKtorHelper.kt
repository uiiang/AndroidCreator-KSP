package uii.ang.creator.template.showcase

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import uii.ang.creator.processor.Const.baseCallFailureClassName
import uii.ang.creator.processor.Const.baseNetworkCallResultClassName
import uii.ang.creator.processor.Const.koinNamedClassName
import uii.ang.creator.processor.Const.koinSingleOfMemberName
import uii.ang.creator.processor.Const.kotlinFlowFlowClassName
import uii.ang.creator.processor.Const.kotlinxCoroutineDispatcherClassName
import uii.ang.creator.processor.Const.kotlinxWithContextClassName
import uii.ang.creator.processor.Const.localCfgRepositoryClassName
import uii.ang.creator.processor.Const.requestBodyPackageName
import uii.ang.creator.processor.Const.stringClassName
import uii.ang.creator.processor.Const.timberClassName
import uii.ang.creator.processor.CreatorData
import uii.ang.creator.processor.ProcessorHelper
import uii.ang.creator.processor.Utils.findParseReturnChain
import uii.ang.creator.processor.Utils.getRequestHashMapClassName
import uii.ang.creator.processor.Utils.getRequestParamWithBody
import uii.ang.creator.processor.Utils.getRequestParamWithMap
import uii.ang.creator.processor.Utils.getRequestParamWithoutBody
import uii.ang.creator.processor.Utils.getRequestParameterSpecList
import uii.ang.creator.processor.Utils.requestParamHasBody
import uii.ang.creator.processor.Utils.requestParamHasMap
import uii.ang.creator.tools.firstCharLowerCase
import uii.ang.creator.tools.firstCharUpperCase

class UseCaseKtorHelper(
  logger: KSPLogger,
  data: CreatorData
) : ProcessorHelper(logger, data) {
  fun genClassBuilder(): TypeSpec.Builder {
    val repositoryInterfaceName = repositoryInterfaceClassName.simpleName.firstCharLowerCase()
    logger.warn(
      "准备生成基于 ${repositoryInterfaceClassName.simpleName.firstCharLowerCase()} 的UseCase" +
              " 类名 ${userCaseClassName.simpleName} 生成于方法名 ${data.annotationData.methodName}"
    )
    val flux = FunSpec.constructorBuilder()
    flux.addParameter(
      ParameterSpec.builder(
        "repository", repositoryInterfaceClassName
      ).build()
//    ).addParameter(
//      ParameterSpec.builder(
//        "localCfgRepository", localCfgRepositoryClassName
//      ).build()
    ).addParameter(
      ParameterSpec.builder(
        "protocol", stringClassName
      ).build()
    ).addParameter(
      ParameterSpec.builder(
        "url", stringClassName
      ).build()
    )
      .addParameter(
        ParameterSpec.builder(
          "dispatcher",
          kotlinxCoroutineDispatcherClassName
        ).build()
      ).build()
    val repositoryProp = PropertySpec.builder(
      "repository",
      repositoryInterfaceClassName
    ).addModifiers(KModifier.PRIVATE)
//      .addTypeVariable(TypeVariableName("T"))
      .initializer("repository")

    val localCfgRepositoryProp = PropertySpec.builder(
      "localCfgRepository", localCfgRepositoryClassName
    ).addModifiers(KModifier.PRIVATE).initializer("localCfgRepository")
    val protocolProp = PropertySpec.builder(
      "protocol", stringClassName
    ).addModifiers(KModifier.PRIVATE).initializer("protocol")
    val urlProp = PropertySpec.builder(
      "url", stringClassName
    ).addModifiers(KModifier.PRIVATE).initializer("url")
    val dispatcherProp = PropertySpec.builder(
      "dispatcher",
      kotlinxCoroutineDispatcherClassName
    ).addModifiers(KModifier.PRIVATE).initializer("dispatcher")
    return TypeSpec.classBuilder(userCaseGenClassName)
//      .addTypeVariable(TypeVariableName("T"))
      .primaryConstructor(flux.build())
      .addProperty(repositoryProp.build())
//      .addProperty(localCfgRepositoryProp.build())
      .addProperty(protocolProp.build())
      .addProperty(urlProp.build())
      .addProperty(dispatcherProp.build())
      .addFunction(genUseCaseFunCode().build())
  }


  fun genUseCaseFunCode(): FunSpec.Builder {
    val anno = data.annotationData
    val methodName = anno.methodName
    val generateParameters = anno.parameters
    val genFunction = FunSpec.builder("invoke")
      .addModifiers(KModifier.SUSPEND, KModifier.OPERATOR)
//      .addTypeVariable(TypeVariableName("T"))
//    val parameterSpecList = getRequestParameterSpecList(generateParameters, true)
//    genFunction.addParameters(parameterSpecList)
//    genFunction.addParameter(ParameterSpec.builder("url", stringClassName).build())
    genFunction.addParameter(ParameterSpec.builder("body", requestBodyClassName).build())

    val returnChain = findParseReturnChain(data.sourceClassDeclaration, logger)
    val funCodeBlock = CodeBlock.builder()
    val retCallResult = if (returnChain.values.isNotEmpty()) {
//      val repositoryInterfaceName = repositoryInterfaceClassName.simpleName.firstCharLowerCase()

//      val url = "${localCfgRepository.getServerProtocol()}://${localCfgRepository.getBaseServerUrl()}"
//      funCodeBlock.addStatement("val url = \"\${localCfgRepository.getServerProtocol()}://\${localCfgRepository.getBaseServerUrl()}\"")
//      funCodeBlock.addStatement("val url = \"\${protocol}://\${url}\"")
//      val hasBody = requestParamHasBody(generateParameters)
//      val hasMap = requestParamHasMap(generateParameters)
//      val noBodyParamList = getRequestParamWithoutBody(generateParameters)
//      var paramList = noBodyParamList.joinToString(", ") { param ->
//        param.paramName
//      }
//      if (hasBody) {
//        val bodyParamList = getRequestParamWithBody(generateParameters)
//          .joinToString(",") {
////          if (it.paramType=="String") {
////            "\n${it.paramName} = \"${it.paramName}\""
////          } else {
//            "\n${it.paramName} = ${it.paramName}"
////          }
//          }
//        val queryBodyClassName = "${methodName}QueryBody"
//        val queryBodyCodeBlock = CodeBlock.builder()
//          .addStatement(
//            "val ${queryBodyClassName.firstCharLowerCase()} = %T(",
//            ClassName(requestBodyPackageName, queryBodyClassName.firstCharUpperCase())
//          )
//          .addStatement(bodyParamList)
//          .addStatement(")")
//        genFunction.addCode(queryBodyCodeBlock.build())
//        if (paramList.isNotEmpty()) {
//          paramList += ", "
//        }
//        paramList += queryBodyClassName
//        funCodeBlock.addStatement(
//          "%T.d(\"$queryBodyClassName params=\${$queryBodyClassName.toString()}\")",
//          timberClassName
//        )
//      }

//      if (hasMap) {
//        val bodyParamList = getRequestParamWithMap(generateParameters)
//        val queryMapCodeBlock = CodeBlock.builder()
//        queryMapCodeBlock.addStatement("val paramMap = %T()", getRequestHashMapClassName())
//        bodyParamList.forEach { param ->
//          queryMapCodeBlock.addStatement("paramMap[\"${param.paramName}\"] = ${param.paramName}")
//        }
//        funCodeBlock.add(queryMapCodeBlock.build())
//      if (paramList.isNotEmpty()) {
//        paramList += ", "
//      }
//      paramList += "paramMap"

      baseNetworkCallResultClassName
        .parameterizedBy(listOf(returnChain.values.last(), baseCallFailureClassName))

//      }
    } else {
      baseNetworkCallResultClassName.parameterizedBy(
        listOf(
          data.sourceClassDeclaration.toClassName(),
          baseCallFailureClassName
        )
      )
    }

    funCodeBlock.addStatement("").addStatement("return %M(dispatcher) {", kotlinxWithContextClassName)
      .addStatement("\trepository(\"\$protocol://\$url\", body)")
      .addStatement("}")
    genFunction.addCode(funCodeBlock.build())
    genFunction.returns(kotlinFlowFlowClassName.parameterizedBy(retCallResult))
    return genFunction
  }

  fun genKoinInjectionCode(): CodeBlock.Builder {
    return CodeBlock.builder().addStatement(
      "\tsingle { %T(get(), get(%M(\"HTTP_PROTOCOL\")), get(%M(\"HTTP_URL\")), get(%M(\"ioDispatcher\"))) }",
      userCaseGenClassName,
      koinNamedClassName, koinNamedClassName, koinNamedClassName
    )
  }
}