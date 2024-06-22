package uii.ang.creator.template.showcase

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.*
import uii.ang.creator.processor.CreatorData
import uii.ang.creator.processor.ProcessorHelper
import uii.ang.creator.tools.firstCharLowerCase

class ResponseHelper(
  logger: KSPLogger,
  data: CreatorData,
  basePackageName: String
) : ProcessorHelper(logger, data, basePackageName) {
  fun genClassBuilder(): TypeSpec.Builder {
//    val apiModelClassName = apiModelClassName
//    val responseClassNameStr = responseClassName.simpleName
    logger.warn("ResponseHelper responseClassName ${responseClassName}")
    val flux = genConstructor(apiModelClassName)
    val propertySpec = genPropertySpec(apiModelClassName)
    val classBuilder = TypeSpec.classBuilder(responseClassName)
      .addModifiers(KModifier.DATA)
      .addModifiers(KModifier.INTERNAL)
      .primaryConstructor(flux.build())
      .addProperty(propertySpec.build())
      // 为生成的Response类添加序列化注解
      .addAnnotation(
        AnnotationSpec
          .builder(serializableClassName).build()
      )
    return classBuilder
  }

  fun genPropertySpec(apiModelClassName: ClassName): PropertySpec.Builder {

    val propertySpec = PropertySpec.builder(
      dataClassName.firstCharLowerCase(),
      apiModelClassName
    ).initializer(dataClassName.firstCharLowerCase())
    return propertySpec
  }

  fun genConstructor(apiModelClassName: ClassName): FunSpec.Builder {
    val flux = FunSpec.constructorBuilder()
    flux.addParameter(
      ParameterSpec.builder(
        dataClassName.firstCharLowerCase(), apiModelClassName
      ).addAnnotation(
        AnnotationSpec.builder(serialNameClassName
        ).addMember("\"${dataClassName.firstCharLowerCase()}\"").build()
      ).build()
    )
    return flux
  }
}