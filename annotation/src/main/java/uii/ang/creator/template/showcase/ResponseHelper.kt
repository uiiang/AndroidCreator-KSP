package uii.ang.creator.template.showcase

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.*
import uii.ang.creator.processor.Const.serializableClassName
import uii.ang.creator.processor.Const.serializableSerialNameClassName
import uii.ang.creator.processor.CreatorData
import uii.ang.creator.processor.ProcessorHelper
import uii.ang.creator.tools.firstCharLowerCase

class ResponseHelper(
  logger: KSPLogger,
  data: CreatorData
) : ProcessorHelper(logger, data) {
  fun genClassBuilder(): TypeSpec.Builder {
    val parseRootProperty = data.primaryConstructorParameters.firstOrNull { it.isParseRoot }
    val apiModel = parseRootProperty?.apiModelWrapperTypeName ?: apiModelClassName

    val builderPropName = parseRootProperty?.className?.getShortName() ?: dataClassName.firstCharLowerCase()
    val flux = genConstructor(apiModel, builderPropName)
    val propertySpec = genPropertySpec(apiModel, builderPropName)
    val classBuilder = TypeSpec.classBuilder(responseClassName)
      .addModifiers(KModifier.DATA)
//      .addModifiers(KModifier.INTERNAL)
      .primaryConstructor(flux.build())
      .addProperty(propertySpec.build())
      // 为生成的Response类添加序列化注解
      .addAnnotation(AnnotationSpec.builder(serializableClassName).build())
    return classBuilder
  }

  fun genPropertySpec(apiModelClassName: TypeName, builderPropName: String): PropertySpec.Builder {
    val propertySpec = PropertySpec.builder(
      builderPropName,
      apiModelClassName
    ).initializer(builderPropName)
    return propertySpec
  }

  fun genConstructor(apiModelClassName: TypeName, builderPropName: String): FunSpec.Builder {
    val parameterSpec = ParameterSpec
    val builder =
      parameterSpec.builder(builderPropName, apiModelClassName).addAnnotation(
        AnnotationSpec.builder(
          serializableSerialNameClassName
        ).addMember("\"${builderPropName}\"").build()
      )

    val flux = FunSpec.constructorBuilder()
    flux.addParameter(builder.build())
    return flux
  }
}