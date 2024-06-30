package uii.ang.creator.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.asClassName
import uii.ang.creator.basePackageName
import uii.ang.creator.modulePackageName

object Const {

  // 模块包相关路径
  val apiModelPackageName = "$basePackageName.$modulePackageName.data.datasource.api.model"
  val responsePackageName = "$basePackageName.$modulePackageName.data.datasource.api.response"
  val retrofitServicePackageName = "$basePackageName.$modulePackageName.data.datasource.api.service"
  val repositoryPackageName = "$basePackageName.$modulePackageName.domain.repository"
  val repositoryImplPackageName = "$basePackageName.$modulePackageName.data.repository"

  // base包相关路径
  val baseRetrofitPackageName = "$basePackageName.base.data.retrofit"
  val baseDomainResultPackageName = "$basePackageName.base.domain.result"

  val moduleToDomainMemberName = MemberName(apiModelPackageName, "toDomainModel")

  // base模块包含的类
  val baseRetrofitApiResultClassName = ClassName(baseRetrofitPackageName, "ApiResult")
  val baseRetrofitApiResultSuccessMemberName = ClassName(baseRetrofitPackageName+"."+baseRetrofitApiResultClassName.simpleName, "Success")
  val baseRetrofitApiResultErrorMemberName = ClassName(baseRetrofitPackageName+"."+baseRetrofitApiResultClassName.simpleName, "Error")
  val baseRetrofitApiResultExceptionMemberName = ClassName(baseRetrofitPackageName+"."+baseRetrofitApiResultClassName.simpleName, "Exception")

  val baseDomainResultClassName = ClassName(baseDomainResultPackageName, "Result")
  val baseDomainResultSuccessMemberName = ClassName(baseDomainResultPackageName+"."+baseDomainResultClassName.simpleName, "Success")
  val baseDomainResultFailureMemberName = ClassName(baseDomainResultPackageName+"."+baseDomainResultClassName.simpleName, "Failure")

  val listClassName = List::class.asClassName()


  // 第三方为相关
  // retrofit库
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

  // 序列化库
  val serializableClassName = ClassName("kotlinx.serialization", "Serializable")
  val serialNameClassName = ClassName("kotlinx.serialization", "SerialName")

  // Timber
  val timberClassName = ClassName("timber.log", "Timber")
  val timberErrorMemberName = MemberName(timberClassName, "e")
}












