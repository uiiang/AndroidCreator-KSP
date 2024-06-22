package uii.ang.creator.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.writeTo
import uii.ang.creator.annotation.*
import uii.ang.creator.codegen.CodeBuilder
import uii.ang.creator.template.showcase.ApiModelHelper
import uii.ang.creator.template.showcase.ResponseHelper
import uii.ang.creator.template.showcase.RetrofitServiceHelper
import kotlin.collections.ArrayList

class CreatorProcessor(
  private val environment: SymbolProcessorEnvironment,
) : SymbolProcessor {
  private val codeGenerator: CodeGenerator = environment.codeGenerator
  private val logger: KSPLogger = environment.logger

  @Synchronized
  override fun process(resolver: Resolver): List<KSAnnotated> {
    val creatorAnnotated = resolver.getSymbolsWithAnnotation(Creator::class.qualifiedName!!)
    if (creatorAnnotated.count() == 0) {
      logger.warn("not found use Creator Annotation Class")
      return emptyList()
    }
    CodeBuilder.clear()
    logger.warn("found CreatorAnnotation ${creatorAnnotated.count()}")
    val data = CreatorCollector.collect(resolver, logger)
    generateMappingCode(resolver, data)
    writeFiles()

    val unableToProcess = creatorAnnotated.filterNot { it.validate() }
    return unableToProcess.toList()
  }

  private fun writeFiles() {
    CodeBuilder.all().forEach {
      it.build().writeTo(
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