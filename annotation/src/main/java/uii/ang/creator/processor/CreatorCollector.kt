package uii.ang.creator.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import uii.ang.creator.annotation.Creator

object CreatorCollector {

  fun collect(resolver: Resolver, logger: KSPLogger): List<CreatorData> {
//    logger.warn("CreatorCollector collect")
    val creatorAnnotated = resolver.getSymbolsWithAnnotation(Creator::class.qualifiedName!!)
    if (creatorAnnotated.count() == 0) {
      logger.warn("CreatorCollector 未找到使用Creator注解的类")
      return emptyList()
    }
    return creatorAnnotated
      .filterNot {
        val ksClassDeclaration = it as KSClassDeclaration
        isInvalidAnnotatedSetup(ksClassDeclaration, logger)
      }
      .flatMap {
//        logger.warn("CreatorCollector collect flatmap count=${it.annotations.count()}")
        val ksClassDeclaration = it as KSClassDeclaration
        it.annotations
          .filter { anno ->
            (anno.annotationType.toTypeName() as? ClassName)?.canonicalName ==
                    Creator::class.qualifiedName
          }
          .map { anno ->
            CreatorData.AnnotationData.from(anno)
          }.map { anno ->
//            logger.warn("CreatorCollector collect map2 ${anno.methodName}")
            CreatorData(
              annotationData = anno,
              sourceClassDeclaration = ksClassDeclaration,
//              logger = logger
            )
          }
      }.toList()
  }


  @Suppress("ReturnCount")
  private fun isInvalidAnnotatedSetup(classDeclaration: KSClassDeclaration, logger: KSPLogger): Boolean {
    val qualifiedName = classDeclaration.qualifiedName?.asString() ?: run {
      logger.error(
        "@DataClass must target classes with a qualified name",
        classDeclaration
      )
      return true
    }

    // 类型必须是data class
    if (!classDeclaration.isDataClass()) {
      logger.error(
        "@DataClass cannot target a non-data class $qualifiedName",
        classDeclaration
      )
      return true
    }

    // dataclass 必须是private类
//      if (!classDeclaration.isPrivate()) {
//        logger.error(
//          "@DataClass target must have private visibility",
//          classDeclaration
//        )
//        return true
//      }

    if (classDeclaration.typeParameters.any()) {
      logger.error(
        "@DataClass target shouldn't have type parameters",
        classDeclaration
      )
      return true
    }

    // 类名必须以Data结尾
//      if (!classDeclaration.simpleName.asString().endsWith("Data")) {
//        logger.error(
//          "@DataClass target must end with Data suffix naming",
//          classDeclaration
//        )
//        return true
//      }
    return false
  }

  private fun KSClassDeclaration.isDataClass() = modifiers.contains(Modifier.DATA)

}