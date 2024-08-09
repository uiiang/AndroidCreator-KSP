package uii.ang.creator.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import uii.ang.creator.annotation.requestParamTypeBody
import uii.ang.creator.codegen.CodeBuilder
import uii.ang.creator.processor.Const.apiModelPackageName
import uii.ang.creator.processor.Const.dataModulePackageName
import uii.ang.creator.processor.Const.databasePackageName
import uii.ang.creator.processor.Const.domainModulePackageName
import uii.ang.creator.processor.Const.entityModelPackageName
import uii.ang.creator.processor.Const.koinDataModuleGenName
import uii.ang.creator.processor.Const.koinDomainModuleGenName
import uii.ang.creator.processor.Const.repositoryImplPackageName
import uii.ang.creator.processor.Const.repositoryPackageName
import uii.ang.creator.processor.Const.requestBodyPackageName
import uii.ang.creator.processor.Const.responsePackageName
import uii.ang.creator.processor.Const.retrofitServicePackageName
import uii.ang.creator.processor.Const.roomDatabaseClassName
import uii.ang.creator.processor.Const.useCasePackageName
import uii.ang.creator.template.showcase.*
import uii.ang.creator.tools.firstCharUpperCase

object CreatorCodeGenerator {
  val entityModelList = mutableListOf<CodeBlock>()
  fun generate(data: CreatorData, resolver: Resolver, logger: KSPLogger) {

//    val processorHelper = ProcessorHelper(logger, data, basePackageName)
    val apiModelHelper = ApiModelHelper(logger, data)
    val responseHelper = ResponseHelper(logger, data)
    val retrofitServiceHelper = RetrofitServiceHelper(logger, data)
    val repositoryHelper = RepositoryHelper(logger, data)
    val repositoryImplHelper = RepositoryImplHelper(logger, data)
    val useCaseHelper = UseCaseHelper(logger, data)
    val queryBodyHelper = RequestQueryBodyHelper(logger, data)
    val entityModelHelper = EntityModelHelper(logger, data)
    var daoHelper = DaoHelper(logger, data)
    // 生成ApiDomain代码
    if (data.generateApiModel) {
      generatorApiModel(apiModelHelper, data)
    }
    if (data.generateEntityModel) {
      generatorEntityModel(entityModelHelper, data)
      generatorDao(daoHelper, data, logger)
    }

    val hasBody = data.annotationData.parameters.any { param -> param.paramQueryType == requestParamTypeBody }
    if (hasBody) {
      generatorQueryBodyObj(queryBodyHelper, data)
    }

    // 生成Response代码
    if (data.generateResponse) {
      generatorResponse(responseHelper)
    }

    // 生成Retrofit代码
    if (data.generateRetrofitService) {
      generatorRetrofitService(retrofitServiceHelper, logger)
      generatorRepository(repositoryHelper, repositoryImplHelper, logger)
    }

    generatorUseCase(useCaseHelper, logger)
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

  private fun generatorResponse(responseHelper: ResponseHelper) {
    val responseClassNameStr = responseHelper.responseClassName.simpleName
    val responseClassName = responseHelper.genClassBuilder()
    CodeBuilder.getOrCreate(responsePackageName,
      responseClassNameStr,
      typeBuilderProvider = { responseClassName }
    )
  }

  private fun generatorQueryBodyObj(queryBodyHelper: RequestQueryBodyHelper, data: CreatorData) {
    data.annotationData.methodName
    val queryBodyObjClassNameStr = "${data.annotationData.methodName.firstCharUpperCase()}QueryBody"
    val queryBodyClassName = queryBodyHelper.genClassBuilder()
    val queryBodyCodeBuilder = CodeBuilder.getOrCreate(
      requestBodyPackageName, queryBodyObjClassNameStr, typeBuilderProvider = { queryBodyClassName }
    )
  }

  private fun generatorDao(
    daoHelper: DaoHelper, data: CreatorData,
    logger: KSPLogger
  ) {
    val daoNameStr = daoHelper.roomDaoInterfaceClassName.simpleName
    logger.warn("generatorDao -> $daoNameStr")
    val dao = daoHelper.genClassBuilder()
    CodeBuilder.getOrCreate(databasePackageName,
      daoNameStr,
      typeBuilderProvider = { dao }
    )
    val databaseAnno = AnnotationSpec.builder(roomDatabaseClassName)
      .addMember("entities = []")
      .addMember("version = 1")
      .addMember("exportSchema = false")
    val inDatabaseCode = daoHelper.genInDatabaseCodeBlock()
    val database = daoHelper.genDatabaseClass()
      .addAnnotation(databaseAnno.build())
//    logger.warn("entityModelList count ${entityModelList.count()}")
    CodeBuilder.getOrCreate(databasePackageName, "ProjDatabase",
      typeBuilderProvider = { database })
      .addFunction(inDatabaseCode.build(), true)
  }

  private fun generatorEntityModel(entityModelHelper: EntityModelHelper, data: CreatorData) {
    entityModelList.add(CodeBlock.builder().add("%T::class", entityModelHelper.entityModelClassName).build())
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
    if (data.generateEntityModel) {
      val toEntityModel = apiModelHelper.toEntityModel(
        apiModelClassNameStr,
        apiModelPackageName, data
      )
      apiModelCodeBuilder.addFunction(toEntityModel, false)
    }
  }
}








