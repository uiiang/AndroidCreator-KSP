package uii.ang.creator.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.squareup.kotlinpoet.ClassName
import uii.ang.creator.codegen.CodeBuilder
import uii.ang.creator.processor.Const.apiModelPackageName
import uii.ang.creator.processor.Const.repositoryImplPackageName
import uii.ang.creator.processor.Const.repositoryPackageName
import uii.ang.creator.processor.Const.responsePackageName
import uii.ang.creator.processor.Const.retrofitServicePackageName
import uii.ang.creator.template.showcase.*

object CreatorCodeGenerator {
  fun generate(data: CreatorData, resolver: Resolver, logger: KSPLogger) {

//    val processorHelper = ProcessorHelper(logger, data, basePackageName)
    val apiModelHelper = ApiModelHelper(logger, data)
    val responseHelper = ResponseHelper(logger, data)
    val retrofitServiceHelper = RetrofitServiceHelper(logger, data)
    val repositoryHelper = RepositoryHelper(logger, data)
    val repositoryImplHelper = RepositoryImplHelper(logger, data)

    // 生成ApiDomain代码
    if (data.generateApiModel) {
      generatorApiModel(apiModelHelper, data)
    }

    // 生成Response代码
    if (data.generateResponse) {
      generatorResponse(responseHelper)
    }

    // 生成Retrofit代码
    if (data.generateRetrofitService) {
      val retrofitServiceClassName = generatorRetrofitService(retrofitServiceHelper)
      generatorRepository(repositoryHelper, repositoryImplHelper)
    }
  }

  private fun generatorRetrofitService(
    retrofitServiceHelper: RetrofitServiceHelper
  ): ClassName {
    val retrofitServiceClassNameStr = retrofitServiceHelper.retrofitServiceClassName.simpleName
    val retrofitServiceClassName = retrofitServiceHelper.genClassBuilder()
    val retrofitServiceCodeBuilder = CodeBuilder.getOrCreate(retrofitServicePackageName,
      retrofitServiceClassNameStr,
      typeBuilderProvider = { retrofitServiceClassName }
    )
    val genFun = retrofitServiceHelper.genRetrofitServiceFuncCode()
    retrofitServiceCodeBuilder.addFunction(genFun.build(), true)
    return retrofitServiceHelper.retrofitServiceClassName
  }

  private fun generatorRepository(
    repositoryHelper: RepositoryHelper,
    repositoryImplHelper: RepositoryImplHelper
  ) {
    val repositoryInterfaceClassNameStr = repositoryHelper.repositoryInterfaceClassName.simpleName
    val repositoryInterfaceClassName = repositoryHelper.genClassBuilder()
    val repositoryInterfaceCodeBuilder = CodeBuilder.getOrCreate(repositoryPackageName,
      repositoryInterfaceClassNameStr, typeBuilderProvider = { repositoryInterfaceClassName })
    val genInterfaceFun = repositoryHelper.genRepositoryFuncCode()
    repositoryInterfaceCodeBuilder.addFunction(genInterfaceFun.build(), true)

    val repositoryImplClassNameStr = repositoryImplHelper.repositoryImplClassName.simpleName
    val repositoryImplClassName = repositoryImplHelper.genClassBuilder()
    val repositoryImplCodeBuilder = CodeBuilder.getOrCreate(
      repositoryImplPackageName,
      repositoryImplClassNameStr, typeBuilderProvider = { repositoryImplClassName })
    val genImplFun = repositoryImplHelper.genRepositoryFuncCode()
    repositoryImplCodeBuilder.addFunction(genImplFun.build(), true)
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








