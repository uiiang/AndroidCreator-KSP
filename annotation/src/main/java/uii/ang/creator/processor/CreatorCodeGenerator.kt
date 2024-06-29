package uii.ang.creator.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import uii.ang.creator.codegen.CodeBuilder
import uii.ang.creator.processor.Const.apiModelPackageName
import uii.ang.creator.processor.Const.repositoryPackageName
import uii.ang.creator.processor.Const.responsePackageName
import uii.ang.creator.processor.Const.retrofitServicePackageName
import uii.ang.creator.template.showcase.ApiModelHelper
import uii.ang.creator.template.showcase.RepositoryHelper
import uii.ang.creator.template.showcase.ResponseHelper
import uii.ang.creator.template.showcase.RetrofitServiceHelper

object CreatorCodeGenerator {
  fun generate(data: CreatorData, resolver: Resolver, logger: KSPLogger) {

//    val processorHelper = ProcessorHelper(logger, data, basePackageName)
    val apiModelHelper = ApiModelHelper(logger, data)
    val responseHelper = ResponseHelper(logger, data)
    val retrofitServiceHelper = RetrofitServiceHelper(logger, data)
    val repositoryHelper = RepositoryHelper(logger, data)

    // 生成ApiDomain代码
    if (data.generateApiModel){
      generatorApiModel(apiModelHelper, data)
    }

    // 生成Response代码
    if (data.generateResponse) {
      generatorResponse(responseHelper)
    }

    // 生成Retrofit代码
    if (data.generateRetrofitService) {
      generatorRetrofitService(retrofitServiceHelper)
      generatorRepository(repositoryHelper)
    }
  }

  private fun generatorRetrofitService(
    retrofitServiceHelper: RetrofitServiceHelper
  ) {
    val retrofitServiceClassNameStr = retrofitServiceHelper.retrofitServiceClassName.simpleName
    val retrofitServiceClassName = retrofitServiceHelper.genClassBuilder()
    val retrofitServiceCodeBuilder = CodeBuilder.getOrCreate(retrofitServicePackageName,
      retrofitServiceClassNameStr,
      typeBuilderProvider = { retrofitServiceClassName }
    )
    val genFun = retrofitServiceHelper.genRetrofitServiceFuncCode()
    retrofitServiceCodeBuilder.addFunction(genFun.build(), true)
  }

  private fun generatorRepository(repositoryHelper: RepositoryHelper) {
    val repositoryClassNameStr = repositoryHelper.repositoryClassName.simpleName
    val repositoryClassName = repositoryHelper.genClassBuilder()
    val repositoryCodeBuilder = CodeBuilder.getOrCreate(repositoryPackageName,
      repositoryClassNameStr, typeBuilderProvider = {repositoryClassName})
    val genFun = repositoryHelper.genRepositoryFuncCode()
    repositoryCodeBuilder.addFunction(genFun.build(), true)
  }

  private fun generatorResponse(
    responseHelper: ResponseHelper
  ) {
    val responseClassNameStr = responseHelper.responseClassName.simpleName
    val responseClassName = responseHelper.genClassBuilder()
    CodeBuilder.getOrCreate(responsePackageName,
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
      apiModelPackageName,
      apiModelClassNameStr,
      typeBuilderProvider = {
        apiModelClassName
      }
    )
    val toDomainModel = apiModelHelper.toDomainModel(
      apiModelClassNameStr,
      apiModelPackageName,
      data
    )
    apiModelCodeBuilder.addFunction(toDomainModel, false)
  }
}








