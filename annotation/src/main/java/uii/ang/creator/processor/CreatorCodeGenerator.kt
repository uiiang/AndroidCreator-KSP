package uii.ang.creator.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import uii.ang.creator.codegen.CodeBuilder
import uii.ang.creator.template.showcase.ApiModelHelper
import uii.ang.creator.template.showcase.ResponseHelper
import uii.ang.creator.template.showcase.RetrofitServiceHelper

object CreatorCodeGenerator {
  fun generate(data: CreatorData, resolver: Resolver, logger: KSPLogger) {

//    val processorHelper = ProcessorHelper(logger, data, basePackageName)
    val apiModelHelper = ApiModelHelper(logger, data)
    val responseHelper = ResponseHelper(logger, data)
    val retrofitServiceHelper = RetrofitServiceHelper(logger, data)

    // 生成ApiDomain代码
    generatorApiModel(apiModelHelper, data)

    // 生成Response代码
    if (data.generateResponse) {
      generatorResponse(responseHelper)
    }

    // 生成Retrofit代码
    if (data.generateRetrofitService) {
      generatorRetrofitService(retrofitServiceHelper)
    }
  }

  private fun generatorRetrofitService(
    retrofitServiceHelper: RetrofitServiceHelper
  ) {
    val retrofitServiceClassNameStr = retrofitServiceHelper.retrofitServiceClassName.simpleName
    val retrofitServiceClassName = retrofitServiceHelper.genClassBuilder()
    val retrofitServiceCodeBuilder = CodeBuilder.getOrCreate(retrofitServiceHelper.retrofitServicePackageName,
      retrofitServiceClassNameStr,
      typeBuilderProvider = { retrofitServiceClassName }
    )
    val genFun = retrofitServiceHelper.genRetrofitServiceFuncCode()
    retrofitServiceCodeBuilder.addFunction(genFun.build(), true)
  }

  private fun generatorResponse(
    responseHelper: ResponseHelper
  ) {
    val responseClassNameStr = responseHelper.responseClassName.simpleName
    val responseClassName = responseHelper.genClassBuilder()
    CodeBuilder.getOrCreate(responseHelper.responsePackageName,
      responseClassNameStr,
      typeBuilderProvider = { responseClassName }
    )
  }

  private fun generatorApiModel(
    apiModelHelper: ApiModelHelper,
    data: CreatorData
  ) {
    val apiModelClassNameStr = apiModelHelper.apiModelClassName.simpleName
    val apiModelClassName = apiModelHelper.genClassBuilder()
    val apiModelCodeBuilder = CodeBuilder.getOrCreate(
      apiModelHelper.apiModelPackageName,
      apiModelClassNameStr,
      typeBuilderProvider = {
        apiModelClassName
      }
    )
    val toDomainModel = apiModelHelper.toDomainModel(
      apiModelClassNameStr,
      apiModelHelper.apiModelPackageName,
      data
    )
    apiModelCodeBuilder.addFunction(toDomainModel, false)
  }
}








