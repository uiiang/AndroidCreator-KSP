package uii.ang.creator.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import uii.ang.creator.codegen.CodeBuilder
import uii.ang.creator.template.showcase.ApiModelHelper
import uii.ang.creator.template.showcase.ResponseHelper
import uii.ang.creator.template.showcase.RetrofitServiceHelper

object CreatorCodeGenerator {

  fun generate(data: CreatorData, resolver: Resolver, logger: KSPLogger) {
    val basePackageName = "uii.ang"

//    val processorHelper = ProcessorHelper(logger, data, basePackageName)
    val apiModelHelper = ApiModelHelper(logger, data, basePackageName)
    val responseHelper = ResponseHelper(logger, data, basePackageName)
    val retrofitServiceHelper = RetrofitServiceHelper(logger, data, basePackageName)

    val apiModelClassNameStr = apiModelHelper.apiModelClassName.simpleName
    val responseClassNameStr = responseHelper.responseClassName.simpleName
    val retrofitServiceClassNameStr = retrofitServiceHelper.retrofitServiceClassName.simpleName

    // 生成ApiDomain代码
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

    // 生成Response代码
    if (data.generateResponse) {
      val responseClassName = responseHelper.genClassBuilder()
      CodeBuilder.getOrCreate(responseHelper.responsePackageName,
        responseClassNameStr,
        typeBuilderProvider = { responseClassName }
      )
    }

    // 生成Retrofit代码
    if (data.generateRetrofitService) {
      val retrofitServiceClassName = retrofitServiceHelper.genClassBuilder()
      val retrofitServiceCodeBuilder = CodeBuilder.getOrCreate(retrofitServiceHelper.retrofitServicePackageName,
        retrofitServiceClassNameStr,
        typeBuilderProvider = { retrofitServiceClassName }
      )
      val genFun = retrofitServiceHelper.genRetrofitServiceFuncCode()
      retrofitServiceCodeBuilder.addFunction(genFun.build(), true)
    }
  }
}








