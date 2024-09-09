package uii.ang.creator.template.showcase

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import uii.ang.creator.processor.Const.baseErrorModelClassName
import uii.ang.creator.processor.Const.domainModulePackageName
import uii.ang.creator.processor.Const.intClassName
import uii.ang.creator.processor.Const.loadParamsClassName
import uii.ang.creator.processor.Const.loadResultClassName
import uii.ang.creator.processor.Const.pagingSourceClassName
import uii.ang.creator.processor.Const.pagingStateClassName
import uii.ang.creator.processor.Const.stringClassName
import uii.ang.creator.processor.CreatorData
import uii.ang.creator.processor.ProcessorHelper
import uii.ang.creator.processor.Utils.findParseReturnKSTypeChain
import uii.ang.creator.processor.Utils.getGenerics
import uii.ang.creator.processor.Utils.getListGenericsCreatorAnnotation

class PaginationHelper(
  logger: KSPLogger,
  data: CreatorData
) : ProcessorHelper(logger, data) {
  fun genClassBuilder(): TypeSpec.Builder {
    val anno = data.annotationData
    val generateParameters = anno.parameters
    val parameterSpecList = generateParameters.map { para ->

      logger.warn("  ${para.paramName}: ${para.paramType}")
      val paramTypeClassName = when (para.paramType) {
        "String" -> stringClassName
        "Int" -> intClassName
        else -> stringClassName
      }
      ParameterSpec.builder(para.paramName, paramTypeClassName).build()

    }
    val propertySpecList = generateParameters.map { para ->

      logger.warn("  ${para.paramName}: ${para.paramType}")
      val paramTypeClassName = when (para.paramType) {
        "String" -> stringClassName
        "Int" -> intClassName
        else -> stringClassName
      }
      PropertySpec.builder(para.paramName, paramTypeClassName)
        .addModifiers(KModifier.PRIVATE)
        .initializer(para.paramName)
        .build()
    }
    val flux = FunSpec.constructorBuilder()
    flux.addParameter(
      ParameterSpec.builder("apiService", apiServiceClassName).build()
    )
      .addParameters(parameterSpecList)
      .build()
    val pageDataClassName = getGenerics(logger, data)

    val pagingSource = pagingSourceClassName.parameterizedBy(
      listOf(intClassName, pageDataClassName)
    )
    val apiServiceProp = PropertySpec.builder("apiService", apiServiceClassName)
      .addModifiers(KModifier.PRIVATE)
      .initializer("apiService")
    val classBuilder = TypeSpec.classBuilder(pageSourceClassName)
      .superclass(pagingSource)
      .primaryConstructor(flux.build())
      .addProperty(apiServiceProp.build())
      .addProperties(propertySpecList)
      .addFunction(generateGetRefreshKeyFunc(pageDataClassName).build())
      .addFunction(generateLoadFunc(pageDataClassName).build())
    return classBuilder
  }


  private fun generateGetRefreshKeyFunc(pageDataClassName: ClassName): FunSpec.Builder {
    val state = pagingStateClassName.parameterizedBy(
      listOf(intClassName, pageDataClassName)
    )
    val genFunction = FunSpec.builder("getRefreshKey")
      .addModifiers(KModifier.OVERRIDE)
    genFunction.addParameter(
      ParameterSpec.builder("state", state)
        .build()
    )
    genFunction.returns(intClassName.copy(nullable = true))
    val refreshCode = CodeBlock.builder()
      .addStatement("")
      .addStatement("return state.anchorPosition?.let { anchorPosition ->")
      .addStatement("\tstate.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)")
      .addStatement("\t\t?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)")
      .addStatement("}")
    genFunction.addCode(refreshCode.build())
    return genFunction
  }

  private fun generateLoadFunc(pageDataClassName: ClassName): FunSpec.Builder {
    val anno = data.annotationData
    val generateParameters = anno.parameters
    val loadResult = loadResultClassName.parameterizedBy(
      listOf(intClassName, pageDataClassName)
    )
    val loadParams = loadParamsClassName.parameterizedBy(intClassName)

    val genFunction = FunSpec.builder("load")
      .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
    genFunction.addParameter(
      ParameterSpec.builder("params", loadParams)
        .build()
    )
    val paramsStr = generateParameters.joinToString(", ") { para ->
      "${para.paramName} = ${para.paramName}"
    }
    genFunction.returns(loadResult)
    val loadCode = CodeBlock.builder()
      .addStatement("")
      .addStatement("return try {")
      .addStatement("\tval page = params.key ?: 1")
      .addStatement("\tval response = apiService(page = page, $paramsStr)")
      .addStatement("\tval nextKey = if (response.data!=null) null else page.plus(1)")
      .addStatement("\tresponse.data?.let {")
      .addStatement("\t\t%T.Page(", loadResultClassName)
      .addStatement("\t\t\tdata = it.list!!,")
      .addStatement("\t\t\tprevKey = if (page == 1) null else page.minus(1),")
      .addStatement("\t\t\tnextKey = nextKey,")
      .addStatement("\t\t)")
      .addStatement("\t} ?: run {")
      .addStatement("\t\t%T.Error(", loadResultClassName)
      .addStatement("\t\t\t%M(response)", getCallFailureFunc)
      .addStatement("\t\t)")
      .addStatement("\t}")
      .addStatement("} catch (e: Exception) {")
      .addStatement("\tLoadResult.Error(e)")
      .addStatement("}")

    genFunction.addCode(loadCode.build())
    return genFunction
  }
}