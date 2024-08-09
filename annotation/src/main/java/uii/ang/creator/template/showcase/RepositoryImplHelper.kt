package uii.ang.creator.template.showcase

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import uii.ang.creator.processor.Const.baseDomainResultClassName
import uii.ang.creator.processor.Const.baseRetrofitApiResultClassName
import uii.ang.creator.processor.Const.databasePackageName
import uii.ang.creator.processor.Const.moduleToDomainMemberName
import uii.ang.creator.processor.Const.moduleToEntityMemberName
import uii.ang.creator.processor.Const.timberClassName
import uii.ang.creator.processor.CreatorData
import uii.ang.creator.processor.ProcessorHelper
import uii.ang.creator.processor.Utils.findParseReturnChain
import uii.ang.creator.processor.Utils.getRequestParamWithoutBody
import uii.ang.creator.processor.Utils.getRequestParameterSpecBody
import uii.ang.creator.processor.Utils.getRequestParameterSpecBodyWithMap
import uii.ang.creator.processor.Utils.getRequestParameterSpecList
import uii.ang.creator.processor.Utils.requestParamHasBody
import uii.ang.creator.processor.Utils.requestParamHasMap
import uii.ang.creator.tools.firstCharLowerCase
import uii.ang.creator.tools.firstCharUpperCase
import uii.ang.creator.tools.isList

class RepositoryImplHelper(
  logger: KSPLogger,
  data: CreatorData
) : ProcessorHelper(logger, data) {

  fun genClassBuilder(): TypeSpec.Builder {
    val flux = FunSpec.constructorBuilder()
//    val returnChain = findParseReturnChain(data.sourceClassDeclaration, logger)
//    val daoInterfaceClassName =
//      ClassName(databasePackageName, returnChain.keys.last().getShortName().firstCharUpperCase() + "Dao")

//    logger.warn("获得dao类名， ${roomDaoInterfaceClassName.simpleName}, 拼装类名 ${daoInterfaceClassName.simpleName}")
    flux.addParameter(
      ParameterSpec.builder(
        retrofitServiceClassName.simpleName.firstCharLowerCase(), retrofitServiceClassName
      ).build()
//    ).addParameter(
//      ParameterSpec.builder(
//        "dao", daoInterfaceClassName
//      ).build()
    ).build()

    val retrofitServiceProp = PropertySpec.builder(
      retrofitServiceClassName.simpleName.firstCharLowerCase(),
      retrofitServiceClassName
    )
      .addModifiers(KModifier.PRIVATE)
      .initializer(retrofitServiceClassName.simpleName.firstCharLowerCase())
//    val roomDaoInterfaceProp = PropertySpec.builder(
//      "dao",
//      daoInterfaceClassName
//    )
//      .addModifiers(KModifier.PRIVATE)
//      .initializer("dao")
    val classBuilder = TypeSpec.classBuilder(repositoryImplClassName)
//      .addModifiers(KModifier.INTERNAL)
      .addSuperinterfaces(listOf(repositoryInterfaceClassName))
      .primaryConstructor(flux.build())
      .addProperty(retrofitServiceProp.build())
//      .addProperty(roomDaoInterfaceProp.build())
//      .superclass(repositoryInterfaceClassName)
    return classBuilder
  }

  fun genRepositoryFuncCode(): FunSpec.Builder {
    val anno = data.annotationData
    val methodName = anno.methodName
    val generateParameters = anno.parameters
    val returnChain = findParseReturnChain(data.sourceClassDeclaration, logger)
    // 生成如下代码
//    override suspend fun searchAlbum(phrase: String?): Result<List<Album>> =
//      when (val apiResult = albumRetrofitService.searchAlbumAsync(phrase)) {
//        is ApiResult.Success -> {
//          val albums = apiResult
//            .data
//            .results
//            .albumMatches
//            .album
//            .also { albumsApiModels ->
//              val albumsEntityModels = albumsApiModels.map { it.toEntityModel() }
//              albumDao.insertAlbums(albumsEntityModels)
//            }
//            .map { it.toDomainModel() }
//
//          Result.Success(albums)
//        }
//        is ApiResult.Error -> {
//          Result.Failure()
//        }
//        is ApiResult.Exception -> {
//          Timber.e(apiResult.throwable)
//
//          val albums = albumDao
//            .getAll()
//            .map { it.toDomainModel() }
//
//          Result.Success(albums)
//        }
//      }
    val noBodyParamList = getRequestParamWithoutBody(generateParameters)
    val hasBody = requestParamHasBody(generateParameters)
    val hasMap = requestParamHasMap(generateParameters)

    val genFunction = FunSpec.builder(methodName)
      .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
    val parameterSpecList = getRequestParameterSpecList(noBodyParamList)
    genFunction.addParameters(parameterSpecList)
    var timberLogCodeBlock = CodeBlock.builder()
    var paramList = noBodyParamList.joinToString(",") { param ->
      param.paramName
    }
    if (hasBody) {
      timberLogCodeBlock = CodeBlock.builder()
        .addStatement(
          "%T.d(\"${retrofitServiceClassName.simpleName}\"+\"${methodName}QueryBody\"+" +
                  "\"params=\${paramData.toString()}\")",
          timberClassName
        )
      val bodyParamSpec = getRequestParameterSpecBody(methodName)
      genFunction.addParameter(bodyParamSpec.build())
      if (paramList.isNotEmpty()) {
        paramList += ", "
      }
      paramList += "paramData"
    }
    if (hasMap) {
      val bodyParamSpec = getRequestParameterSpecBodyWithMap()
      genFunction.addParameter(bodyParamSpec.build())
      if (paramList.isNotEmpty()) {
        paramList += ", "
      }
      paramList += "paramMap"
    }
    // when (val apiResult = albumRetrofitService.searchAlbumAsync(phrase)) {

    val whenCodeBlock = "return when (val apiResult = " +
            "${retrofitServiceClassName.simpleName.firstCharLowerCase()}.${methodName}Async($paramList))"

    // is ApiResult.Success
    // 成功case时，使用反射链一直调用到需要return的字段
    val apiResultSuccessCodeBlock = CodeBlock.builder()
      .addStatement("is %T.Success -> {", baseRetrofitApiResultClassName)
      .addStatement("\tval result = apiResult.data")
    returnChain.forEach { (t, u) ->
      apiResultSuccessCodeBlock.addStatement("\t\t.${t.getShortName().firstCharLowerCase()}")
    }
//    // 如果需要插入数据库，使用.also{ dao.insert }
////    val albumsEntityModels = albumsApiModels.map { it.toEntityModel() }
////    albumDao.insertAlbums(albumsEntityModels)
//    apiResultSuccessCodeBlock.addStatement("\t\t.also{ apiModel ->")
//    apiResultSuccessCodeBlock.addStatement(
//      "\t\t\tval entityModels = apiModel.map { it.%M() }",
//      moduleToEntityMemberName
//    )
//    apiResultSuccessCodeBlock.addStatement("\t\t\tdao.insert(entityModels)")
//    apiResultSuccessCodeBlock.addStatement("\t\t}")

    // 返回toDomainModel
    if (returnChain.values.last().isList()) {
      apiResultSuccessCodeBlock.addStatement("\t\t.map { it.%M() }", moduleToDomainMemberName)
    } else {
      apiResultSuccessCodeBlock.addStatement("\t\t.%M()", moduleToDomainMemberName)
    }
    apiResultSuccessCodeBlock
      .addStatement("\t%T.Success(result)", baseDomainResultClassName)
      .addStatement("}")

    //is ApiResult.Error
    val apiResultErrorCodeBlock = CodeBlock.builder()
      .addStatement("is %T.Error -> {", baseRetrofitApiResultClassName)
      .addStatement("\t%T.Failure()", baseDomainResultClassName)
      .addStatement("}")

    //is ApiResult.Exception
    val apiResultExceptionCodeBlock = CodeBlock.builder()
      .addStatement("is %T.Exception -> {", baseRetrofitApiResultClassName)
      .addStatement("\t%T.e(apiResult.throwable)", timberClassName)

    if (returnChain.values.last().isList()) {
      apiResultExceptionCodeBlock.addStatement("\t%T.Failure()", baseDomainResultClassName)
    } else {
      apiResultExceptionCodeBlock.addStatement("\t%T.Failure()", baseDomainResultClassName)
    }
    apiResultExceptionCodeBlock.addStatement("}")

    genFunction
      .addCode(timberLogCodeBlock.build())
      .beginControlFlow(whenCodeBlock)
      .addCode(apiResultSuccessCodeBlock.build())
      .addCode(apiResultErrorCodeBlock.build())
      .addCode(apiResultExceptionCodeBlock.build())
      .endControlFlow()


    if (returnChain.values.isNotEmpty()) {
      genFunction.returns(baseDomainResultClassName.parameterizedBy(returnChain.values.last()))
    }
    return genFunction
  }

  fun genKoinInjectionCode(): CodeBlock.Builder {
    return CodeBlock.builder()
      .addStatement("\tsingle<%T> { %T(get()) }", repositoryInterfaceClassName, repositoryImplClassName)
  }
}