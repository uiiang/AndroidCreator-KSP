package uii.ang.creator.template.showcase

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import uii.ang.creator.processor.Const.baseDomainResultClassName
import uii.ang.creator.processor.Const.baseDomainResultMemberName
import uii.ang.creator.processor.Const.koinSingleOfMemberName
import uii.ang.creator.processor.Const.requestBodyPackageName
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
//      .addModifiers(KModifier.INTERNAL)
      .primaryConstructor(flux.build())
      .addProperty(prop.build())
  }

  fun genUseCaseFunCode(): FunSpec.Builder {
    val anno = data.annotationData
    val methodName = anno.methodName
    val generateParameters = anno.parameters
    val genFunction = FunSpec.builder("invoke")
      .addModifiers(KModifier.SUSPEND, KModifier.OPERATOR)
    val parameterSpecList = getRequestParameterSpecList(generateParameters, true)
    genFunction.addParameters(parameterSpecList)

    val returnChain = findParseReturnChain(data.sourceClassDeclaration, logger)

    if (returnChain.values.isNotEmpty()) {
      val returnParam = baseDomainResultClassName.parameterizedBy(returnChain.values.last())
      PropertySpec.builder("result", returnParam).build()
      val funCodeBlock = CodeBlock.builder()

      val hasBody = requestParamHasBody(generateParameters)
      val hasMap = requestParamHasMap(generateParameters)
      val noBodyParamList = getRequestParamWithoutBody(generateParameters)
      var paramList = noBodyParamList.joinToString(", ") { param ->
        param.paramName
      }
      if (hasBody) {
        val bodyParamList = getRequestParamWithBody(generateParameters)
          .joinToString(",") {
//          if (it.paramType=="String") {
//            "\n${it.paramName} = \"${it.paramName}\""
//          } else {
            "\n${it.paramName} = ${it.paramName}"
//          }
          }
        val queryBodyClassName = "${methodName}QueryBody"
        val queryBodyCodeBlock = CodeBlock.builder()
          .addStatement(
            "val ${queryBodyClassName.firstCharLowerCase()} = %T(",
            ClassName(requestBodyPackageName, queryBodyClassName.firstCharUpperCase())
          )
          .addStatement(bodyParamList)
          .addStatement(")")
        genFunction.addCode(queryBodyCodeBlock.build())
        if (paramList.isNotEmpty()) {
          paramList += ", "
        }
        paramList += queryBodyClassName
        funCodeBlock.addStatement(
          "%T.d(\"$queryBodyClassName params=\${$queryBodyClassName.toString()}\")",
          timberClassName
        )
      }

      if (hasMap) {
        val bodyParamList = getRequestParamWithMap(generateParameters)
        val queryMapCodeBlock = CodeBlock.builder()
        queryMapCodeBlock.addStatement("val paramMap = %T()", getRequestHashMapClassName())
        bodyParamList.forEach { param->
          queryMapCodeBlock.addStatement("paramMap[\"${param.paramName}\"] = ${param.paramName}")
        }
        funCodeBlock.add(queryMapCodeBlock.build())
        if (paramList.isNotEmpty()) {
          paramList += ", "
        }
        paramList += "paramMap"
      }

      funCodeBlock.addStatement("val result = ${repositoryInterfaceClassName.simpleName.firstCharLowerCase()}")
        .addStatement("\t.$methodName($paramList)")
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