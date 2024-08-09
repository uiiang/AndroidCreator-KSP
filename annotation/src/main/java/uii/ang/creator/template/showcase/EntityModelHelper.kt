package uii.ang.creator.template.showcase

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import uii.ang.creator.processor.Const.entityModelPackageName
import uii.ang.creator.processor.Const.listClassName
import uii.ang.creator.processor.Const.roomEntityClassName
import uii.ang.creator.processor.Const.roomPrimaryKeyClassName
import uii.ang.creator.processor.Const.roomTypeConverterClassName
import uii.ang.creator.processor.Const.roomTypeConvertersClassName
import uii.ang.creator.processor.Const.serialEncodeToStringMemberName
import uii.ang.creator.processor.Const.serializableJsonClassName
import uii.ang.creator.processor.Const.stringClassName
import uii.ang.creator.processor.CreatorData
import uii.ang.creator.processor.ProcessorHelper
import uii.ang.creator.processor.PropertyDescriptor
import uii.ang.creator.processor.Utils.getListGenericsCreatorAnnotation
import uii.ang.creator.tools.firstCharUpperCase
import uii.ang.creator.tools.isBaseType
import uii.ang.creator.tools.isList
import uii.ang.creator.tools.primitiveDefaultInit

class EntityModelHelper(
  logger: KSPLogger,
  data: CreatorData
) : ProcessorHelper(logger, data) {

  fun getToDatabaseAnno() =
    data.primaryConstructorParameters.filter { it.isToDatabase }//.filter { !it.isBaseType }

  //  @Entity(tableName = "albums")
//  @TypeConverters(
//    AlbumImageEntityTypeConverter::class,
//    AlbumTrackEntityTypeConverter::class,
//    AlbumTagEntityTypeConverter::class,
//  )
//  internal data class AlbumEntityModel(
//@PrimaryKey(autoGenerate = true) val id: Int = 0,
//  val mbId: String,
//  val name: String,
//  val artist: String,
//  val images: List<ImageEntityModel> = listOf(),
//  val tracks: List<TrackEntityModel>?,
//  val tags: List<TagEntityModel>?,
  fun genClassBuilder(): TypeSpec.Builder {
    val constructorParams = genConstructor(data.primaryConstructorParameters)
//    logger.warn("开始生成entity Model类")
    val propertyList = convertProperty(data.primaryConstructorParameters)
    val roomEntityAnno = AnnotationSpec.builder(roomEntityClassName)
      .addMember("tableName = \"${classDeclaration.simpleName.getShortName().lowercase()}\"")
    val typeConvertParamClass = genTypeConvertParameterAnnotation(data.primaryConstructorParameters)
    val roomTypeConvertAnno = AnnotationSpec.builder(roomTypeConvertersClassName)
    typeConvertParamClass.onEach {
      roomTypeConvertAnno.addMember("\t${it.simpleName}::class")
    }
    return TypeSpec.classBuilder(entityModelClassName)
      .addModifiers(KModifier.DATA)
      .addAnnotation(roomEntityAnno.build())
      .primaryConstructor(constructorParams.build())
      .addProperties(propertyList)
      .apply {
        if (typeConvertParamClass.isNotEmpty()) {
          addAnnotation(roomTypeConvertAnno.build())
        }
      }
  }

  // 循环构造函数里的参数，生成TypeConverters注解中的参数类
  //  @TypeConverters(
  //    AlbumImageEntityTypeConverter::class,
  //    AlbumTrackEntityTypeConverter::class,
  //    AlbumTagEntityTypeConverter::class,
  //  )
  fun genTypeConvertParameterAnnotation(primaryConstructorParameters: List<PropertyDescriptor>): List<ClassName> {
    val classes = primaryConstructorParameters.filter { !it.isBaseType }.map {
      getEntityTypeConvertClassName(it)
    }
    return classes
  }

  @OptIn(DelicateKotlinPoetApi::class)
  fun genClassBuilder(propertyDescriptor: PropertyDescriptor): TypeSpec.Builder {
    val constructorParams = genConstructor(data.primaryConstructorParameters)
    logger.warn("开始生成entity Model类")
    // 获取当前属性的数据类型
    // 数据类型是否@Creator
    // 获取data class对象，生成entityModel
    val resolve = propertyDescriptor.resolve
    val toTypeName = resolve.toTypeName()
    if (toTypeName.isList()) {
      val ksNode = resolve.arguments.first()
        .type?.resolve()?.let { it1 -> getListGenericsCreatorAnnotation(it1) }
      ksNode?.let { node ->
        val className = node.javaClass.asClassName()

        logger.warn("\t当前属性${propertyDescriptor.typeClassName.simpleName} 是List对象，数据类型为${ksNode.toString()} toClassName=${resolve.toClassName().simpleName}")
      }
    } else {
      val ksNode = getListGenericsCreatorAnnotation(resolve)
      ksNode?.let { node ->
        val className = resolve.toClassName()
        logger.warn("\t当前属性${propertyDescriptor.typeClassName.simpleName} 非List对象，数据类型为${ksNode.toString()} toClassName=${resolve.toClassName().simpleName}")
      }
    }


    val propertyList = convertProperty(data.primaryConstructorParameters)
    val roomEntityAnno = AnnotationSpec.builder(roomEntityClassName)
      .addMember("tableName = \"${classDeclaration.simpleName.getShortName()}\"")
    return TypeSpec.classBuilder(entityModelClassName)
      .addModifiers(KModifier.DATA)
      .addAnnotation(roomEntityAnno.build())
      .primaryConstructor(constructorParams.build())
      .addProperties(propertyList)
  }

  // 生成构造函数里的参数
  fun genConstructor(propertyList: List<PropertyDescriptor>): FunSpec.Builder {
    val flux = FunSpec.constructorBuilder()
    val parameterSpecList = propertyList.map { entry ->
      val genTypeName = entry.entityModelWrapperTypeName
      val paramSpec = ParameterSpec.builder(
        entry.className.getShortName(), genTypeName.copy(nullable = entry.isNullable)
      )
      if (entry.isNullable) {
        paramSpec.defaultValue("${entry.typeClassName.primitiveDefaultInit()}")
      }
      paramSpec.build()
    }
    val idKey = getPrimaryKeyParameterSpec()
    flux.addParameter(idKey.build())
    flux.addParameters(parameterSpecList)
    return flux
  }

  private fun getPrimaryKeyParameterSpec(): ParameterSpec.Builder {
    val primaryKeyAnno = AnnotationSpec.builder(roomPrimaryKeyClassName)
      .addMember("autoGenerate = true")
    val idKey = ParameterSpec.builder("id", Int::class.java)
      .addAnnotation(primaryKeyAnno.build())
      .defaultValue("0")
    return idKey
  }

  // 生成构造函数中的属性列表
  private fun convertProperty(propertyList: List<PropertyDescriptor>):
          List<PropertySpec> {
    val retList: MutableList<PropertySpec> = mutableListOf()
    for (entry in propertyList) {
      val genTypeName = entry.entityModelWrapperTypeName
      val prop = PropertySpec.builder(
        entry.className.getShortName(),
        genTypeName.copy(nullable = entry.isNullable)
      )
      if (entry.isNullable) {
        prop.initializer(entry.className.getShortName(), null)
      } else {
        prop.initializer(entry.className.getShortName())
      }
      retList.add(prop.build())
    }
//    val primaryKeyAnno = AnnotationSpec.builder(roomPrimaryKeyClassName)
//      .addMember("autoGenerate = true")
    val idKey = PropertySpec.builder("id", Int::class.java)
//      .addAnnotation(primaryKeyAnno.build())
      .initializer("id", 0)
    retList.add(idKey.build())
    return retList
  }

  // 生成对象的扩展方法toEntityModel
  fun toEntityModel(
    className: String,
    packageName: String,
    data: CreatorData): FunSpec {
    val propertyList = data.primaryConstructorParameters
    val toEntityModel = FunSpec.builder("toEntityModel")
//      .receiver(ClassName(packageName, className))
      .receiver(data.sourceClassDeclaration.toClassName())
      .returns(entityModelClassName)
//      .addModifiers(KModifier.INTERNAL)
      .addStatement("")
      .addStatement("return %T (", entityModelClassName)
    for (entry in propertyList) {
      val typeName = entry.typeName
      val paramName = entry.className.getShortName()
      val typeClassName = entry.typeClassName
//      logger.warn("toDomainModel type = $typeName null = ${entry.isNullable}")

      val isList = typeName.toString().startsWith("kotlin.collections.List")
      if (isList && entry.isNullable) {
        toEntityModel.addStatement("  $paramName = this.$paramName?.map { it.toEntityModel() } ?: listOf(),")
      } else if (typeName.isList() && !entry.isNullable) {
        if (getListGenericsCreatorAnnotation(entry) != null) {
          toEntityModel.addStatement("  $paramName = this.$paramName.map { it.toEntityModel() },")
        } else {
          toEntityModel.addStatement("  $paramName = this.$paramName.map { it },")
        }
      } else {
//        logger.warn("  typeName=${typeName} isBaseType=${typeName.isBaseType()} primitiveDefaultInit=${typeName.primitiveDefaultInit()}")
        if (typeName.isBaseType()) {
          val defValue = if (typeName.isNullable) "?: ${typeName.primitiveDefaultInit()}" else ""
          toEntityModel.addStatement("  $paramName = this.$paramName $defValue,")
        } else {
          toEntityModel.addStatement("  $paramName = this.$paramName?.toEntityModel(),")
        }
      }
    }
    toEntityModel.addStatement(")")
    return toEntityModel.build()
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
//      .addModifiers(KModifier.INTERNAL)
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
          toDomainModel.addStatement("  $paramName = this.$paramName?.toDomainModel(),")
        }
      }
    }
    toDomainModel.addStatement(")")
    return toDomainModel.build()
  }

  //  internal class AlbumImageEntityTypeConverter {
//    @TypeConverter
//    fun stringToList(data: String?) =
//        data?.let { Json.decodeFromString<List<ImageEntityModel>>(it) } ?: listOf()
//
//    @TypeConverter
//    fun listToString(someObjects: List<ImageEntityModel>): String =
//        Json.encodeToString(someObjects)
//}

  fun genTypeConverter(propertyDescriptor: PropertyDescriptor): TypeSpec.Builder {
    val typeConverterClassName = getEntityTypeConvertClassName(propertyDescriptor)
    logger.warn("准备生成TypeConverter类， 类名${typeConverterClassName.simpleName}")
    val targetClassName = if (propertyDescriptor.typeClassName.isList()) {
      val entityModelName = "${propertyDescriptor.arguments.first().type}EntityModel"
      ClassName(entityModelPackageName, entityModelName)
    } else {
      val entityModelName = "${propertyDescriptor.entityModelWrapperTypeName}"
      //        logger.warn("apiModelName $apiModelName")
      ClassName(entityModelPackageName, entityModelName)
    }
    val stringToListFunc = genStringToList(targetClassName)
    val listToStringFunc = genListToString(targetClassName)
    return TypeSpec.classBuilder(typeConverterClassName)
//      .addModifiers(KModifier.INTERNAL)
      .addFunction(stringToListFunc.build())
      .addFunction(listToStringFunc.build())
  }

  private fun getEntityTypeConvertClassName(propertyDescriptor: PropertyDescriptor): ClassName {
    val className =
      "${classDeclaration.simpleName.getShortName()}${propertyDescriptor.className.getShortName().firstCharUpperCase()}${propertyDescriptor.typeClassName.simpleName}EntityTypeConverter"
    val typeConverterClassName = ClassName(entityModelPackageName, className)
    return typeConverterClassName
  }

  fun genListToString(entityModel: ClassName): FunSpec.Builder {
    val retCodeBlock = CodeBlock.builder()
      .addStatement("return Json.%M(someObjects)", serialEncodeToStringMemberName)
    val listToString = FunSpec.builder("listToString")
      .addAnnotation(roomTypeConverterClassName)
      .addParameter("someObjects", listClassName.parameterizedBy(entityModel))
      .returns(stringClassName)
      .addCode(retCodeBlock.build())
    return listToString
  }

  fun genStringToList(entityModel: ClassName): FunSpec.Builder {
    //        logger.warn("apiModelName $apiModelName")
    val retCodeBlock = CodeBlock.builder()
      .addStatement(
        "return data?.let{ %T.decodeFromString<%T>(it) } ?: listOf()",
        serializableJsonClassName,
        listClassName.parameterizedBy(entityModel)
      )
    val stringToList = FunSpec.builder("stringToList")
      .addAnnotation(roomTypeConverterClassName)
      .addParameter("data", stringClassName.copy(nullable = true))
      .returns(listClassName.parameterizedBy(entityModel))
      .addCode(retCodeBlock.build())
    return stringToList
  }
}