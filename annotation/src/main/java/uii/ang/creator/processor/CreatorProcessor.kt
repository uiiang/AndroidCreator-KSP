package uii.ang.creator.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ksp.writeTo
import uii.ang.creator.*
import uii.ang.creator.annotation.Creator
import uii.ang.creator.annotation.ParseRoot
import uii.ang.creator.codegen.CodeBuilder

class CreatorProcessor(
  private val environment: SymbolProcessorEnvironment,
) : SymbolProcessor {
  private val codeGenerator: CodeGenerator = environment.codeGenerator
  private val logger: KSPLogger = environment.logger

  @Synchronized
  override fun process(resolver: Resolver): List<KSAnnotated> {

    val options = environment.options
    val creatorAnnotated = resolver.getSymbolsWithAnnotation(Creator::class.qualifiedName!!).toList()


//    val classToParseRootValuesMap =
//      mutableMapOf<KSClassDeclaration, MutableMap<String, String?>>()
//    val symbolsWithParseRootAnnotation =
//      resolver.getSymbolsWithAnnotation(ParseRoot::class.qualifiedName!!, true)
//    symbolsWithParseRootAnnotation.forEach { annotatedProperty ->
//      if (annotatedProperty is KSValueParameter) {
//        val parentClass = annotatedProperty.findParentClass()!!
//        logger.warn("annotatedProperty.name.getShortName=${annotatedProperty.name?.getShortName()}") //album
//        logger.warn("annotatedProperty.name.getQualifier=${annotatedProperty.name?.getQualifier()}") //
////        logger.warn("annotatedProperty.name.getQualifier=${annotatedProperty}") //
////        logger.warn("qualifiedName getShortName=${annotatedProperty.qualifiedName?.getShortName()}") // album
////        logger.warn("qualifiedName getQualifier=${annotatedProperty.qualifiedName?.getQualifier()}") // uii.ang.domain.AlbumDetail
////        logger.warn("typeParameters.count=${annotatedProperty.typeParameters.count()}")
////        logger.warn("simpleName.getShortName=${annotatedProperty.simpleName.getShortName()}") // album
////        logger.warn("simpleName.getQualifier=${annotatedProperty.simpleName.getQualifier()}")
////        logger.warn("location=${annotatedProperty.location}") // FileLocation(filePath=D:/code/mycodes/yeoman/AndroidCreatorKsp/src/main/kotlin/uii/ang/domain/AlbumDetail.kt, lineNumber=23)
//        var parseRootValueMap = classToParseRootValuesMap[parentClass]
////        logger.warn("parseRootValueMap count=${parseRootValueMap?.count()}")
//        if (parseRootValueMap == null) {
//          parseRootValueMap = mutableMapOf()
//        }
//        val parseRootAnnotationsParams =
//          annotatedProperty.annotations.firstOrNull()?.arguments
//        parseRootAnnotationsParams?.let {
//          val defaultValue = it.first()
//          parseRootValueMap[annotatedProperty.name!!.getShortName()] =
//            defaultValue.value as? String?
//        }
//        classToParseRootValuesMap[parentClass] = parseRootValueMap
//      }
//    }
//    classToParseRootValuesMap.forEach { (t, u) ->
//      logger.warn("classToParseRootValuesMap-> key=${t.simpleName.getShortName()} u=${u["album"].toString()}")
//    }

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