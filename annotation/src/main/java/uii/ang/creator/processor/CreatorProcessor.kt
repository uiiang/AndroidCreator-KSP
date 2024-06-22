package uii.ang.creator.processor

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ksp.writeTo
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

    val creatorAnnotated = resolver.getSymbolsWithAnnotation(Creator::class.qualifiedName!!).toList()

    creatorAnnotated.forEach {
      it.containingFile?.let { cf ->
        logger.warn("containingFile filePath ${cf.filePath} ${cf.packageName} ${cf.fileName}")
      }
    }
    if (creatorAnnotated.isEmpty()) {
      logger.warn("not found use Creator Annotation Class")
      return emptyList()
    }
    CodeBuilder.clear()
    logger.warn("found CreatorAnnotation ${creatorAnnotated.count()}")
    val data = CreatorCollector.collect(resolver, logger)
    generateMappingCode(resolver, data)
    writeFiles()

    resolver.getAllFiles().forEach {
      logger.warn("allFile fileName=${it.fileName} filePath=${it.filePath} packageName=${it.packageName.getShortName()}")
    }

    val unableToProcess = creatorAnnotated.filterNot { it.validate() }
    return unableToProcess.toList()
  }

  private fun writeFiles() {
    CodeBuilder.allBuilder().forEach {
      val fileSpec = it.build()
//      logger.warn(" content ${fileSpec} ")
      // 生成文件到指定目录
//      val path = File("Test")
//      logger.warn("writeFiles path=${path.absolutePath} relativePath=${fileSpec.relativePath}")
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