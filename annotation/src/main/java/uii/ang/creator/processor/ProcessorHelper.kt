package uii.ang.creator.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import uii.ang.creator.processor.Const.apiModelPackageName
import uii.ang.creator.processor.Const.apiServicePackageName
import uii.ang.creator.processor.Const.baseKtorPackageName
import uii.ang.creator.processor.Const.databasePackageName
import uii.ang.creator.processor.Const.entityModelPackageName
import uii.ang.creator.processor.Const.pagingSourcePackageName
import uii.ang.creator.processor.Const.repositoryImplPackageName
import uii.ang.creator.processor.Const.repositoryPackageName
import uii.ang.creator.processor.Const.requestBodyPackageName
import uii.ang.creator.processor.Const.responsePackageName
import uii.ang.creator.processor.Const.retrofitServicePackageName
import uii.ang.creator.processor.Const.useCasePackageName
import uii.ang.creator.processor.Utils.getClassName
import uii.ang.creator.processor.Utils.getPackageName
import uii.ang.creator.tools.firstCharUpperCase

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

  // 根据当前类生成apiModelClassName对象
  val apiModelClassName = ClassName(
    apiModelPackageName,
    classDeclaration.simpleName.getShortName() + "ApiModel"
  )
  val queryQueryBodyClassName = ClassName(
    requestBodyPackageName, classDeclaration.simpleName.getShortName() + "QueryBody"
  )

  // Response对象
  val responseClassName = ClassName(
    responsePackageName,
    if (data.annotationData.responseClassName.isEmpty()) "${data.sourceClassDeclaration.simpleName.getShortName()}Response"
    else "${data.annotationData.responseClassName}Response"
  )

  // EntityModel对象
  val entityModelClassName = ClassName(
    entityModelPackageName,
    classDeclaration.simpleName.getShortName() + "EntityModel"
  )

  val roomDaoInterfaceClassName = ClassName(
    databasePackageName,
    "${classDeclaration.simpleName.getShortName()}Dao"
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
    if (data.annotationData.retrofitServiceClassName.isEmpty()) "${data.sourceClassDeclaration.simpleName.getShortName()}RepositoryImpl"
    else "${data.annotationData.retrofitServiceClassName}RepositoryImpl"
  )

  val pageSourceClassName = ClassName(
    pagingSourcePackageName,
    "${classDeclaration.simpleName.getShortName()}PagingSource"
  )

  val requestBodyClassName = ClassName(
    requestBodyPackageName,
    "${classDeclaration.simpleName.getShortName()}RequestBody"
  )

  val userCaseClassName = ClassName(
    useCasePackageName,
    "${data.annotationData.methodName.firstCharUpperCase()}UseCase"
  )
  val userCaseGenClassName = ClassName(
    useCasePackageName,
    "${data.annotationData.methodName.firstCharUpperCase()}UseCaseGen"
  )

  val dataModuleClassName = ClassName(
    dataClassPackageName, "DataModule"
  )

  val apiServiceClassName = ClassName(
    apiServicePackageName,
    classDeclaration.simpleName.getShortName() + "ApiService"
  )

  val baseObjClass = if (data.baseObjClassName.isNotEmpty()) {
    ClassName(baseKtorPackageName, data.baseObjClassName)
  } else {
    null
  }
  val checkResponseSuccessFunc = if (data.checkResponseSuccessFuncPath.isNotEmpty()) {
    MemberName(responsePackageName, data.checkResponseSuccessFuncPath)
  } else null
  val getCallFailureFunc = if (data.getCallFailureFuncPath.isNotEmpty()) {
    MemberName(responsePackageName, data.getCallFailureFuncPath)
  } else null
  val callFailureClassName = ClassName(responsePackageName, "CallFailure")

}












