package uii.ang.creator.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.ClassName
import uii.ang.creator.processor.Const.apiModelPackageName
import uii.ang.creator.processor.Const.repositoryImplPackageName
import uii.ang.creator.processor.Const.repositoryPackageName
import uii.ang.creator.processor.Const.responsePackageName
import uii.ang.creator.processor.Const.retrofitServicePackageName
import uii.ang.creator.processor.Utils.getClassName
import uii.ang.creator.processor.Utils.getPackageName

open class ProcessorHelper(
  val logger: KSPLogger,
  val data: CreatorData,
) {
  val classDeclaration = data.sourceClassDeclaration
  val dataClassName = getClassName(classDeclaration)

  //  val responseClassName = getClassName(classDeclaration, suffix = "Response")
  val classKdoc = classDeclaration.docString
  val dataClassPackageName = getPackageName(classDeclaration)

  fun getApiModelClassNameByDataModel(dataModelName: String): ClassName = ClassName(
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
//    "${data.sourceClassDeclaration.simpleName.getShortName()}RetrofitService"
  )

  val repositoryInterfaceClassName = ClassName(
    repositoryPackageName,
    if (data.annotationData.retrofitServiceClassName.isEmpty()) "${data.sourceClassDeclaration.simpleName.getShortName()}Repository"
    else "${data.annotationData.retrofitServiceClassName}Repository"
  )

  val repositoryImplClassName = ClassName(
    repositoryImplPackageName,
    if (data.annotationData.retrofitServiceClassName.isEmpty()) "${data.sourceClassDeclaration.simpleName.getShortName()}Repository"
    else "${data.annotationData.retrofitServiceClassName}RepositoryImpl"
  )

}