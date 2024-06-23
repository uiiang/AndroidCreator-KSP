package uii.ang.creator.processor

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ksp.writeTo
import uii.ang.creator.*
import uii.ang.creator.annotation.*
import uii.ang.creator.codegen.CodeBuilder
import java.io.File

class CreatorProcessor(
  private val environment: SymbolProcessorEnvironment,
) : SymbolProcessor {
  private val codeGenerator: CodeGenerator = environment.codeGenerator
  private val logger: KSPLogger = environment.logger

  @Synchronized
  override fun process(resolver: Resolver): List<KSAnnotated> {
    val options = environment.options
    val creatorAnnotated = resolver.getSymbolsWithAnnotation(Creator::class.qualifiedName!!).toList()

    basePackageName = options[creator_base_package_name] ?: default_base_package_name
    modulePackageName = options[creator_module_package_name] ?: default_module_package_name

    if (creatorAnnotated.isEmpty()) {
      logger.warn("not found use Creator Annotation Class")
      return emptyList()
    }
    CodeBuilder.clear()
    logger.warn("found CreatorAnnotation ${creatorAnnotated.count()}")
    val data = CreatorCollector.collect(resolver, logger)
    generateMappingCode(resolver, data)

//    resolver.getAllFiles().forEach {
//      logger.warn("allFile fileName=${it.fileName} filePath=${it.filePath} packageName=${it.packageName.getShortName()}")
//    }

    val unableToProcess = creatorAnnotated.filterNot { it.validate() }
    return unableToProcess.toList()
  }

  override fun finish() {
    writeFiles()
  }

  private fun writeFiles() {
    // TODO 获取当前模块所在路径
    // TODO 获取当前模块包名
    CodeBuilder.allBuilder().forEach {
      val fileSpec = it.build()
//      logger.warn(" content ${fileSpec} ")
      // 生成文件到指定目录
//      val path = File("Test")
//      logger.warn("writeFiles relativePath=${fileSpec.relativePath}")
//      fileSpec.writeTo(path)
      // 生成文件到build/generated/ksp/main/kotlin
      fileSpec
        .writeTo(
        codeGenerator,
        aggregating = true, // always aggregating, as any new file could be a mapper with higher prio than a potentially used one.
      )
    }
  }
  private fun generateMappingCode(resolver: Resolver, converterData: List<AnnotatedBaseData>) {
    converterData.forEach {
      when (it) {
        is CreatorData -> CreatorCodeGenerator.generate(it, resolver, logger)
      }
    }
  }
}