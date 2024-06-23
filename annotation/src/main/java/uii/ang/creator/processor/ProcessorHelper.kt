package uii.ang.creator.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import uii.ang.creator.annotation.Creator
import uii.ang.creator.basePackageName
import uii.ang.creator.modulePackageName

open class ProcessorHelper(
  val logger: KSPLogger,
  val data: CreatorData,
) {
  val classDeclaration = data.sourceClassDeclaration
  val dataClassName = getClassName(classDeclaration)

  //  val responseClassName = getClassName(classDeclaration, suffix = "Response")
  val classKdoc = classDeclaration.docString
  val dataClassPackageName = getPackageName(classDeclaration)
  val apiModelPackageName = "${basePackageName}.${modulePackageName}.data.datasource.api.model"
  val responsePackageName = "${basePackageName}.${modulePackageName}.data.datasource.api.response"
  val retrofitServicePackageName = "${basePackageName}.${modulePackageName}.data.datasource.api.service"
  val baseRetrofitPackageName = "$basePackageName.base.data.retrofit"

  val apiModelClassName = ClassName(
  apiModelPackageName,
    classDeclaration.simpleName.getShortName() + "ApiModel"
  )
  val responseClassName =ClassName(
    responsePackageName,
    data.annotationData.responseClassName.ifEmpty { "${data.sourceClassDeclaration.simpleName.getShortName()}Response" })

  val retrofitServiceClassName = ClassName(
    retrofitServicePackageName,
    data.annotationData.retrofitServiceClassName.ifEmpty { "${data.sourceClassDeclaration.simpleName.getShortName()}RetrofitService" }
  )

  val retrofitPackageName = "retrofit2.http"
  val retrofitFormUrlEncodedClassName = ClassName(retrofitPackageName, "FormUrlEncoded")
  val retrofitPostClassName = ClassName(retrofitPackageName, "POST")
  val retrofitGetClassName = ClassName(retrofitPackageName, "GET")

  //search?userId={userId}
  val retrofitQueryClassName = ClassName(retrofitPackageName, "Query")

  //"News/{userId}
  val retrofitPathClassName = ClassName(retrofitPackageName, "Path")

  //POST请求体内的字段
  val retrofitFieldClassName = ClassName(retrofitPackageName, "Field")
  val serializableClassName = ClassName("kotlinx.serialization", "Serializable")
  val serialNameClassName = ClassName("kotlinx.serialization", "SerialName")

  // base模块包含的类
  val baseRetrofitApiResultClassName = ClassName(baseRetrofitPackageName, "ApiResult")

  val listClassName = List::class.asClassName()

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