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
    val genFunction = FunSpec.builder(methodName)
      .addModifiers(KModifier.ABSTRACT, KModifier.SUSPEND)
    generateParameters.forEach { param ->
      val paramSpec = ParameterSpec.builder(param.paramName, convertType(param.paramType))
      if (param.paramDefault.isNotEmpty()) {
        if (param.paramType == "String") {
          paramSpec.defaultValue("\"${param.paramDefault}\"")
        } else {
          paramSpec.defaultValue(param.paramDefault)
        }
      }
      genFunction.addParameter(
        paramSpec.build()
      )
    }
    val returnChain = findParseReturnChain(data.sourceClassDeclaration, logger)
    if (returnChain.values.isNotEmpty()) {
      genFunction.returns(baseDomainResultClassName.parameterizedBy(returnChain.values.last()))
    }
    return genFunction
  }


}