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
import uii.ang.creator.annotation.Creator
import uii.ang.creator.annotation.Parameter
import uii.ang.creator.annotation.ParseRoot
import uii.ang.creator.annotation.requestMethodGet
import uii.ang.creator.processor.Const.apiModelPackageName
import uii.ang.creator.processor.Const.listClassName
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

  // 使用primaryConstructor来转换构造函数中的参数，可以获得给参数标注的注解
  val primaryConstructorParameters: List<PropertyDescriptor> =
    sourceClassDeclaration.primaryConstructor?.parameters?.map {
      val resolve = it.type.resolve()
      val toTypeName = resolve.toTypeName(sourceClassDeclaration.typeParameters.toTypeParameterResolver())
      val wrapperType = getWrapperType(it)
      val propertyDescriptor = PropertyDescriptor(
        sourceClassName = sourceClassDeclaration.toClassName(),
        typeClassName = resolve.toClassName(),
        //包含完整包名类名
        typeName = toTypeName,
        wrapperTypeName = wrapperType,
        isNullable = resolve.isNullable(),
        //仅类名
        className = it.name!!,
        //泛型参数
        arguments = resolve.arguments,
        mandatoryForConstructor = true,
        kDoc = "",
        isParseRoot = it.hasAnnotation<ParseRoot>()
      )
      logger.warn(propertyDescriptor.toString())
      propertyDescriptor
    }?.toList() ?: emptyList()

  private fun getWrapperType(it: KSValueParameter): TypeName {
    val resolve = it.type.resolve()
    val toTypeName = resolve.toTypeName(sourceClassDeclaration.typeParameters.toTypeParameterResolver())
    val wrapperType = if (toTypeName.isBaseType()) {
      toTypeName
    } else if (toTypeName.isList()) {
      // 如果是list，获取list中的泛型类，
      // 如果泛型类是注解Creator的数据类，转换成apimodel
      // 判断当前list的泛型类是否为creator
      val ksNode = resolve.arguments.first()
        .type?.resolve()?.let { it1 -> getListGenericsCreatorAnnotation(it1) }
      ksNode?.let { node ->
        // 字段属性为List<注解了Create的data class>
        val apiModelName = node.toString() + "ApiModel"
        //        logger.warn("apiModelName $apiModelName")
        val apiModelClass = ClassName(apiModelPackageName, apiModelName)
        val parameterizedBy = listClassName.parameterizedBy(apiModelClass)
        //        logger.warn("  convert parameterizedBy ${parameterizedBy.toString()}")
        parameterizedBy
      } ?: toTypeName
    } else {
      // 字段属性为data class
      val retClassName = ClassName(
        apiModelPackageName,
        it.name!!.getShortName().firstCharUpperCase() + "ApiModel"
      )
      //      logger.warn("  convert api model ${retClassName.simpleName}")
      retClassName
    }
    return wrapperType
  }

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