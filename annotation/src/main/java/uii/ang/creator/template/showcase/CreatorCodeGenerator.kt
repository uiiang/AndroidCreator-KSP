package uii.ang.creator.template.showcase

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.squareup.kotlinpoet.CodeBlock
import uii.ang.creator.annotation.apiTypeKtor
import uii.ang.creator.annotation.apiTypeRetrofit
import uii.ang.creator.annotation.requestParamTypeBody
import uii.ang.creator.codegen.CodeBuilder
import uii.ang.creator.processor.Const.apiModelPackageName
import uii.ang.creator.processor.Const.apiServicePackageName
import uii.ang.creator.processor.Const.dataModulePackageName
import uii.ang.creator.processor.Const.databasePackageName
import uii.ang.creator.processor.Const.domainModulePackageName
import uii.ang.creator.processor.Const.entityModelPackageName
import uii.ang.creator.processor.Const.koinApiServiceModuleGenName
import uii.ang.creator.processor.Const.koinDataModuleGenName
import uii.ang.creator.processor.Const.koinDomainModuleGenName
import uii.ang.creator.processor.Const.repositoryImplPackageName
import uii.ang.creator.processor.Const.repositoryPackageName
import uii.ang.creator.processor.Const.requestBodyPackageName
import uii.ang.creator.processor.Const.responsePackageName
import uii.ang.creator.processor.Const.retrofitServicePackageName
import uii.ang.creator.processor.Const.useCasePackageName
import uii.ang.creator.processor.CreatorData
import uii.ang.creator.tools.firstCharUpperCase

object CreatorCodeGenerator {
  val entityModelList = mutableListOf<CodeBlock>()
  val entitiesCodeBlock = CodeBlock.builder()
  fun generate(
    data: CreatorData, resolver: Resolver, logger: KSPLogger,
    index: Int, allSize: Int
  ) {
    if (data.generateApiType == apiTypeRetrofit) {
//    val processorHelper = ProcessorHelper(logger, data, basePackageName)
      val apiModelHelper = ApiModelHelper(logger, data)
      val responseHelper = ResponseHelper(logger, data)
      val retrofitServiceHelper = RetrofitServiceHelper(logger, data)
      val repositoryHelper = RepositoryHelper(logger, data)
      val repositoryImplHelper = RepositoryImplHelper(logger, data)
      val useCaseHelper = UseCaseHelper(logger, data)
      val entityModelHelper = EntityModelHelper(logger, data)
      var daoHelper = DaoHelper(logger, data)
      // 生成ApiDomain代码
      if (data.generateApiModel) {
        generatorApiModel(logger, data)
      }
      if (data.generateEntityModel) {
        generatorEntityModel(entityModelHelper, data)
        generatorDao(daoHelper, data, logger, index, allSize)
      }

//      val hasBody = data.annotationData.parameters.any { param -> param.paramQueryType == requestParamTypeBody }
//      if (hasBody) {
//        generatorQueryBodyObj(queryBodyHelper, data)
//      }

      // 生成Response代码
      if (data.generateResponse) {
        generatorResponse(logger, data)
      }

      // 生成Retrofit代码
      if (data.generateRetrofitService) {
        generatorRetrofitService(retrofitServiceHelper, logger)
        generatorRepository(repositoryHelper, repositoryImplHelper, logger)
      }

      generatorUseCase(useCaseHelper, logger)
    } else if (data.generateApiType == apiTypeKtor) {
      val generateApiService = data.generateApiService
      if (generateApiService) {
        generatorApiModel(logger, data)
        generatorApiService(logger, data)
        generatorRepositoryKtor(logger, data)
        generatorRepositoryImplKtor(logger, data)
        generatorUseCaseKtor(logger, data)
        generatorQueryBodyObj(logger, data)
        generatorResponse(logger, data)
      }
//      if (data.generateApiModel) {
//        val apiModelHelper = ApiModelHelper(logger, data)
//        generatorApiModel(apiModelHelper, data)
//      }
    }
  }

  private fun generatorQueryBodyObj(logger: KSPLogger, data: CreatorData) {
    val queryBodyHelper = RequestQueryBodyHelper(logger, data)
    data.annotationData.methodName
    val queryBodyObjClassNameStr = queryBodyHelper.requestBodyClassName.simpleName
    val queryBodyClassName = queryBodyHelper.genClassBuilder()
    val queryBodyCodeBuilder = CodeBuilder.getOrCreate(
      requestBodyPackageName, queryBodyObjClassNameStr, typeBuilderProvider = { queryBodyClassName }
    )
  }

  private fun generatorUseCaseKtor(logger: KSPLogger, data: CreatorData) {
    val useCaseHelper = UseCaseKtorHelper(logger, data)
    if (useCaseHelper.data.annotationData.methodName.isNotEmpty()) {
      val useCaseClassNameStr = useCaseHelper.userCaseGenClassName.simpleName
      val useCaseClassBuilder = useCaseHelper.genClassBuilder()
      CodeBuilder.getOrCreate(useCasePackageName, useCaseClassNameStr,
        typeBuilderProvider = { useCaseClassBuilder })

      val useCaseKoinCodeBlock = useCaseHelper.genKoinInjectionCode()
      CodeBuilder.putCollectCodeBlock(
        domainModulePackageName,
        koinDomainModuleGenName,
        useCaseKoinCodeBlock.build(), logger
      )
    }
  }

  private fun generatorRepositoryImplKtor(
    logger: KSPLogger, data: CreatorData
  ) {
    val repositoryImplHelper = RepositoryKtorImplHelper(logger, data)
    val repositoryImplClassNameStr = repositoryImplHelper.repositoryImplClassName.simpleName
    val repositoryImplClassName = repositoryImplHelper.genClassBuilder()
    CodeBuilder.getOrCreate(repositoryImplPackageName,
      repositoryImplClassNameStr,
      typeBuilderProvider = { repositoryImplClassName }
    )

    val repositoryKoinCodeBlock = repositoryImplHelper.genKoinInjectionCode()
    CodeBuilder.putCollectCodeBlock(
      dataModulePackageName,
      koinDataModuleGenName,
      repositoryKoinCodeBlock.build(), logger
    )
  }

  private fun generatorRepositoryKtor(
    logger: KSPLogger,
    data: CreatorData
  ) {
    val repositoryHelper = RepositoryKtorHelper(logger, data)
    val repositoryInterfaceClassNameStr = repositoryHelper.repositoryInterfaceClassName.simpleName

    CodeBuilder.getOrCreate(repositoryPackageName,
      repositoryInterfaceClassNameStr,
      typeBuilderProvider = { repositoryHelper.genClassBuilder() }
    )
  }

  private fun generatorApiService(
    logger: KSPLogger,
    data: CreatorData
  ) {
    val apiServiceHelper = ApiServiceHelper(logger, data)
    CodeBuilder.getOrCreate(
      apiServicePackageName,
      apiServiceHelper.apiServiceClassName.simpleName,
      typeBuilderProvider = { apiServiceHelper.genClassBuilder() }
    )

    val repositoryKoinCodeBlock = apiServiceHelper.genKoinInjectionCode()
    CodeBuilder.putCollectCodeBlock(
      apiServicePackageName,
      koinApiServiceModuleGenName,
      repositoryKoinCodeBlock.build(), logger
    )
  }

  private fun generatorUseCase(useCaseHelper: UseCaseHelper, logger: KSPLogger) {
    if (useCaseHelper.data.annotationData.methodName.isNotEmpty()) {
      val useCaseClassNameStr = useCaseHelper.userCaseGenClassName.simpleName
      val useCaseClassBuilder = useCaseHelper.genClassBuilder()
      CodeBuilder.getOrCreate(useCasePackageName, useCaseClassNameStr,
        typeBuilderProvider = { useCaseClassBuilder })
      val genFun = useCaseHelper.genUseCaseFunCode()
      useCaseClassBuilder.addFunction(genFun.build())

      val useCaseKoinCodeBlock = useCaseHelper.genKoinInjectionCode()
      CodeBuilder.putCollectCodeBlock(
        domainModulePackageName,
        koinDomainModuleGenName,
        useCaseKoinCodeBlock.build(), logger
      )
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

    CodeBuilder.putCollectCodeBlock(
      dataModulePackageName,
      koinDataModuleGenName,
      retrofitServiceKoinCodeBlock.build(), logger
    )
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

    CodeBuilder.putCollectCodeBlock(
      dataModulePackageName,
      koinDataModuleGenName,
      repositoryKoinCodeBlock.build(), logger
    )
  }

  private fun generatorResponse(logger: KSPLogger, data: CreatorData) {
    val responseHelper = ResponseHelper(logger, data)
    val responseClassNameStr = responseHelper.responseClassName.simpleName
    val responseClassName = responseHelper.genClassBuilder()
    CodeBuilder.getOrCreate(responsePackageName,
      responseClassNameStr,
      typeBuilderProvider = { responseClassName }
    )
  }


  private fun generatorDao(
    daoHelper: DaoHelper, data: CreatorData,
    logger: KSPLogger,
    index: Int, allSize: Int
  ) {
    val daoNameStr = daoHelper.roomDaoInterfaceClassName.simpleName
    logger.warn("generatorDao -> $daoNameStr index=$index allSize=$allSize")
    val dao = daoHelper.genClassBuilder()
    CodeBuilder.getOrCreate(databasePackageName,
      daoNameStr,
      typeBuilderProvider = { dao }
    )
//    logger.warn("==============================================")
//    val entities = daoHelper.genRoomAnnotationDatabaseEntitiesClassName()
//    logger.warn("gen projdatabase get dao name =${entities.simpleName}")
////    val entitiesCodeBlock =  CodeBlock.builder()
//    entitiesCodeBlock.addStatement("%T::class", entities)
//    logger.warn("========codeblock")
//    logger.warn("${entitiesCodeBlock.build().toString()}")
////    entityModelList.add(entitiesCodeBlock.build())
////    val genCodeBlock = CodeBlock.builder()
////    entityModelList.onEach {
////      logger.warn("add dao ${it}")
////      genCodeBlock.addStatement("%T::class", entities)
////    }
////      .addStatement(entitiesCodeBlock.build())
//    val entitiesAnno = CodeBlock.builder()
//      .addStatement("entities = [")
//      .add(entitiesCodeBlock.build())
//      .addStatement("]")
//    logger.warn("entitiesAnno == ${entitiesAnno.build()}")
//    logger.warn("entityModelList.size = ${entityModelList.count()}")
//    val databaseAnno = AnnotationSpec.builder(roomDatabaseClassName)
//      .addMember(entitiesAnno.build())
//      .addMember("version = 1")
//      .addMember("exportSchema = false")
//    val inDatabaseCode = daoHelper.genInDatabaseCodeBlock()
//    val database = daoHelper.genDatabaseClass()
//    if (index == allSize) {
//      database.addAnnotation(databaseAnno.build())
//    }
////    logger.warn("entityModelList count ${entityModelList.count()}")
//    CodeBuilder.getOrCreate(databasePackageName, "ProjDatabase",
//      typeBuilderProvider = { database })
//      .addFunction(inDatabaseCode.build(), true)
  }

  private fun generatorEntityModel(entityModelHelper: EntityModelHelper, data: CreatorData) {
//    entityModelList.add(CodeBlock.builder().add("%T::class", entityModelHelper.entityModelClassName).build())
    val entityModelNameStr = entityModelHelper.entityModelClassName.simpleName
    val entityModelName = entityModelHelper.genClassBuilder()
    val entityModelBuilder = CodeBuilder.getOrCreate(
      entityModelPackageName,
      entityModelNameStr,
      typeBuilderProvider = {
        entityModelName
      }
    )
    val toEntityModel = entityModelHelper.toEntityModel(
      entityModelNameStr,
      entityModelPackageName,
      data
    )
    val toDomainModel = entityModelHelper.toDomainModel(
      entityModelNameStr,
      entityModelPackageName,
      data
    )
    // 循环数据类的构造参数，用非基本类型的字段生成TypeConvert
    data.primaryConstructorParameters.filter { !it.isBaseType }
      .map {
        entityModelHelper.genTypeConverter(it)
      }.onEach {
        entityModelBuilder.addType(it)
      }
    entityModelBuilder.addFunction(toDomainModel, false)
    entityModelBuilder.addFunction(toEntityModel, false)
  }

  private fun generatorApiModel(logger: KSPLogger, data: CreatorData) {
    val apiModelHelper = ApiModelHelper(logger, data)
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
    if (data.generateEntityModel) {
      val toEntityModel = apiModelHelper.toEntityModel(
        apiModelClassNameStr,
        apiModelPackageName, data
      )
      apiModelCodeBuilder.addFunction(toEntityModel, false)
    }
  }
}








