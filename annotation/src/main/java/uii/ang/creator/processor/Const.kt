package uii.ang.creator.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import uii.ang.creator.basePackageName
import uii.ang.creator.modulePackageName

object Const {

  val apiModelPackageName = "$basePackageName.$modulePackageName.data.datasource.api.model"
  val responsePackageName = "$basePackageName.$modulePackageName.data.datasource.api.response"
  val retrofitServicePackageName = "$basePackageName.$modulePackageName.data.datasource.api.service"
  val baseRetrofitPackageName = "$basePackageName.base.data.retrofit"

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

}