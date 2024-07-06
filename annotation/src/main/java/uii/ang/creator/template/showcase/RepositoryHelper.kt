package uii.ang.creator.template.showcase

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import uii.ang.creator.processor.Const.baseDomainResultClassName
import uii.ang.creator.processor.CreatorData
import uii.ang.creator.processor.ProcessorHelper
import uii.ang.creator.processor.Utils.convertType
import uii.ang.creator.processor.Utils.findParseReturnChain
import uii.ang.creator.processor.Utils.getRequestParamWithoutBody
import uii.ang.creator.processor.Utils.getRequestParameterSpecBody
import uii.ang.creator.processor.Utils.getRequestParameterSpecBodyWithMap
import uii.ang.creator.processor.Utils.getRequestParameterSpecList
import uii.ang.creator.processor.Utils.requestParamHasBody
import uii.ang.creator.processor.Utils.requestParamHasMap

class RepositoryHelper(
  logger: KSPLogger,
  data: CreatorData
) : ProcessorHelper(logger, data) {

  fun genClassBuilder(): TypeSpec.Builder {
    val classBuilder = TypeSpec.interfaceBuilder(repositoryInterfaceClassName)
      .addModifiers(KModifier.INTERNAL)
    return classBuilder
  }

  fun genRepositoryFuncCode(): FunSpec.Builder {
    val anno = data.annotationData
    val methodName = anno.methodName
    val generateParameters = anno.parameters
    // 生成如下代码
//    internal interface AlbumRepository {
//      suspend fun getAlbumInfo(artistName: String, albumName: String, mbId: String?):
//        Result<Album>
//      suspend fun searchAlbum(phrase: String?):
//        Result<List<Album>>
//    }
    val noBodyParamList = getRequestParamWithoutBody(generateParameters)
    val hasBody = requestParamHasBody(generateParameters)
    val hasMap = requestParamHasMap(generateParameters)

    val genFunction = FunSpec.builder(methodName)
      .addModifiers(KModifier.ABSTRACT, KModifier.SUSPEND)
    val parameterSpecList = getRequestParameterSpecList(noBodyParamList)
    genFunction.addParameters(parameterSpecList)
    if (hasBody) {
      val bodyParamSpec = getRequestParameterSpecBody(methodName)
      genFunction.addParameter(bodyParamSpec.build())
    }
    if (hasMap) {
      val bodyParamSpec = getRequestParameterSpecBodyWithMap()
      genFunction.addParameter(bodyParamSpec.build())
    }
    val returnChain = findParseReturnChain(data.sourceClassDeclaration, logger)
    if (returnChain.values.isNotEmpty()) {
      genFunction.returns(baseDomainResultClassName.parameterizedBy(returnChain.values.last()))
    }
    return genFunction
  }


}