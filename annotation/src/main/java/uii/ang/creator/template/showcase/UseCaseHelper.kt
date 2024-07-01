package uii.ang.creator.template.showcase

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import uii.ang.creator.processor.Const.baseDomainResultClassName
import uii.ang.creator.processor.Const.baseDomainResultMemberName
import uii.ang.creator.processor.Const.koinSingleOfMemberName
import uii.ang.creator.processor.CreatorData
import uii.ang.creator.processor.ProcessorHelper
import uii.ang.creator.processor.Utils.convertType
import uii.ang.creator.processor.Utils.findParseReturnChain
import uii.ang.creator.tools.firstCharLowerCase

class UseCaseHelper(
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
        repositoryInterfaceName, repositoryInterfaceClassName
      ).build()
    ).build()
    val prop = PropertySpec.builder(
      repositoryInterfaceName,
      repositoryInterfaceClassName
    ).addModifiers(KModifier.PRIVATE).initializer(repositoryInterfaceName)

    return TypeSpec.classBuilder(userCaseGenClassName)
      .addModifiers(KModifier.INTERNAL)
      .primaryConstructor(flux.build())
      .addProperty(prop.build())
  }

  fun genUseCaseFunCode(): FunSpec.Builder {
    val anno = data.annotationData
    val methodName = anno.methodName
    val generateParameters = anno.parameters
    val genFunction = FunSpec.builder("invoke")
      .addModifiers(KModifier.SUSPEND, KModifier.OPERATOR)

    generateParameters.forEach { param ->
      val paramSpec = ParameterSpec.builder(param.paramName, convertType(param.paramType))
      if (param.paramDefault.isNotEmpty()) {
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

    val returnChain = findParseReturnChain(data.sourceClassDeclaration, logger)

    if (returnChain.values.isNotEmpty()) {
      val returnParam = baseDomainResultClassName.parameterizedBy(returnChain.values.last())

      PropertySpec.builder("result", returnParam).build()
      val funCodeBlock = CodeBlock.builder()
        .addStatement("val result = ${repositoryInterfaceClassName.simpleName.firstCharLowerCase()}")
        .addStatement("\t.$methodName(${generateParameters.joinToString(",") { it.paramName }})")
        .addStatement("\t.%M {", baseDomainResultMemberName)
        .addStatement("\t\tcopy(value = value)")
        .addStatement("\t}")
        .addStatement("return result")
      genFunction.addCode(funCodeBlock.build())
      genFunction.returns(returnParam)
    }

    return genFunction
  }

  fun genKoinInjectionCode(): CodeBlock.Builder {
    return CodeBlock.builder().addStatement("\t%M(::%T)", koinSingleOfMemberName, userCaseGenClassName)
  }
}