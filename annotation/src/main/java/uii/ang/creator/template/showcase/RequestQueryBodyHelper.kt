package uii.ang.creator.template.showcase

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.*
import uii.ang.creator.annotation.Parameter
import uii.ang.creator.annotation.requestParamTypeBody
import uii.ang.creator.processor.Const.baseRequestBodyClassName
import uii.ang.creator.processor.Const.baseRequestBodyUserClassName
import uii.ang.creator.processor.Const.serializableClassName
import uii.ang.creator.processor.Const.serializableSerialNameClassName
import uii.ang.creator.processor.Const.serializableTransientClassName
import uii.ang.creator.processor.Const.stringClassName
import uii.ang.creator.processor.CreatorData
import uii.ang.creator.processor.ProcessorHelper
import uii.ang.creator.processor.Utils.convertType
import uii.ang.creator.tools.firstCharUpperCase

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
      .addSuperinterface(baseRequestBodyClassName)
  }

  // 生成构造函数里的参数
  fun genConstructor(parameters: List<Parameter>): FunSpec.Builder {
    val flux = FunSpec.constructorBuilder()
    val parameterSpecList = parameters.filter { param -> param.paramQueryType == requestParamTypeBody }
      .map { param ->
        val paramSpec = ParameterSpec.builder(
          param.paramName, convertType(param.paramType).asTypeName().copy(nullable = param.paramDefault.isEmpty())
        )
        if (param.paramDefault.isNotEmpty()) {
//          paramSpec.defaultValue("\"${param.paramDefault}\"${convertToType(param.paramType)}")
          if (param.paramType == "String") {
            paramSpec.defaultValue("\"${param.paramDefault}\"")
          } else {
            paramSpec.defaultValue(param.paramDefault)
          }
        }
        paramSpec.build()
      }
    val userParamSpec = ParameterSpec.builder(
      "_user", baseRequestBodyUserClassName
    )
          .addModifiers(KModifier.OVERRIDE)
      .addAnnotation(AnnotationSpec.builder(serializableSerialNameClassName)
      .addMember("\"_user\"").build())
      .defaultValue(CodeBlock.builder()
        .addStatement("%T(uName, uTypeID)", baseRequestBodyUserClassName)
        .build())
    val uNameParamSpec = ParameterSpec.builder(
      "uName", stringClassName
    ).defaultValue("\"\"")//.addModifiers(KModifier.PRIVATE)
      .build()
    val uTypeIDParamSpec = ParameterSpec.builder(
      "uTypeID", stringClassName
    ).defaultValue("\"\"")//.addModifiers(KModifier.PRIVATE)
      .build()

    flux.addParameters(parameterSpecList)
//    flux.addParameter(userParamSpec.build())
    flux.addParameter(uNameParamSpec)
    flux.addParameter(uTypeIDParamSpec)
    return flux
  }

  // 生成构造函数中的属性列表
  private fun convertProperty(parameters: List<Parameter>):
          List<PropertySpec> {
    val propertySpecSpecList = parameters.filter { param -> param.paramQueryType == requestParamTypeBody }
      .map { param ->
        val propSpec = PropertySpec.builder(
          param.paramName,// convertType(param.paramType),
          convertType(param.paramType).asTypeName().copy(nullable = param.paramDefault.isEmpty())
        )
        if (param.paramName == "msgType") {
          propSpec.addModifiers(KModifier.OVERRIDE).mutable()
        }
        if (param.paramDefault.isNotEmpty()) {
          propSpec.initializer(param.paramName)
        } else {
          propSpec.initializer(param.paramName, null)
        }
        propSpec
          .addAnnotation(
            AnnotationSpec.builder(serializableSerialNameClassName)
              .addMember("\"${param.paramName}\"").build()
          )
        propSpec.build()
      }.toMutableList()

    val userPropSpec = PropertySpec.builder(
      "_user",// convertType(param.paramType),
      baseRequestBodyUserClassName
    ).addModifiers(KModifier.OVERRIDE)
    userPropSpec.mutable()
    userPropSpec
      .addAnnotation(
        AnnotationSpec.builder(serializableSerialNameClassName)
          .addMember("\"_user\"").build()
      )
      .initializer(CodeBlock.builder()
        .addStatement("%T(uName, uTypeID)", baseRequestBodyUserClassName)
        .build()
      )
    val uNamePropSpec = PropertySpec.builder(
      "uName", stringClassName
    ).addModifiers(KModifier.PRIVATE)
      .addAnnotation(AnnotationSpec.builder(serializableTransientClassName).build())
      .initializer("uName")
    val uTypePropSpec = PropertySpec.builder(
      "uTypeID", stringClassName
    ).addModifiers(KModifier.PRIVATE)
      .addAnnotation(AnnotationSpec.builder(serializableTransientClassName).build())
      .initializer("uTypeID")
    propertySpecSpecList.add(userPropSpec.build())
    propertySpecSpecList.add(uNamePropSpec.build())
    propertySpecSpecList.add(uTypePropSpec.build())
    return propertySpecSpecList.ifEmpty { emptyList() }
  }
}