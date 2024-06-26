package uii.ang.creator.template.showcase

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.*
import uii.ang.creator.annotation.ParseRoot
import uii.ang.creator.processor.CreatorData
import uii.ang.creator.processor.ProcessorHelper
import uii.ang.creator.tools.firstCharLowerCase
import uii.ang.creator.tools.hasAnnotation

class ResponseHelper(
  logger: KSPLogger,
  data: CreatorData
) : ProcessorHelper(logger, data) {
  fun genClassBuilder(): TypeSpec.Builder {
//    val apiModelClassName = apiModelClassName
//    val responseClassNameStr = responseClassName.simpleName
//    logger.warn("ResponseHelper responseClassName ${responseClassName}")
    logger.warn("sourceClassDeclaration= ${data.sourceClassDeclaration.simpleName.getShortName()}")

    val parseRootProperty =
      data.sourceClassDeclaration.primaryConstructor?.parameters?.firstOrNull { it.hasAnnotation<ParseRoot>() }
    val apiModel = parseRootProperty?.let {
//      logger.warn("get parseRootProperty in ${data.sourceClassDeclaration.simpleName.getShortName()} \n" +
//              "\tproperty name = ${it.name?.getShortName()}\n" +
//              "\ttype =${it.type.resolve().declaration.qualifiedName?.getQualifier()}.${it.type.resolve().declaration.qualifiedName?.getShortName()}\n" +
//              "\torigin =${it.origin.name} ${it.parent.toString()}")
      getApiModelClassNameByDataModel(it.type.resolve().declaration.qualifiedName?.getShortName()!!)
    } ?: apiModelClassName

    val builderPropName = parseRootProperty?.name?.getShortName() ?: dataClassName.firstCharLowerCase()
    val flux = genConstructor(apiModel, builderPropName)
    val propertySpec = genPropertySpec(apiModel, builderPropName)
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

  fun genPropertySpec(apiModelClassName: ClassName, builderPropName: String): PropertySpec.Builder {
    val propertySpec = PropertySpec.builder(
      builderPropName,
      apiModelClassName
    ).initializer(builderPropName)
    return propertySpec
  }

  fun genConstructor(apiModelClassName: ClassName, builderPropName: String): FunSpec.Builder {
    val parameterSpec = ParameterSpec
    val builder =
      parameterSpec.builder(builderPropName, apiModelClassName).addAnnotation(
        AnnotationSpec.builder(
          serialNameClassName
        ).addMember("\"${builderPropName}\"").build()
      )

    val flux = FunSpec.constructorBuilder()
    flux.addParameter(builder.build())
    return flux
  }
}