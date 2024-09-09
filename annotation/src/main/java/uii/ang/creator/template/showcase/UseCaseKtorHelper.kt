package uii.ang.creator.template.showcase

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import uii.ang.creator.annotation.requestMethodGet
import uii.ang.creator.annotation.requestMethodPost
import uii.ang.creator.processor.Const.baseNetworkCallResultClassName
import uii.ang.creator.processor.Const.intClassName
import uii.ang.creator.processor.Const.koinNamedClassName
import uii.ang.creator.processor.Const.kotlinFlowFlowClassName
import uii.ang.creator.processor.Const.kotlinxCoroutineDispatcherClassName
import uii.ang.creator.processor.Const.kotlinxWithContextClassName
import uii.ang.creator.processor.Const.localCfgRepositoryClassName
import uii.ang.creator.processor.Const.pagingDataClassName
import uii.ang.creator.processor.Const.stringClassName
import uii.ang.creator.processor.CreatorData
import uii.ang.creator.processor.ProcessorHelper
import uii.ang.creator.processor.Utils.findParseReturnChain
import uii.ang.creator.processor.Utils.getGenerics
import uii.ang.creator.tools.firstCharLowerCase

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
    val anno = data.annotationData
    val flux = FunSpec.constructorBuilder()
    flux.addParameter(
      ParameterSpec.builder(
        "repository", repositoryInterfaceClassName
      ).build()
//    ).addParameter(
//      ParameterSpec.builder(
//        "localCfgRepository", localCfgRepositoryClassName
//      ).build()
    )
    if (anno.isDynamicBaseUrl) {
      flux.addParameter(ParameterSpec.builder("protocol", stringClassName).build())
        .addParameter(ParameterSpec.builder("url", stringClassName).build())
    }
    flux.addParameter(
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
    val typeSpec = TypeSpec.classBuilder(userCaseGenClassName)
//      .addTypeVariable(TypeVariableName("T"))
      .primaryConstructor(flux.build())
      .addProperty(repositoryProp.build())
//      .addProperty(localCfgRepositoryProp.build())
    if (anno.isDynamicBaseUrl) {
      typeSpec.addProperty(protocolProp.build())
      typeSpec.addProperty(urlProp.build())
    }
    typeSpec.addProperty(dispatcherProp.build())
      .addFunction(genUseCaseFunCode().build())
    return typeSpec
  }


  private fun genUseCaseFunCode(): FunSpec.Builder {
    val anno = data.annotationData
    val methodName = anno.methodName
    val generateParameters = anno.parameters
    val genFunction = FunSpec.builder("invoke")
      .addModifiers(KModifier.SUSPEND, KModifier.OPERATOR)

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
      }
    }

    val returnChain = findParseReturnChain(data.sourceClassDeclaration, logger)
    val funCodeBlock = CodeBlock.builder()
    val retCallResult = if (data.annotationData.isSupportPage) {
      val pageDataClassName = getGenerics(logger, data)
      pagingDataClassName
        .parameterizedBy(pageDataClassName)
    } else {
      if (returnChain.values.isNotEmpty()) {
        baseNetworkCallResultClassName
          .parameterizedBy(listOf(returnChain.values.last(), callFailureClassName))
      } else {
        baseNetworkCallResultClassName.parameterizedBy(
          listOf(
            data.sourceClassDeclaration.toClassName(),
            callFailureClassName
          )
        )
      }
    }

    funCodeBlock.addStatement("").addStatement("return %M(dispatcher) {", kotlinxWithContextClassName)
    val paramStr = when (anno.method) {
      requestMethodPost -> "body"
      else -> {
        generateParameters.joinToString(", ") { it.paramName }
      }
    }

    val serverUrlCode = if (anno.isDynamicBaseUrl) {
      "\"\$protocol://\$url\", "
    } else ""
    funCodeBlock.addStatement("\trepository($serverUrlCode$paramStr)")
    funCodeBlock.addStatement("}")
    genFunction.addCode(funCodeBlock.build())
    genFunction.returns(kotlinFlowFlowClassName.parameterizedBy(retCallResult))
    return genFunction
  }

  fun genKoinInjectionCode(): CodeBlock.Builder {
    logger.warn("准备生成基于 ${repositoryInterfaceClassName.simpleName.firstCharLowerCase()} 的usecase 的koin代码")
    val anno = data.annotationData
    return if (anno.isDynamicBaseUrl) {
      CodeBlock.builder().addStatement(
        "\tsingle { %T(get(), get(%M(\"HTTP_PROTOCOL\")), get(%M(\"HTTP_URL\")), get(%M(\"ioDispatcher\"))) }",
        userCaseGenClassName,
        koinNamedClassName, koinNamedClassName, koinNamedClassName
      )
    } else {
      CodeBlock.builder().addStatement(
        "\tsingle { %T(get(), get(%M(\"ioDispatcher\"))) }",
        userCaseGenClassName, koinNamedClassName
      )
    }

  }
}