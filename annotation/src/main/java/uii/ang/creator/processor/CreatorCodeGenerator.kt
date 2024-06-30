package uii.ang.creator.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import uii.ang.creator.codegen.CodeBuilder
import uii.ang.creator.processor.Const.apiModelPackageName
import uii.ang.creator.processor.Const.dataModulePackageName
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
      generatorRetrofitService(retrofitServiceHelper, logger)
      logger.warn("generateRetrofitService ioc1 =${CodeBuilder.allCollect().count()}")
      generatorRepository(repositoryHelper, repositoryImplHelper, logger)
      logger.warn("generateRetrofitService ioc2 =${CodeBuilder.allCollect().count()}")
    }
  }

  private fun generatorRetrofitService(retrofitServiceHelper: RetrofitServiceHelper, logger: KSPLogger) {
    val retrofitServiceClassNameStr = retrofitServiceHelper.retrofitServiceClassName.simpleName
    val retrofitServiceClassName = retrofitServiceHelper.genClassBuilder()
    val retrofitServiceKoinCodeBlock = retrofitServiceHelper.genKoinInjectionCode()
    val retrofitServiceCodeBuilder = CodeBuilder.getOrCreate(
      retrofitServicePackageName,
      retrofitServiceClassNameStr,
      typeBuilderProvider = { retrofitServiceClassName },
    )
    val genFun = retrofitServiceHelper.genRetrofitServiceFuncCode()
    retrofitServiceCodeBuilder.addFunction(genFun.build(), true)

    CodeBuilder.putCollectCodeBlock(dataModulePackageName,
      "DataModuleGen",
      retrofitServiceKoinCodeBlock, logger)
    logger.warn("ioc1 ${CodeBuilder.allCollect().count()}")
  }

  private fun generatorRepository(
    repositoryHelper: RepositoryHelper,
    repositoryImplHelper: RepositoryImplHelper,
    logger: KSPLogger
  ) {
    val repositoryInterfaceClassNameStr = repositoryHelper.repositoryInterfaceClassName.simpleName
    val repositoryInterfaceClassName = repositoryHelper.genClassBuilder()
    val repositoryInterfaceCodeBuilder = CodeBuilder.getOrCreate(repositoryPackageName,
      repositoryInterfaceClassNameStr, typeBuilderProvider = { repositoryInterfaceClassName })
    val genInterfaceFun = repositoryHelper.genRepositoryFuncCode()
    repositoryInterfaceCodeBuilder.addFunction(genInterfaceFun.build(), true)

    val repositoryImplClassNameStr = repositoryImplHelper.repositoryImplClassName.simpleName
    val repositoryImplClassName = repositoryImplHelper.genClassBuilder()
    val repositoryKoinCodeBlock = repositoryImplHelper.genKoinInjectionCode()
    val repositoryImplCodeBuilder = CodeBuilder.getOrCreate(
      repositoryImplPackageName,
      repositoryImplClassNameStr,
      typeBuilderProvider = { repositoryImplClassName }
    )
    val genImplFun = repositoryImplHelper.genRepositoryFuncCode()
    repositoryImplCodeBuilder.addFunction(genImplFun.build(), true)

    CodeBuilder.putCollectCodeBlock(dataModulePackageName,
      "DataModuleGen",
      repositoryKoinCodeBlock, logger)
    logger.warn("ioc2 ${CodeBuilder.allCollect().count()}")
  }

  private fun generatorResponse(responseHelper: ResponseHelper) {
    val responseClassNameStr = responseHelper.responseClassName.simpleName
    val responseClassName = responseHelper.genClassBuilder()
    CodeBuilder.getOrCreate(responsePackageName,
      responseClassNameStr,
      typeBuilderProvider = { responseClassName }
    )
  }

  private fun generatorApiModel(apiModelHelper: ApiModelHelper, data: CreatorData) {
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








