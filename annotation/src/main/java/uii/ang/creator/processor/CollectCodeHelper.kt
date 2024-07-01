package uii.ang.creator.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import uii.ang.creator.codegen.CollectCodeDesc
import uii.ang.creator.processor.Const.koinModuleClassName
import uii.ang.creator.processor.Const.koinModuleFunClassName
import uii.ang.creator.tools.firstCharLowerCase

object CollectCodeHelper {
  fun genClassBuilder(collectCodeList: List<CollectCodeDesc>, logger: KSPLogger): List<FileSpec> {
    val codeGroup = collectCodeList.groupBy { it.qualifiedName }
    logger.warn("准备生成依赖注入代码${collectCodeList.count()}个, 需要生成${codeGroup.count()}个文件")
    val fileSpecList = mutableListOf<FileSpec>()
    codeGroup.forEach { (t, u) ->
      logger.warn(" 注入代码分组$t")
      val fileSpec = FileSpec.builder(ClassName(t.packageName, t.fileName))
      val funSpec = FunSpec.builder(t.fileName.firstCharLowerCase())
        .addModifiers(KModifier.INTERNAL)
        .returns(koinModuleClassName)
        .addStatement("")
        .addStatement("return %T {", koinModuleFunClassName)
      u.map { it.codeBlock.build() }.onEach {
//        logger.warn("在文件 ${t.fileName} 中生成代码片段 $it")
        funSpec.addCode(it)
//        funSpec.addStatement("single<AlbumRepository> { AlbumRepositoryImpl(get(), get()) }")
      }
      funSpec.addStatement("}")
      fileSpec.addFunction(funSpec.build())
      fileSpecList.add(fileSpec.build())
    }
    return fileSpecList
  }
}