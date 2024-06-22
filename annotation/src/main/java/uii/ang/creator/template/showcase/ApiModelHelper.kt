package uii.ang.creator.template.showcase

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import uii.ang.creator.processor.CreatorData
import uii.ang.creator.processor.ProcessorHelper
import uii.ang.creator.processor.PropertyDescriptor

class ApiModelHelper(
  logger: KSPLogger,
  data: CreatorData,
  basePackageName: String
) : ProcessorHelper(logger, data, basePackageName) {
  // @Serializable
  // internal data class AlbumApiModel(
  fun genClassBuilder(): TypeSpec.Builder {
//    val className = data.apiModelClassName.simpleName
    val constructorParams = genConstructor(data.propertyDescriptorList)

    // @SerialName("name") val name: String,
    // 生成构造函数中的属性列表
    val propertyList = convertProperty(data.propertyDescriptorList)
//    logger.warn("propertyList ${propertyList.count()}")
    return TypeSpec.classBuilder(apiModelClassName)
      .addModifiers(KModifier.DATA)
      .addModifiers(KModifier.INTERNAL)
      .addAnnotation(AnnotationSpec.builder(serializableClassName).build())
      .primaryConstructor(constructorParams.build())
      .addProperties(propertyList)
  }

  // 生成构造函数里的参数
  fun genConstructor(propertyList: List<PropertyDescriptor>): FunSpec.Builder {
    val flux = FunSpec.constructorBuilder()
    for (entry in propertyList) {
      val genTypeName = convertPropertyToTypeName(entry)
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
      val genTypeName = convertPropertyToTypeName(entry)
      val prop = PropertySpec.builder(
        entry.className.getShortName(),
        genTypeName.copy(nullable = entry.isNullable)
      )
      if (entry.isNullable) {
//      val type = entry.value.typeName.toString()
//        logger.warn("genProperty type = $genTypeName")
        prop.initializer(entry.className.getShortName(), null)
      } else {
//      if (isBaseType(entry.value.typeName)){
        prop.initializer(entry.className.getShortName())
//      } else {
//        prop.initializer(entry.key.simpleName.getShortName())
//      }
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

  private fun convertPropertyToTypeName(propertyDescriptor: PropertyDescriptor): TypeName {
//    logger.warn(" convertPropertyToTypeName  $propertyDescriptor")
    val typeName = propertyDescriptor.typeName
    val className = propertyDescriptor.className.getShortName()
//    logger.warn(
//      "genTypeByPropertyEntry " +
//              "typeName=${typeName}" +
//              " className $className " +
//              " isBaseType ${isBaseType(typeName)}"
//    )
    val genTypeName = if (isBaseType(typeName)) {
      // 如果字段属性为基本类型，直接返回原类型
      typeName
    } else if (isList(typeName)) {
      // 如果是list，获取list中的泛型类，
      // 如果泛型类是注解Creator的数据类，转换成apimodel
      // 判断当前list的泛型类是否为creator
      val ksNode = getListGenericsCreatorAnnotation(propertyDescriptor)
      ksNode?.let {
        // 字段属性为List<注解了Create的data class>
        val apiModelName = it.toString() + "ApiModel"
//        logger.warn("apiModelName $apiModelName")
        val apiModelClass = ClassName(apiModelPackageName, apiModelName)
        val parameterizedBy = listClassName.parameterizedBy(apiModelClass)
//        logger.warn("  convert parameterizedBy ${parameterizedBy.toString()}")
        parameterizedBy
      } ?: typeName // 字段属性为List<基本类型>
    } else {
// 字段属性为data class
      val retClassName = ClassName(
        apiModelPackageName,
        propertyDescriptor.className.getShortName().firstCharUpperCase() + "ApiModel"
      )
//      logger.warn("  convert api model ${retClassName.simpleName}")
      retClassName
    }
    return genTypeName
  }


  // 生成toDomainModel扩展方法
  fun toDomainModel(
    className: String,
    packageName: String,
    data: CreatorData
  ): FunSpec {
    val propertyList = data.propertyDescriptorList
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
      logger.warn("toDomainModel type = $typeName null = ${entry.isNullable}")

      val isList = typeName.toString().startsWith("kotlin.collections.List")
      if (isList && entry.isNullable) {
        toDomainModel.addStatement("  $paramName = this.$paramName?.map { it.toDomainModel() } ?: listOf(),")
      } else if (isList(typeName) && !entry.isNullable) {
        if (getListGenericsCreatorAnnotation(entry) != null) {
          toDomainModel.addStatement("  $paramName = this.$paramName.map { it.toDomainModel() },")
        } else {
          toDomainModel.addStatement("  $paramName = this.$paramName.map { it } ,")
        }
      } else {
        if (isBaseType(typeClassName)) {
          val defValue = if (typeName.isNullable) "?: \"\"" else ""
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