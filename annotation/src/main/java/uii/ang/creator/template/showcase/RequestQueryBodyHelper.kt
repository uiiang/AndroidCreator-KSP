package uii.ang.creator.template.showcase

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.*
import uii.ang.creator.annotation.Parameter
import uii.ang.creator.annotation.requestParamTypeBody
import uii.ang.creator.processor.Const.baseRequestBodyClassName
import uii.ang.creator.processor.Const.requestBodyPackageName
import uii.ang.creator.processor.Const.serializableClassName
import uii.ang.creator.processor.Const.serializableSerialNameClassName
import uii.ang.creator.processor.Const.serializableTransientClassName
import uii.ang.creator.processor.CreatorData
import uii.ang.creator.processor.ProcessorHelper
import uii.ang.creator.processor.Utils.convertType

class RequestQueryBodyHelper(
  logger: KSPLogger,
  data: CreatorData
) : ProcessorHelper(logger, data) {

  fun genClassBuilder(): TypeSpec.Builder {
    logger.warn("开始生成RequestQuery类名=${data.annotationData.methodName}")
    val constructorParams = genConstructor(data.annotationData.parameters)
    val propertyList = convertProperty(data.annotationData.parameters)
    return TypeSpec.classBuilder(requestBodyClassName)
      .addModifiers(KModifier.DATA)
      .addAnnotation(AnnotationSpec.builder(serializableClassName).build())
      .primaryConstructor(constructorParams.build())
      .addProperties(propertyList)
//      .addSuperinterface(baseRequestBodyClassName)
  }

  // 生成构造函数里的参数
  private fun genConstructor(parameters: List<Parameter>): FunSpec.Builder {
    val flux = FunSpec.constructorBuilder()
    val parameterSpecList = parameters
      .filter { param -> param.paramQueryType == requestParamTypeBody }
      .map { param ->
        val paramSpec = ParameterSpec.builder(
          param.paramName, convertType(param.paramType).asTypeName().copy(nullable = param.paramDefault.isEmpty())
        )
        if (param.paramDefault.isNotEmpty()) {
          if (param.paramType == "String") {
            paramSpec.defaultValue("\"${param.paramDefault}\"")
          } else {
            paramSpec.defaultValue(param.paramDefault)
          }
        }else {
          if (param.paramPostObjName.isNotEmpty()) {
            if (param.paramType == "Int") {
              paramSpec.defaultValue("0")
            } else {
              paramSpec.defaultValue("\"\"")
            }
          }
        }
        paramSpec.build()
      }

    flux.addParameters(parameterSpecList)
    return flux
  }

  // 生成构造函数中的属性列表
  private fun convertProperty(parameters: List<Parameter>, isParamObj: Boolean = false):
          List<PropertySpec> {
    val propertySpecSpecList = parameters
      .filter { param -> param.paramQueryType == requestParamTypeBody }
      .map { param ->
        val propSpec = PropertySpec.builder(
          param.paramName,// convertType(param.paramType),
          convertType(param.paramType).asTypeName().copy(nullable = param.paramDefault.isEmpty())
        )
//        if (param.paramName == "msgType") {
//          propSpec.addModifiers(KModifier.OVERRIDE).mutable()
//        }
        if (param.paramDefault.isNotEmpty()) {
          propSpec.initializer(param.paramName)
        } else {
          propSpec.initializer(param.paramName, null)
        }
        logger.warn("param.paramPostObjName == ${param.paramPostObjName}")
        if (param.paramPostObjName.isNotEmpty() && !isParamObj) {
          propSpec.addModifiers(KModifier.PRIVATE)
            .addAnnotation(AnnotationSpec.builder(serializableTransientClassName).build())
            .initializer(param.paramName)
        } else {
          propSpec
            .addAnnotation(
              AnnotationSpec.builder(serializableSerialNameClassName)
                .addMember("\"${param.paramName}\"").build()
            )
        }
        propSpec.build()
      }.toMutableList()

    // 生成调用自定义子对象的代码
    if (!isParamObj) {
      parameters
        .filter { param -> param.paramQueryType == requestParamTypeBody }
        .filter { param -> param.paramPostObjName.isNotEmpty() }
        .groupBy { param -> param.paramPostObjName }
        .onEach { (t, u) ->
          val paramBodyClassName = ClassName(
            requestBodyPackageName,
            "${classDeclaration.simpleName.getShortName()}RequestBody$t"
          )
          val paramBody = PropertySpec.builder(
            t,// convertType(param.paramType),
            paramBodyClassName
          )
          paramBody.mutable()
          paramBody.addAnnotation(
            AnnotationSpec.builder(serializableSerialNameClassName)
              .addMember("\"$t\"").build()
          )
          val paramsStr = u.joinToString { param -> param.paramName }
          paramBody.initializer(
            CodeBlock.builder()
              .addStatement("%T($paramsStr)", paramBodyClassName)
              .build()
          )
          propertySpecSpecList.add(paramBody.build())
        }
    }
    return propertySpecSpecList.ifEmpty { emptyList() }
  }


  // 创建body参数体内的对象
  fun createRequestParamBody(): List<TypeSpec.Builder> {
    val parameters = data.annotationData.parameters
    val paramBodyList = parameters
      .filter { param -> param.paramQueryType == requestParamTypeBody }
      .filter { param -> param.paramPostObjName.isNotEmpty() }
      .groupBy { param -> param.paramPostObjName }
      .map { (t, u) ->

        val paramBodyClassName = ClassName(
          requestBodyPackageName,
          "${classDeclaration.simpleName.getShortName()}RequestBody$t"
        )
        val constructorParams = genConstructor(u)
        val propertyList = convertProperty(u, true)
        TypeSpec.classBuilder(paramBodyClassName)
          .addModifiers(KModifier.DATA)
          .addAnnotation(AnnotationSpec.builder(serializableClassName).build())
          .primaryConstructor(constructorParams.build())
          .addProperties(propertyList)
      }
    return paramBodyList
  }
}