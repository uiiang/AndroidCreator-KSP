package uii.ang.creator.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import uii.ang.creator.*
import uii.ang.creator.annotation.Creator
import uii.ang.creator.processor.Const.apiModelPackageName
import uii.ang.creator.processor.Const.responsePackageName
import uii.ang.creator.processor.Const.retrofitServicePackageName

open class ProcessorHelper(
  val logger: KSPLogger,
  val data: CreatorData,
) {
  val classDeclaration = data.sourceClassDeclaration
  val dataClassName = getClassName(classDeclaration)

  //  val responseClassName = getClassName(classDeclaration, suffix = "Response")
  val classKdoc = classDeclaration.docString
  val dataClassPackageName = getPackageName(classDeclaration)

  fun getApiModelClassNameByDataModel(dataModelName:String):ClassName = ClassName(
      apiModelPackageName,
      dataModelName + "ApiModel"
    )

  val apiModelClassName = ClassName(
    apiModelPackageName,
    classDeclaration.simpleName.getShortName() + "ApiModel"
  )
  val responseClassName = ClassName(
    responsePackageName,
    if (data.annotationData.responseClassName.isEmpty()) "${data.sourceClassDeclaration.simpleName.getShortName()}Response"
    else "${data.annotationData.responseClassName}Response"
  )

  val retrofitServiceClassName = ClassName(
    retrofitServicePackageName,
    if (data.annotationData.retrofitServiceClassName.isEmpty()) "${data.sourceClassDeclaration.simpleName.getShortName()}RetrofitService"
    else "${data.annotationData.retrofitServiceClassName}RetrofitService"
  )

  fun getListGenericsCreatorAnnotation(propertyDesc: PropertyDescriptor):
          KSNode? {
    val ksType = propertyDesc.arguments.first()
      .type?.resolve()
    val annoList = ksType?.declaration?.annotations
      ?.filter {
        isCreatorAnnotation(it)
      } ?: emptySequence()
    return if (annoList.count() > 0) annoList.first().parent else null
  }

  fun isCreatorAnnotation(annotation: KSAnnotation): Boolean {
    return annotation.shortName.getShortName() == Creator::class.simpleName
  }

  fun isNullable(propertyDeclaration: KSPropertyDeclaration): Boolean {
    return propertyDeclaration.type.resolve().isMarkedNullable
  }

  fun getClassName(
    classDeclaration: KSClassDeclaration,
    prex: String = "", suffix: String = ""
  ): String {
    return prex + classDeclaration.simpleName.asString() + suffix
  }

  fun getPackageName(classDeclaration: KSClassDeclaration): String {
    return classDeclaration.packageName.asString()
  }
}