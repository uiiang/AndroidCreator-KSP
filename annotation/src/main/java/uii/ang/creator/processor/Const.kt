package uii.ang.creator.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.asClassName
import uii.ang.creator.basePackageName
import uii.ang.creator.modulePackageName

object Const {

  // 模块包相关路径
  val dataModulePackageName = "$basePackageName.$modulePackageName.data"
  val localCfgRepositoryPackageName = "$basePackageName.$modulePackageName.data.repository"
  val apiModelPackageName = "$basePackageName.$modulePackageName.data.model"
  val responsePackageName = "$basePackageName.$modulePackageName.data.response"
  val requestBodyPackageName = "$basePackageName.$modulePackageName.domain.request"
  val retrofitServicePackageName = "$basePackageName.$modulePackageName.data.datasource.api.service"
  val entityModelPackageName = "$basePackageName.$modulePackageName.data.datasource.database.model"
  val databasePackageName = "$basePackageName.$modulePackageName.data.datasource.database"

  val repositoryImplPackageName = "$basePackageName.$modulePackageName.data.repository"
  val pagingSourcePackageName = "$basePackageName.$modulePackageName.data.pagination"

  val koinDataModuleGenName = "DataModuleGen"
  val koinApiServiceModuleGenName = "ApiServiceModule"

  val domainModulePackageName = "$basePackageName.$modulePackageName.domain"
  val repositoryPackageName = "$basePackageName.$modulePackageName.domain.repository"
  val useCasePackageName = "$basePackageName.$modulePackageName.domain.usecase"
  val koinDomainModuleGenName = "DomainModuleGen"

  val repositoryFlowImplPackageName = "$basePackageName.$modulePackageName.data.repository"
  val repositoryFlowPackageName = "$basePackageName.$modulePackageName.domain.repository"

  // apiservice
  val apiServicePackageName = "$basePackageName.$modulePackageName.service"

  // base模块相关路径
  val baseRetrofitPackageName = "$basePackageName.base.data.retrofit"
  val baseDomainResultPackageName = "$basePackageName.base.domain.result"
  val baseKtorPackageName = "$basePackageName.base.data.ktor"

  val baseRequestBodyClassName = ClassName(baseKtorPackageName, "BaseRequestBody")
  val baseRequestBodyUserClassName = ClassName(baseKtorPackageName, "BaseRequestBodyUser")
  val moduleToDomainMemberName = MemberName(apiModelPackageName, "toDomainModel")
  val moduleToEntityMemberName = MemberName(apiModelPackageName, "toEntityModel")
  val localCfgRepositoryClassName = ClassName(localCfgRepositoryPackageName, "LocalCfgRepository")

  // android 翻页
  val pagingSourceClassName = ClassName("androidx.paging", "PagingSource")
  val pagingStateClassName = ClassName("androidx.paging", "PagingState")
  val loadParamsClassName = ClassName("androidx.paging.PagingSource","LoadParams")
  val loadResultClassName = ClassName("androidx.paging.PagingSource","LoadResult")
  val pagingDataClassName = ClassName("androidx.paging", "PagingData")
  val pagerClassName = ClassName("androidx.paging", "Pager")
  val pagingConfigClassName = ClassName("androidx.paging", "PagingConfig")

  // base模块包含的类
  val baseRetrofitApiResultClassName = ClassName(baseRetrofitPackageName, "ApiResult")
  val baseRetrofitApiResultSuccessMemberName =
    ClassName(baseRetrofitPackageName + "." + baseRetrofitApiResultClassName.simpleName, "Success")
  val baseRetrofitApiResultErrorMemberName =
    ClassName(baseRetrofitPackageName + "." + baseRetrofitApiResultClassName.simpleName, "Error")
  val baseRetrofitApiResultExceptionMemberName =
    ClassName(baseRetrofitPackageName + "." + baseRetrofitApiResultClassName.simpleName, "Exception")

  val baseRemoteResponseClassName = ClassName(baseKtorPackageName, "RemoteResponse")
  val baseNetworkCallResultClassName = ClassName(baseKtorPackageName, "NetworkCallResult")
  val baseErrorModelClassName = ClassName(baseKtorPackageName, "ErrorModel")
  val baseDomainResultClassName = ClassName(baseDomainResultPackageName, "Result")
  val baseDomainResultSuccessMemberName =
    ClassName(baseDomainResultPackageName + "." + baseDomainResultClassName.simpleName, "Success")
  val baseDomainResultFailureMemberName =
    ClassName(baseDomainResultPackageName + "." + baseDomainResultClassName.simpleName, "Failure")

  val baseDomainResultMemberName = MemberName(baseDomainResultPackageName, "mapSuccess")

  val arrayListClassName = ArrayList::class.asClassName()
  val listClassName = List::class.asClassName()
  val hashMapClassName = HashMap::class.asClassName()
  val stringClassName = String::class.asClassName()
  val intClassName = Int::class.asClassName()
  val anyClassName = Any::class.asClassName()


  // 第三方为相关
  // retrofit库
  val retrofitPackageName = "retrofit2.http"
  val retrofitBasePackageName = "retrofit2"
  val retrofitClassName = ClassName(retrofitBasePackageName, "Retrofit")
  val retrofitFormUrlEncodedClassName = ClassName(retrofitPackageName, "FormUrlEncoded")
  val retrofitPostClassName = ClassName(retrofitPackageName, "POST")
  val retrofitUrlClassName = ClassName(retrofitPackageName, "Url")
  val retrofitGetClassName = ClassName(retrofitPackageName, "GET")

  //search?userId={userId}
  val retrofitQueryClassName = ClassName(retrofitPackageName, "Query")

  //"News/{userId}
  val retrofitPathClassName = ClassName(retrofitPackageName, "Path")

  // POST请求体直接发送JSON数据
  val retrofitBodyClassName = ClassName(retrofitPackageName, "Body")

  //POST请求体内的字段
  val retrofitFieldClassName = ClassName(retrofitPackageName, "Field")

  // 序列化库
  val serializableClassName = ClassName("kotlinx.serialization", "Serializable")
  val serializableSerialNameClassName = ClassName("kotlinx.serialization", "SerialName")
  val serializableJsonClassName = ClassName("kotlinx.serialization.json", "Json")
  val serializableTransientClassName = ClassName("kotlinx.serialization", "Transient")
  val serialEncodeToStringMemberName = MemberName("kotlinx.serialization", "encodeToString")
  val serialDecodeFromStringMemberName = MemberName("kotlinx.serialization", "decodeFromString")

  // flow
  val kotlinFlowFlowOnMemberName = MemberName("kotlinx.coroutines.flow", "flowOn")
  val kotlinFlowFlowMemberName = ClassName("kotlinx.coroutines.flow", "flow")
  val kotlinFlowCatchMemberName = MemberName("kotlinx.coroutines.flow", "catch")
  val kotlinFlowFlowClassName = ClassName("kotlinx.coroutines.flow", "Flow")
  val kotlinxCoroutineDispatcherClassName = ClassName("kotlinx.coroutines", "CoroutineDispatcher")
  val kotlinxWithContextClassName = MemberName("kotlinx.coroutines", "withContext")

  // Timber
  val timberClassName = ClassName("timber.log", "Timber")
  val timberErrorMemberName = MemberName(timberClassName, "e")

  val koinModuleFunClassName = ClassName("org.koin.dsl", "module")
  val koinModuleClassName = ClassName("org.koin.core.module", "Module")
  val koinNamedClassName = MemberName("org.koin.core.qualifier", "named")
  val koinSingleOfMemberName = MemberName("org.koin.core.module.dsl", "singleOf")

  // Room数据库操作
  val roomEntityClassName = ClassName("androidx.room", "Entity")
  val roomPrimaryKeyClassName = ClassName("androidx.room", "PrimaryKey")
  val roomTypeConverterClassName = ClassName("androidx.room", "TypeConverter")
  val roomTypeConvertersClassName = ClassName("androidx.room", "TypeConverters")
  val roomDaoClassName = ClassName("androidx.room", "Dao")
  val roomInsertClassName = ClassName("androidx.room", "Insert")
  val roomUpdateClassName = ClassName("androidx.room", "Update")
  val roomQueryClassName = ClassName("androidx.room", "Query")
  val roomDeleteClassName = ClassName("androidx.room", "Delete")
  val roomOnConflictStrategyClassName = ClassName("androidx.room", "OnConflictStrategy")
  val roomDatabaseClassName = ClassName("androidx.room", "Database")
  val roomRoomDatabaseClassName = ClassName("androidx.room", "RoomDatabase")

  // ktor
  val ktorClientBasePackageName = "io.ktor.client"
  val ktorHttpBasePackageName = "io.ktor.http"
  val ktorCallBasePackageName = "${ktorClientBasePackageName}.call"
  val ktorRequestBasePackageName = "${ktorClientBasePackageName}.request"
  val ktorHttpClient = ClassName(ktorClientBasePackageName, "HttpClient")
  val ktorBody = MemberName(ktorCallBasePackageName, "body")
  val ktorGet = MemberName(ktorRequestBasePackageName, "get")
  val ktorParameter = MemberName(ktorRequestBasePackageName, "parameter")
  val ktorPost = MemberName(ktorRequestBasePackageName, "post")
  val ktorSetBody = MemberName(ktorRequestBasePackageName, "setBody")
  val ktorAccept = MemberName(ktorRequestBasePackageName, "accept")
  val ktorContentType = ClassName(ktorHttpBasePackageName, "ContentType")

  val appendPathSegments = MemberName(ktorHttpBasePackageName, "appendPathSegments")
}












