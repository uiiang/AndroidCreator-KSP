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

    logger.warn("found CreatorAnnotation ${creatorAnnotated.count()}")
    val data = CreatorCollector.collect(resolver, logger)
    generateMappingCode(resolver, data)
    writeFiles()

//
//    val classToDefaultValuesMap =
//      mutableMapOf<KSClassDeclaration, MutableMap<String, String?>>()
//    val symbolsWithDefaultAnnotation =
//      resolver.getSymbolsWithAnnotation(Default::class.qualifiedName!!, true)
//    logger.warn("symbolsWithDefaultAnnotation count ${symbolsWithDefaultAnnotation.count()}")
//    symbolsWithDefaultAnnotation.forEach { annotatedProperty ->
//      logger.warn("annotatedProperty${annotatedProperty.location.toString()}")
//      if (annotatedProperty is KSValueParameter) {
//        val parentClass = annotatedProperty.findParentClass()!!
//        var defaultValueMap = classToDefaultValuesMap[parentClass]
//        if (defaultValueMap == null) {
//          defaultValueMap = mutableMapOf()
//        }
//        val defaultAnnotationsParams =
//          annotatedProperty.annotations.firstOrNull()?.arguments
//        val defaultValue = defaultAnnotationsParams?.first()
//        defaultValueMap[annotatedProperty.name!!.getShortName()] =
//          defaultValue?.value as? String?
//        classToDefaultValuesMap[parentClass] = defaultValueMap
//      }
//    }
//
    val unableToProcess = creatorAnnotated.filterNot { it.validate() }
////    logger.warn("classToDefaultValuesMap count ${classToDefaultValuesMap.count()}")
//    creatorAnnotated.filter { it is KSClassDeclaration && it.validate() }
//      .forEach {
//        it.accept(Visitor(classToDefaultValuesMap), Unit)
//      }
//    val retrofitArgumentMap = mutableMapOf<KSClassDeclaration, List<KSValueArgument>>()
//    creatorAnnotated.filter { it is KSClassDeclaration && it.validate() }
//      .forEach {
//        val classDecl = it as KSClassDeclaration
////        val className = it.annotations.firstOrNull()?.parent
//        it.annotations.firstOrNull()?.arguments!!
//          .forEach { arg ->
//            if (arg.name?.getShortName() == "retrofitServiceClassName" && arg.value.toString().isNotEmpty()) {
//              retrofitArgumentMap[classDecl] = it.annotations.firstOrNull()?.arguments!!
//            }
//          }
//      }
//    val funcDescMap = mutableMapOf<String, MutableList<FunSpec>>()
//    retrofitArgumentMap.forEach { (t, u) ->
//      logger.warn("classArgumentMap $t , ${u.count()}")
//      u.forEach { i ->
//        logger.warn("    name ${i.name?.getShortName()} value ${i.value}")
//      }
//      // 每个循环中是一个data类和注解，在循环中根据注解转换成service中的FuncSpec，
//      //
//      val basePackageName = "uii.ang"
//      val retrofitServiceHelper = RetrofitServiceHelper(logger, t, basePackageName)
//      val genRetrofitServiceFuncCode = retrofitServiceHelper.genRetrofitServiceFuncCode(u)
//      logger.warn("gen retrofit service ${genRetrofitServiceFuncCode.first}")
//      if (funcDescMap.containsKey(genRetrofitServiceFuncCode.first)) {
//        logger.warn(" has key add ${genRetrofitServiceFuncCode.first} -> ${genRetrofitServiceFuncCode.second.name}")
//        funcDescMap[genRetrofitServiceFuncCode.first] =
//          funcDescMap[genRetrofitServiceFuncCode.first]?.plusElement(genRetrofitServiceFuncCode.second)?.toMutableList()!!
//      } else {
//        logger.warn(" not found ${genRetrofitServiceFuncCode.first} -> ${genRetrofitServiceFuncCode.second.name}")
//        funcDescMap[genRetrofitServiceFuncCode.first] = mutableListOf(genRetrofitServiceFuncCode.second)
//      }
//    }
//    logger.warn("funcDescMap count ${funcDescMap.count()}")
//    funcDescMap.forEach { (t, u) ->
//    val classBuilder = TypeSpec.interfaceBuilder(t)
//      .addModifiers(KModifier.INTERNAL)
//      logger.warn("funcDescMap key ${t} ${u.count()}")
//      u.forEach { fs->
//        classBuilder.addFunction(fs)
//        logger.warn("  classname ${t} count funcname ${fs.name}")
//      }
//      FileSpec.builder("uii.ang.product.data.datasource.api.service",
//        t)
//      .addType(classBuilder.build())
//        .build().writeTo(codeGenerator = codeGenerator, aggregating = false)
//    }

    return unableToProcess.toList()
  }

//  private fun KSNode.findParentClass(): KSClassDeclaration? {
//    var currentParent: KSNode? = parent
//    while (currentParent !is KSClassDeclaration) {
//      currentParent = parent?.parent
//      if (currentParent == null) {
//        return null
//      }
//    }
//    return currentParent
//  }
//
//  private inner class Visitor(
//    private val defaultValuesMap: Map<KSClassDeclaration, MutableMap<String, String?>>,
//  ) : KSVisitorVoid() {
//
//    @Suppress("LongMethod", "MaxLineLength", "ComplexMethod")
//    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
//      if (isInvalidAnnotatedSetup(classDeclaration)) {
//        return
//      }
//      logger.warn("Visitor visitClassDeclaration")
//      val basePackageName = "uii.ang"
//      val processorHelper = ProcessorHelper(logger, classDeclaration, basePackageName)
//      val apiModelHelper = ApiModelHelper(logger, classDeclaration, basePackageName)
//      val responseHelper = ResponseHelper(logger, classDeclaration, basePackageName)
//      val retrofitServiceHelper = RetrofitServiceHelper(logger, classDeclaration, basePackageName)
//
//      // 获取类的注解
//      val dataCompatAnnotation = classDeclaration.annotations.firstOrNull {
//        it.annotationType.resolve().toString() == Creator::class.simpleName
//      }
//      val imports = ArrayList<String>()
//      var generateApiModel = true
//      var generateResponse = false
//      var generateRetrofitService = false
//
//      dataCompatAnnotation?.let { anno ->
//        logger.warn("shortName ${anno.shortName.getShortName()}")
////        anno.arguments.forEach {
////          logger.warn("anno.arguments ${it.name?.getShortName()}")
////        }
//        anno.arguments.firstOrNull {
//          it.name?.getShortName() == "generateApiModel"
//        }?.value?.let { generateApiModel = it as Boolean }
//
//        anno.arguments.firstOrNull {
//          it.name?.getShortName() == "generateResponse"
//        }?.value?.let { generateResponse = it as Boolean }
//
//        anno.arguments.firstOrNull {
//          it.name?.getShortName() == "generateRetrofitService"
//        }?.value?.let { generateRetrofitService = it as Boolean }
//      }
////      generateParameters.forEach {
////        logger.warn("generaterParameters name ${it.paramName} type ${it.paramType}")
////      }
//      val otherAnnotations = classDeclaration.annotations
//        .filter { it.annotationType.resolve().toString() != Creator::class.simpleName }
//      otherAnnotations.forEach {
//        logger.warn("otherAnnotations ${it.shortName}")
//      }
//      val implementedInterfaces = classDeclaration
//        .superTypes
//        .filter { (it.resolve().declaration as? KSClassDeclaration)?.classKind == ClassKind.INTERFACE }
//
//      // 获取数据类的属性
//      val propertyMap = processorHelper.getPropertyMap(classDeclaration, defaultValuesMap)
//      // Build mandatory param list for toBuilder and DSL function
//      // 获取构造函数中不能为空的属性列表
//      val mandatoryParams = propertyMap.filter {
//        it.value.mandatoryForConstructor
//      }.map { it.key.toString() }.joinToString(", ")
////      logger.warn("mandatoryParams $mandatoryParams")
//
//      // 生成apimodel
//      if (generateApiModel) {
//        // File
//        val apiModelFileBuilder = apiModelHelper.genApiModelFileSpec(
//          classDeclaration,
//          basePackageName = basePackageName, propertyMap
//        )
//        imports.forEach {
//          apiModelFileBuilder
//            .addImport(
//              it.split(".").dropLast(1).joinToString("."),
//              it.split(".").last()
//            )
//        }
//        apiModelFileBuilder.build().writeTo(codeGenerator = codeGenerator, aggregating = false)
//      }
//      //生成Response，默认false
//      if (generateResponse) {
//        val responseFileBuilder = responseHelper.genResponseFileSpec(dataCompatAnnotation!!)
//        imports.forEach {
//          responseFileBuilder
//            .addImport(
//              it.split(".").dropLast(1).joinToString("."),
//              it.split(".").last()
//            )
//        }
//        responseFileBuilder.build().writeTo(codeGenerator = codeGenerator, aggregating = false)
//      }
////      //生成RetrofitService
////      if (generateRetrofitService) {
//////        retrofitServiceOpt.forEach {
//////          logger.warn("generateRetrofitService ${it}")
//////        }
////        val retrofitServiceFileBuilder =
////          retrofitServiceHelper.genRetrofitServiceFileSpec(dataCompatAnnotation!!)
////        imports.forEach {
////          retrofitServiceFileBuilder
////            .addImport(
////              it.split(".").dropLast(1).joinToString("."),
////              it.split(".").last()
////            )
////        }
////        retrofitServiceFileBuilder.build().writeTo(codeGenerator = codeGenerator, aggregating = false)
////      }
//    }
//
//
//    @Suppress("SameParameterValue")
//    private fun FileSpec.Builder.suppressWarningTypes(vararg types: String): FileSpec.Builder {
//      if (types.isEmpty()) {
//        return this
//      }
//
//      val format = "%S,".repeat(types.count()).trimEnd(',')
//      addAnnotation(
//        AnnotationSpec.builder(ClassName("", "Suppress"))
//          .addMember(format, *types)
//          .build()
//      )
//      return this
//    }
//
//    @Suppress("ReturnCount")
//    private fun isInvalidAnnotatedSetup(classDeclaration: KSClassDeclaration): Boolean {
//      val qualifiedName = classDeclaration.qualifiedName?.asString() ?: run {
//        logger.error(
//          "@DataClass must target classes with a qualified name",
//          classDeclaration
//        )
//        return true
//      }
//
//      // 类型必须是data class
//      if (!classDeclaration.isDataClass()) {
//        logger.error(
//          "@DataClass cannot target a non-data class $qualifiedName",
//          classDeclaration
//        )
//        return true
//      }
//
//      // dataclass 必须是private类
////      if (!classDeclaration.isPrivate()) {
////        logger.error(
////          "@DataClass target must have private visibility",
////          classDeclaration
////        )
////        return true
////      }
//
//      if (classDeclaration.typeParameters.any()) {
//        logger.error(
//          "@DataClass target shouldn't have type parameters",
//          classDeclaration
//        )
//        return true
//      }
//
//      // 类名必须以Data结尾
////      if (!classDeclaration.simpleName.asString().endsWith("Data")) {
////        logger.error(
////          "@DataClass target must end with Data suffix naming",
////          classDeclaration
////        )
////        return true
////      }
//      return false
//    }
//
//    private fun KSClassDeclaration.isDataClass() = modifiers.contains(Modifier.DATA)
//
//  }

  private fun writeFiles() {
    CodeBuilder.all().forEach {
      it.build().writeTo(
        codeGenerator,
        aggregating = true, // always aggregating, as any new file could be a mapper with higher prio than a potentially used one.
//        it.originating
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

  private companion object {
    private const val CLASS_NAME_DROP_LAST_CHARACTERS = 4
    private const val INDENTATION_SIZE = 2
  }
}