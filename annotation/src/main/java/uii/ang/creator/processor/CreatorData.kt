package uii.ang.creator.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import uii.ang.creator.annotation.*
import uii.ang.creator.processor.Const.apiModelPackageName
import uii.ang.creator.processor.Const.listClassName
import uii.ang.creator.processor.Utils.convertKSValueParameterToPropertyDescriptor
import uii.ang.creator.processor.Utils.findParseReturnChain
import uii.ang.creator.processor.Utils.getListGenericsCreatorAnnotation
import uii.ang.creator.tools.*

class CreatorData(
  val annotationData: AnnotationData,
  val sourceClassDeclaration: KSClassDeclaration,
  val logger: KSPLogger
) : AnnotatedBaseData {

  val generateApiModel: Boolean = annotationData.generateApiModel
  val generateResponse: Boolean = annotationData.generateResponse
  val generateRetrofitService: Boolean = annotationData.generateRetrofitService


  // 如果没有指定responseClassName, 使用默认的 [data className]加Response
//  val responseClassName = getResponseClass()
//  val apiModelClassName = getApiModelClass()


//  val getParseReturnChain: List<Any> = findParseReturnChain(sourceClassDeclaration, logger)

  // 使用primaryConstructor来转换构造函数中的参数，可以获得给参数标注的注解
  val primaryConstructorParameters: List<PropertyDescriptor> =
    sourceClassDeclaration.primaryConstructor?.parameters?.map {
      val propertyDescriptor = convertKSValueParameterToPropertyDescriptor(it, sourceClassDeclaration)
//      logger.warn(propertyDescriptor.toString())
      propertyDescriptor
    }?.toList() ?: emptyList()

  //使用getAllProperties来转换构造函数中的参数，无法获得给参数标注的注解
//  val propertyDescriptorList: List<PropertyDescriptor> =
//    sourceClassDeclaration.getAllProperties().map {
////      logger.warn("sourceClassDeclaration ${sourceClassDeclaration.simpleName.getShortName()}")
////      logger.warn(
////        "declaration ${it.type.resolve().toClassName().simpleName} "+
////        "toTypeName ${it.type.resolve().toTypeName(sourceClassDeclaration.typeParameters.toTypeParameterResolver()).annotations.count()} " +
////                "it.simpleName ${it.simpleName.getShortName()} " +
////                "isNullable ${it.type.resolve().isNullable()}"
////      )
////      logger.warn("propertyDescriptorList\n \tprop = ${it.simpleName.getShortName()}\n " +
////              "\tannotations.count=${it.type.resolve().annotations.count()}\n " +
////              "\tdeclaration =${it.type.resolve().declaration.simpleName.getShortName()}\n " +
////              "\tit.count=${it.annotations.count()}")
//      PropertyDescriptor(
//        sourceClassName = sourceClassDeclaration.toClassName(),
//        typeClassName = it.type.resolve().toClassName(),
//        //包含完整包名类名
//        typeName = it.type.resolve().toTypeName(sourceClassDeclaration.typeParameters.toTypeParameterResolver()),
//        isNullable = it.type.resolve().isNullable(),
//        //仅类名
//        className = it.simpleName,
//        //泛型参数
//        arguments = it.type.resolve().arguments,
//        mandatoryForConstructor = true,
//        kDoc = it.docString?.trim(' ', '\n') ?: it.toString()
//          .capitalizeAndAddSpaces()
//      )
//    }.toList()


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