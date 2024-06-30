package uii.ang.creator.template.showcase

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import uii.ang.creator.processor.Const.serialNameClassName
import uii.ang.creator.processor.Const.serializableClassName
import uii.ang.creator.processor.CreatorData
import uii.ang.creator.processor.ProcessorHelper
import uii.ang.creator.processor.PropertyDescriptor
import uii.ang.creator.processor.Utils.getListGenericsCreatorAnnotation
import uii.ang.creator.tools.isBaseType
import uii.ang.creator.tools.isList
import uii.ang.creator.tools.primitiveDefaultInit

class ApiModelHelper(
  logger: KSPLogger,
  data: CreatorData
) : ProcessorHelper(logger, data) {
  // @Serializable
  // internal data class AlbumApiModel(
  fun genClassBuilder(): TypeSpec.Builder {
//    val className = data.apiModelClassName.simpleName
    val constructorParams = genConstructor(data.primaryConstructorParameters)

    // @SerialName("name") val name: String,
    // 生成构造函数中的属性列表
    val propertyList = convertProperty(data.primaryConstructorParameters)
//    logger.warn("propertyList ${propertyList.count()}")
    return TypeSpec.classBuilder(apiModelClassName)
      .addModifiers(KModifier.INTERNAL, KModifier.DATA)
      .addAnnotation(AnnotationSpec.builder(serializableClassName).build())
      .primaryConstructor(constructorParams.build())
      .addProperties(propertyList)
  }

  // 生成构造函数里的参数
  fun genConstructor(propertyList: List<PropertyDescriptor>): FunSpec.Builder {
    val flux = FunSpec.constructorBuilder()
    for (entry in propertyList) {
      val genTypeName = entry.wrapperTypeName
      flux.addParameter(
        ParameterSpec.builder(
          entry.className.getShortName(), genTypeName.copy(nullable = entry.isNullable)
        ).build()
      )
    }
    return flux
  }

  // 生成构造函数中的属性列表
  private fun convertProperty(propertyList: List<PropertyDescriptor>):
          List<PropertySpec> {
    val retList: MutableList<PropertySpec> = mutableListOf()
    for (entry in propertyList) {
      val genTypeName = entry.wrapperTypeName
      val prop = PropertySpec.builder(
        entry.className.getShortName(),
        genTypeName.copy(nullable = entry.isNullable)
      )
      if (entry.isNullable) {
        prop.initializer(entry.className.getShortName(), null)
      } else {
        prop.initializer(entry.className.getShortName())
      }
      prop
        .addAnnotation(
          AnnotationSpec.builder(serialNameClassName)
            .addMember("\"${entry.className.getShortName()}\"").build()
        )
      retList.add(prop.build())
    }
    return retList
  }

  // 生成toDomainModel扩展方法
  fun toDomainModel(
    className: String,
    packageName: String,
    data: CreatorData
  ): FunSpec {
    val propertyList = data.primaryConstructorParameters
    val toDomainModel = FunSpec.builder("toDomainModel")
      .receiver(ClassName(packageName, className))
      .returns(data.sourceClassDeclaration.toClassName())
      .addModifiers(KModifier.INTERNAL)
      .addStatement("")
      .addStatement("return ${data.sourceClassDeclaration.simpleName.asString()} (")
    for (entry in propertyList) {
      val typeName = entry.typeName
      val paramName = entry.className.getShortName()
      val typeClassName = entry.typeClassName
//      logger.warn("toDomainModel type = $typeName null = ${entry.isNullable}")

      val isList = typeName.toString().startsWith("kotlin.collections.List")
      if (isList && entry.isNullable) {
        toDomainModel.addStatement("  $paramName = this.$paramName?.map { it.toDomainModel() } ?: listOf(),")
      } else if (typeName.isList() && !entry.isNullable) {
        if (getListGenericsCreatorAnnotation(entry) != null) {
          toDomainModel.addStatement("  $paramName = this.$paramName.map { it.toDomainModel() },")
        } else {
          toDomainModel.addStatement("  $paramName = this.$paramName.map { it } ,")
        }
      } else {
//        logger.warn("  typeName=${typeName} isBaseType=${typeName.isBaseType()} primitiveDefaultInit=${typeName.primitiveDefaultInit()}")
        if (typeName.isBaseType()) {
          val defValue = if (typeName.isNullable) "?: ${typeName.primitiveDefaultInit()}" else ""
          toDomainModel.addStatement("  $paramName = this.$paramName $defValue,")
        } else {
          toDomainModel.addStatement("  $paramName = this.$paramName.toDomainModel(),")
        }
      }
    }
    toDomainModel.addStatement(")")
    return toDomainModel.build()
  }
}