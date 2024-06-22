package uii.ang.creator.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import uii.ang.creator.annotation.Creator
import uii.ang.creator.annotation.Parameter
import uii.ang.creator.annotation.requestMethodGet
import uii.ang.creator.tools.capitalizeAndAddSpaces
import uii.ang.creator.tools.from
import uii.ang.creator.tools.isList
import uii.ang.creator.tools.isNullable

class CreatorData(
  val annotationData: AnnotationData,
  val sourceClassDeclaration: KSClassDeclaration,
  val logger: KSPLogger
) : AnnotatedBaseData {

  val generateResponse: Boolean = annotationData.generateResponse
  val generateRetrofitService: Boolean = annotationData.generateRetrofitService

  // 如果没有指定responseClassName, 使用默认的 [data className]加Response
//  val responseClassName = getResponseClass()
//  val apiModelClassName = getApiModelClass()

  val propertyDescriptorList: List<PropertyDescriptor> =
    sourceClassDeclaration.getAllProperties().map {
//      logger.warn(
//        "declaration ${it.type.resolve().toClassName().simpleName} "+
//        "toTypeName ${it.type.resolve().toTypeName(sourceClassDeclaration.typeParameters.toTypeParameterResolver())} " +
//                "it.simpleName ${it.simpleName.getShortName()} " +
//                "isNullable ${it.type.resolve().isNullable()}"
//      )
      it.type.resolve().toClassName().isList()
      PropertyDescriptor(
        typeClassName = it.type.resolve().toClassName(),
        //包含完整包名类名
        typeName = it.type.resolve().toTypeName(sourceClassDeclaration.typeParameters.toTypeParameterResolver()),
        isNullable = it.type.resolve().isNullable(),
        //仅类名
        className = it.simpleName,
        //泛型参数
        arguments = it.type.resolve().arguments,
        mandatoryForConstructor = true,
        kDoc = it.docString?.trim(' ', '\n') ?: it.toString()
          .capitalizeAndAddSpaces()
      )
    }.toList()


  data class AnnotationData(
    val generateApiModel: Boolean,
    val generateResponse: Boolean,
    val generateRetrofitService: Boolean,
    val responseClassName: String,
    val retrofitServiceClassName: String,
    val method: String = requestMethodGet,
    val url: String,
    val methodName: String,
    val returnResponseClassName: String,
    val parameters: List<Parameter>,
  ) {
    companion object {
      fun from(annotation: KSAnnotation): AnnotationData {
        return AnnotationData(
          generateApiModel = annotation.arguments.first { it.name?.asString() == Creator::generateApiModel.name }.value as Boolean,
          generateResponse = annotation.arguments.first { it.name?.asString() == Creator::generateResponse.name }.value as Boolean,
          generateRetrofitService = annotation.arguments.first { it.name?.asString() == Creator::generateRetrofitService.name }.value as Boolean,
          responseClassName = annotation.arguments.first { it.name?.asString() == Creator::responseClassName.name }.value as String,
          retrofitServiceClassName = annotation.arguments.first { it.name?.asString() == Creator::retrofitServiceClassName.name }.value as String,
          method = annotation.arguments.first { it.name?.asString() == Creator::method.name }.value as String,
          methodName = annotation.arguments.first { it.name?.asString() == Creator::methodName.name }.value as String,
          url = annotation.arguments.first { it.name?.asString() == Creator::url.name }.value as String,
          returnResponseClassName = annotation.arguments.first { it.name?.asString() == Creator::returnResponseClassName.name }.value as String,
          parameters = (annotation.arguments.first { it.name?.asString() == Creator::parameters.name }.value as List<*>)
            .filterIsInstance<KSAnnotation>()
            .map { Parameter.from(it) },
        )
      }
    }
  }
}