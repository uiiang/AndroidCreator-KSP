package uii.ang.creator.template.showcase

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import uii.ang.creator.annotation.requestMethodGet
import uii.ang.creator.annotation.requestMethodPost
import uii.ang.creator.annotation.requestParamTypePath
import uii.ang.creator.processor.Const
import uii.ang.creator.processor.Const.appendPathSegments
import uii.ang.creator.processor.Const.baseKtorPackageName
import uii.ang.creator.processor.Const.baseRemoteResponseClassName
import uii.ang.creator.processor.Const.intClassName
import uii.ang.creator.processor.Const.koinSingleOfMemberName
import uii.ang.creator.processor.Const.ktorBody
import uii.ang.creator.processor.Const.ktorGet
import uii.ang.creator.processor.Const.ktorPost
import uii.ang.creator.processor.Const.ktorSetBody
import uii.ang.creator.processor.Const.stringClassName
import uii.ang.creator.processor.CreatorData
import uii.ang.creator.processor.ProcessorHelper
import uii.ang.creator.processor.PropertyDescriptor

class ApiServiceHelper(
  logger: KSPLogger,
  data: CreatorData
) : ProcessorHelper(logger, data) {
  fun genClassBuilder(): TypeSpec.Builder {
    val fetchUrl = "FETCH${
      data.annotationData.url.replace("/", "_")
        .replace(".", "_")
        .replace("?", "_")
        .replace("=", "_")
        .uppercase()
    }"
    val constructorParam = genConstructor()
    val property = convertProperty(fetchUrl)
    val fetchFunc = generateFetchFuncCode(fetchUrl)
    return TypeSpec.classBuilder(apiServiceClassName)
      .primaryConstructor(constructorParam.build())
      .addProperties(property)
      .addFunction(fetchFunc.build())
  }

  private fun genConstructor(): FunSpec.Builder {
    val flux = FunSpec.constructorBuilder()
    val paramSpec = ParameterSpec.builder(
      "client", Const.ktorHttpClient
    )
//    paramSpec.addModifiers(KModifier.PRIVATE)
    flux.addParameter(paramSpec.build())
    return flux
  }

  private fun convertProperty(fetchUrl: String): List<PropertySpec> {
    val retList: MutableList<PropertySpec> = mutableListOf()
    val prop = PropertySpec.builder(
      "client", Const.ktorHttpClient
    )
      .addModifiers(KModifier.PRIVATE)
    prop.initializer("client")
    retList.add(prop.build())
    val url = data.annotationData.url
    if (url.isNotEmpty()) {
      val urlProp = PropertySpec.builder(
        fetchUrl,
        stringClassName
      )
        .addModifiers(KModifier.PRIVATE)
      urlProp.initializer("\"${url}\"")
      retList.add(urlProp.build())
    }
    return retList
  }

  fun generateFetchFuncCode(fetchUrl: String): FunSpec.Builder {
    val anno = data.annotationData
    val methodName = anno.methodName
    val requestMethod = when (anno.method) {
      requestMethodPost -> ktorPost
      requestMethodGet -> ktorGet
      else -> ktorGet
    }
    val generateParameters = anno.parameters
    val genFunction = FunSpec.builder("invoke")
      .addModifiers(KModifier.SUSPEND, KModifier.OPERATOR)
    if (anno.isDynamicBaseUrl) {
      genFunction.addParameter(
        ParameterSpec
          .builder("serverUrl", stringClassName)
          .defaultValue("\"\"")
          .build()
      )
    }
    if (anno.method == requestMethodPost) {
      genFunction.addParameter(
        ParameterSpec
          .builder("bodyStr", stringClassName).build()
      )
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
    genFunction
      .returns(
//        baseRemoteResponseClassName
//          .parameterizedBy(data.sourceClassDeclaration.toClassName())
        data.sourceClassDeclaration.toClassName()
      )
//    return client.post(FETCH_S_API_GETBRANCH) {
    val serverUrlCode = if (anno.isDynamicBaseUrl) {
      "serverUrl + "
    } else ""
    val fetchCode = CodeBlock.builder()
      .addStatement("")
      .addStatement("return client.%M($serverUrlCode$fetchUrl) {", requestMethod)
    if (anno.method == requestMethodPost) {
      fetchCode.addStatement("\t%M(bodyStr)", ktorSetBody)
    }
    if (anno.method == requestMethodGet) {
      fetchCode.addStatement("\turl {")
      generateParameters.onEach { para ->
        when (para.paramQueryType) {
          requestParamTypePath-> {
            appendPathSegments
            fetchCode.addStatement("\t\t%M(\"${para.paramName}\", ${para.paramName})", appendPathSegments)
          }
          else -> {
            fetchCode.addStatement("\t\tparameters.append(\"${para.paramName}\", ${para.paramName})")
          }
        }
      }

      fetchCode.addStatement("\t}")
    }
    fetchCode.addStatement("}.%M()", ktorBody)
    genFunction.addCode(fetchCode.build())
    return genFunction
  }

  fun genKoinInjectionCode(): CodeBlock.Builder {
    return CodeBlock.builder().addStatement("\tsingle { %T(get()) }", apiServiceClassName)
  }
}













