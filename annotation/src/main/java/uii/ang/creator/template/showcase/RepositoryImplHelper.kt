package uii.ang.creator.template.showcase

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import uii.ang.creator.processor.Const.baseDomainResultClassName
import uii.ang.creator.processor.Const.baseRetrofitApiResultClassName
import uii.ang.creator.processor.Const.moduleToDomainMemberName
import uii.ang.creator.processor.Const.timberClassName
import uii.ang.creator.processor.CreatorData
import uii.ang.creator.processor.ProcessorHelper
import uii.ang.creator.processor.Utils.findParseReturnChain
import uii.ang.creator.tools.firstCharLowerCase
import uii.ang.creator.tools.isList

class RepositoryImplHelper(
  logger: KSPLogger,
  data: CreatorData
) : ProcessorHelper(logger, data) {

  fun genClassBuilder(): TypeSpec.Builder {
    val flux = FunSpec.constructorBuilder()
    flux.addParameter(
      ParameterSpec.builder(
        retrofitServiceClassName.simpleName.firstCharLowerCase(), retrofitServiceClassName
      ).build()
    ).build()

    val prop = PropertySpec.builder(
      retrofitServiceClassName.simpleName.firstCharLowerCase(),
      retrofitServiceClassName
    )
      .addModifiers(KModifier.PRIVATE)
      .initializer(retrofitServiceClassName.simpleName.firstCharLowerCase())
    val classBuilder = TypeSpec.classBuilder(repositoryImplClassName)
      .addModifiers(KModifier.INTERNAL)
      .addSuperinterfaces(listOf(repositoryInterfaceClassName))
      .primaryConstructor(flux.build())
      .addProperty(prop.build())
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
    val genFunction = FunSpec.builder(methodName)
      .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
    generateParameters.forEach { param ->
      val paramSpec = ParameterSpec.builder(param.paramName, convertType(param.paramType))
      genFunction.addParameter(
        paramSpec.build()
      )
    }

    // when (val apiResult = albumRetrofitService.searchAlbumAsync(phrase)) {
    val paramList = generateParameters.joinToString(",") { param ->
      param.paramName
    }
    val whenCodeBlock = "return when (val apiResult = " +
            "${retrofitServiceClassName.simpleName.firstCharLowerCase()}.${methodName}Async($paramList))"

    // is ApiResult.Success
    val apiResultSuccessCodeBlock = CodeBlock.builder()
      .addStatement("is %T.Success -> {", baseRetrofitApiResultClassName)
      .addStatement("\tval result = apiResult.data")
    returnChain.forEach { (t, u) ->
      apiResultSuccessCodeBlock.addStatement("\t\t.${t.getShortName().firstCharLowerCase()}")
    }
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
      apiResultExceptionCodeBlock.addStatement("\t%T.Success(emptyList())", baseDomainResultClassName)
    } else {
      apiResultExceptionCodeBlock.addStatement("\t%T.Success(null)")
    }
    apiResultExceptionCodeBlock.addStatement("}")

    genFunction.beginControlFlow(whenCodeBlock)
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

  private fun convertType(type: String) = when (type) {
    "String" -> String::class
    "Long" -> Long::class
    "Int" -> Int::class
    "Double" -> Double::class
    "Float" -> Float::class
    "Short" -> Short::class
    "Boolean" -> Boolean::class
    else -> String::class
  }

}