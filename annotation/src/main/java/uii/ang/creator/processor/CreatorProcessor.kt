package uii.ang.creator.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ksp.writeTo
import uii.ang.creator.*
import uii.ang.creator.annotation.Creator
import uii.ang.creator.codegen.CodeBuilder
import uii.ang.creator.processor.CollectCodeHelper.genClassBuilder

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

    val unableToProcess = creatorAnnotated.filterNot { it.validate() }
    return unableToProcess.toList()
  }

  private fun KSNode.findParentClass(): KSClassDeclaration? {
    var currentParent: KSNode? = parent
    while (currentParent !is KSClassDeclaration) {
      currentParent = parent?.parent
      if (currentParent == null) {
        return null
      }
    }
    return currentParent
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
//      fileSpec.writeTo(path)
      // 生成文件到build/generated/ksp/main/kotlin
      fileSpec.writeTo(
        codeGenerator,
        aggregating = true, // always aggregating, as any new file could be a mapper with higher prio than a potentially used one.
      )
    }

    genClassBuilder(CodeBuilder.allCollectKoin(), logger).forEach {
      it.writeTo(codeGenerator, aggregating = true)
    }
  }

  private fun generateMappingCode(resolver: Resolver, converterData: List<AnnotatedBaseData>) {
    converterData.forEachIndexed { index, annotatedBaseData ->
      when (annotatedBaseData) {
        is CreatorData -> CreatorCodeGenerator.generate(annotatedBaseData, resolver, logger, index, converterData.count())
      }
    }
  }
}










