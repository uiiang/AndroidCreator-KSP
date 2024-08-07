package uii.ang.creator.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import uii.ang.creator.annotation.*
import uii.ang.creator.processor.Const.apiModelPackageName
import uii.ang.creator.processor.Const.entityModelPackageName
import uii.ang.creator.processor.Const.hashMapClassName
import uii.ang.creator.processor.Const.listClassName
import uii.ang.creator.processor.Const.requestBodyPackageName
import uii.ang.creator.processor.Const.retrofitBodyClassName
import uii.ang.creator.processor.Const.retrofitFieldClassName
import uii.ang.creator.processor.Const.retrofitGetClassName
import uii.ang.creator.processor.Const.retrofitPathClassName
import uii.ang.creator.processor.Const.retrofitPostClassName
import uii.ang.creator.processor.Const.retrofitQueryClassName
import uii.ang.creator.processor.Const.serializableSerialNameClassName
import uii.ang.creator.processor.Const.stringClassName
import uii.ang.creator.tools.*

object Utils {
  fun convertToType(type: String) = when (type) {
    "String" -> ""
    "Long" -> ".toLong()"
    "Int" -> ".toInt()"
    "Double" -> ".toDouble()"
    "Float" -> ".toFloat()"
    "Short" -> ".toShort()"
    "Boolean" -> ".Boolean()"
    else -> ""
  }

  fun requestParamHasBody(parameters: List<Parameter>) =
    parameters.any { param -> param.paramQueryType == requestParamTypeBody }

  fun requestParamHasMap(parameters: List<Parameter>) =
    parameters.any { param -> param.paramQueryType == requestParamTypeMap }

  fun getRequestParamWithoutBody(parameters: List<Parameter>) =
    parameters.filter { param -> param.paramQueryType != requestParamTypeBody && param.paramQueryType != requestParamTypeMap }

  fun getRequestParamWithBody(parameters: List<Parameter>) =
    parameters.filter { param -> param.paramQueryType == requestParamTypeBody }

  fun getRequestParamWithMap(parameters: List<Parameter>) =
    parameters.filter { param -> param.paramQueryType == requestParamTypeMap }

  fun getRequestParameterSpecBody(objectName: String): ParameterSpec.Builder {
    val queryObjName = "${objectName}QueryBody"
    return ParameterSpec.builder(
      "paramData", ClassName(requestBodyPackageName, queryObjName.firstCharUpperCase())
    )
  }

  fun getRequestHashMapClassName() = hashMapClassName.parameterizedBy(listOf(stringClassName, stringClassName))
  fun getRequestParameterSpecBodyWithMap(): ParameterSpec.Builder {
    return ParameterSpec.builder(
      "paramMap",
      getRequestHashMapClassName()
    )
  }

  fun getRequestParameterSpecList(parameters: List<Parameter>, hasDefaultValue: Boolean = false): List<ParameterSpec> {
    return parameters.map { param ->
      val paramSpec = ParameterSpec.builder(param.paramName, convertType(param.paramType))
      if (hasDefaultValue && param.paramDefault.isNotEmpty()) {
        if (param.paramType == "String") {
          paramSpec.defaultValue("\"${param.paramDefault}\"")
        } else {
          paramSpec.defaultValue(param.paramDefault)
        }
      }
      paramSpec.build()
    }
  }

  fun getRetrofitClassName(method: String) = when (method) {
    requestMethodPost -> retrofitPostClassName
    requestMethodGet -> retrofitGetClassName
    requestParamTypeQuery -> retrofitQueryClassName
    requestParamTypePath -> retrofitPathClassName
    requestParamTypeField -> retrofitFieldClassName
    requestParamTypeBody -> retrofitBodyClassName
    requestParamTypeMap -> retrofitBodyClassName
    else -> retrofitGetClassName
  }

  fun convertType(type: String) = when (type) {
    "String" -> String::class
    "Long" -> Long::class
    "Int" -> Int::class
    "Double" -> Double::class
    "Float" -> Float::class
    "Short" -> Short::class
    "Boolean" -> Boolean::class
    else -> String::class
  }

  fun getListGenericsCreatorAnnotation(ksType: KSType):
          KSNode? {
    val annoList = ksType.declaration.annotations.filter {
      it.isCreatorAnnotation()
    }
    return if (annoList.count() > 0) annoList.first().parent else null
  }

  fun getListGenericsCreatorAnnotation(propertyDesc: PropertyDescriptor):
          KSNode? {
    val ksType = propertyDesc.arguments.first()
      .type?.resolve()
    val annoList = ksType?.declaration?.annotations
      ?.filter {
        isCreatorAnnotation(it)
      } ?: emptySequence()
    return if (annoList.count() > 0) annoList.first().parent else null
  }

  fun isParseReturnAnnotation(annotation: KSAnnotation): Boolean {
    return annotation.shortName.getShortName() == ParseReturn::class.simpleName
  }

  fun isCreatorAnnotation(annotation: KSAnnotation): Boolean {
    return annotation.shortName.getShortName() == Creator::class.simpleName
  }

  fun isNullable(propertyDeclaration: KSPropertyDeclaration): Boolean {
    return propertyDeclaration.type.resolve().isMarkedNullable
  }

  fun getClassName(
    classDeclaration: KSClassDeclaration,
    prex: String = "", suffix: String = ""
  ): String {
    return prex + classDeclaration.simpleName.asString() + suffix
  }

  fun getPackageName(classDeclaration: KSClassDeclaration): String {
    return classDeclaration.packageName.asString()
  }

  fun convertKSValueParameterToPropertyDescriptor(
    kp: KSValueParameter,
    sourceClassDeclaration: KSClassDeclaration,
    logger: KSPLogger
  ): PropertyDescriptor {
    logger.warn("解析字段${kp.name!!.getShortName()} 注解数量${kp.annotations.count()}")

    kp.annotations.forEach {
      logger.warn("\t找到注解2 ${it.annotationType.resolve().declaration.qualifiedName?.asString()}")
    }
    val resolve = kp.type.resolve()
    val queryData = kp.annotations.filter { anno ->
      (anno.annotationType.toTypeName() as? ClassName)?.canonicalName ==
              Query::class.qualifiedName
    }.map { anno ->
      QueryData.AnnotationData.from(anno)
    }.map { anno ->
      QueryData(
        annotationData = anno,
        typeClassName = resolve.toClassName()
      )
    }.toList()
    val toTypeName = resolve.toTypeName()
    val apiModelWrapperType = getApiModelWrapperType(kp, logger)
    val entityModelWrapperType = getEntityModelWrapperType(kp, logger)
    val isBaseType = isBaseType(kp)
    val isParseReturn = kp.hasAnnotation<ParseReturn>()
    val isToDatabase = kp.hasAnnotation<ToDatabase>()
    val isPk = kp.hasAnnotation<PK>()
    val isQuery = kp.hasAnnotation<Query>()
    return PropertyDescriptor(
      sourceClassName = sourceClassDeclaration.toClassName(),
      typeClassName = resolve.toClassName(),
      //包含完整包名类名
      typeName = toTypeName,
      apiModelWrapperTypeName = apiModelWrapperType,
      entityModelWrapperTypeName = entityModelWrapperType,
      isNullable = resolve.isNullable(),
      //仅类名
      className = kp.name!!,
      //泛型参数
      arguments = resolve.arguments,
      mandatoryForConstructor = true,
      kDoc = "",
      isSerialName = kp.hasAnnotation(serializableSerialNameClassName),
      isParseRoot = kp.hasAnnotation<ParseRoot>(),
      isParseReturn = isParseReturn,
      isToDatabase = isToDatabase,
      isPk = isPk,
      isQuery = isQuery,
      queryData = queryData,
      isBaseType = isBaseType,
      resolve = resolve,
    )
  }

  private fun isBaseType(it: KSValueParameter): Boolean {
    val resolve = it.type.resolve()
    val toTypeName = resolve.toTypeName()
    return toTypeName.isBaseType()
  }

  private fun getEntityModelWrapperType(it: KSValueParameter,
                                        logger: KSPLogger): TypeName {
    val resolve = it.type.resolve()
    val toTypeName = resolve.toTypeName()
    val wrapperType = if (toTypeName.isBaseType()) {
      toTypeName
    } else if (toTypeName.isList()) {
      // 如果是list，获取list中的泛型类，
      // 如果泛型类是注解Creator的数据类，转换成EntityModel
      // 判断当前list的泛型类是否为creator
      val ksNode = resolve.arguments.first()
        .type?.resolve()?.let { it1 -> getListGenericsCreatorAnnotation(it1) }
      ksNode?.let { node ->
        // 字段属性为List<注解了Create的data class>
        val entityModelName = node.toString() + "EntityModel"
        //        logger.warn("apiModelName $apiModelName")
        val entityModelClass = ClassName(entityModelPackageName, entityModelName)
        val parameterizedBy = listClassName.parameterizedBy(entityModelClass)
        //        logger.warn("  convert parameterizedBy ${parameterizedBy.toString()}")
        parameterizedBy
      } ?: toTypeName
    } else {
      // 字段属性为data class
      // 非基本类型且不为list的情况下，找到此字段数据类型并生成EntityModel类名
      logger.warn("找到字段 名为${it.name!!.getShortName()} 数据类型为 ${it.type}")
      val retClassName = ClassName(
        entityModelPackageName,
        "${it.type}EntityModel"
      )
      //      logger.warn("  convert api model ${retClassName.simpleName}")
      retClassName
    }
    return wrapperType
  }

  private fun getApiModelWrapperType(
    it: KSValueParameter,
    logger: KSPLogger
  ): TypeName {
    val resolve = it.type.resolve()
    val toTypeName = resolve.toTypeName()
    val wrapperType = if (toTypeName.isBaseType()) {
      toTypeName
    } else if (toTypeName.isList()) {
      logger.warn("开始为${toTypeName}组装 apimodel 包装类")
      // 如果是list，获取list中的泛型类，
      // 如果泛型类是注解Creator的数据类，转换成apimodel
      // 判断当前list的泛型类是否为creator
      val ksNode = resolve.arguments.first()
        .type?.resolve()?.let { it1 -> getListGenericsCreatorAnnotation(it1) }
      ksNode?.let { node ->
        // 字段属性为List<注解了Create的data class>
        val apiModelName = node.toString() + "ApiModel"
        logger.warn("组装apiModelName类名为 $apiModelName")
        val apiModelClass = ClassName(apiModelPackageName, apiModelName)
        val parameterizedBy = listClassName.parameterizedBy(apiModelClass)
        //        logger.warn("  convert parameterizedBy ${parameterizedBy.toString()}")
        parameterizedBy
      } ?: toTypeName
    } else {
      // 字段属性为data class
      // 非基本类型且不为list的情况下，找到此字段数据类型并生成apiModel类名
      logger.warn("找到字段 名为${it.name!!.getShortName()} 数据类型为 ${it.type}")
      val retClassName = ClassName(
        apiModelPackageName,
        "${it.type}ApiModel"
      )
      //      logger.warn("  convert api model ${retClassName.simpleName}")
      retClassName
    }
    return wrapperType
  }

  fun findHasCreatorAnnoClass(param: KSValueParameter, logger: KSPLogger): KSClassDeclaration? {
    val resolve = param.type.resolve()
    val toTypeName = resolve.toTypeName()
    if (toTypeName.isList()) {
      logger.warn("\t\t检查参数${param.name?.getShortName()}为List类型")
      val ksType = resolve.arguments.first()
        .type?.resolve()
      val annoList = ksType?.declaration?.annotations
        ?.filter {
          isCreatorAnnotation(it)
        } ?: emptySequence()
      if (annoList.count() > 0) {
        return ksType?.classDeclaration()
      }
    } else if (!toTypeName.isBaseType()) {
      logger.warn("\t\t检查参数${param.name?.getShortName()}为非List并且不是基本类型")
      val annoList = resolve.classDeclaration()?.annotations ?: emptySequence<KSAnnotation>()
      annoList.onEach {
        logger.warn("\t\t参数的类型标注注解${it.shortName.getShortName()}")
      }
        .filter { it.isCreatorAnnotation() }
      if (annoList.count() > 0) {
        return resolve.classDeclaration()
      }
    } else {
      logger.warn("\t\t检查参数${param.name?.getShortName()}为其它类型")
      return null
    }
    return null
  }

  /**
   * 查找标注ParseReturn注解的参数链条
   */
  fun findParseReturnChain(classDeclaration: KSClassDeclaration, logger: KSPLogger): Map<KSName, TypeName> {
//    val chainList: MutableList<PropertyDescriptor> = mutableListOf()
    val chainMap: MutableMap<KSName, TypeName> = mutableMapOf()
    val tmpMap: MutableMap<KSName, TypeName> = mutableMapOf()

    // name, typeName是当前要解析的类的上一级类的标识，用于保存链条的顺序
    fun traverse(
      classDeclaration: KSClassDeclaration,
      upperName: KSName?,
      upperTypeName: TypeName?,
      logger: KSPLogger
    ) {
      val currentClassName = classDeclaration.simpleName.getShortName()
      logger.warn("开始检查数据类$currentClassName")
      // 1, 判断当前参数是否标注@ParseReturn
      // 1.1, 如果有返回参数typeName kotlin.String // kotlin.collections.List<uii.ang.domain.Album>
      // 1.2, 如果没有判断参数原类型是否为标注@Creator注解的自定义dataclass，
      // 如果是则使用自定义dataclass的构造函数里的参数递归判断第1步
      // 如果没有找到链条，没有标注ParseReturn注解，则使用原数据类做为返回数据类型
      val parameters = classDeclaration.primaryConstructor?.parameters ?: emptyList()
      // 构造函数中的参数不为空时
      parameters.filter {
        // 1, 判断当前参数是否标注@ParseReturn
//      logger.warn("\t开始检查参数${it.name?.getShortName()}")
        it.hasAnnotation<ParseReturn>()
      }
        .onEach { logger.warn("\t在数据类$currentClassName 中找到一个ParseReturn注解， ${it.name?.getShortName()}") }
        .onEach {
          // 将符合条件的类转换成PropertyDescriptor
          val resolve = it.type.resolve()
          val toTypeName = resolve.toTypeName()
          if (upperName != null && upperTypeName != null) {
            tmpMap[upperName] = upperTypeName
          }
          chainMap.putAll(tmpMap)
          chainMap[it.name!!] = toTypeName
          tmpMap.clear()
//          chainList.add(convertKSValueParameterToPropertyDescriptor(it, classDeclaration))
//          logger.warn("chainList = ${chainList.count()}")
        }

//      if (hasParseReturnList.isEmpty()) {
      // 1.2, 如果没有判断参数原类型是否为标注@Creator注解的自定义dataclass，
      logger.warn("数据类$currentClassName 的参数中未发现使用@ParseReturn注解的参数，开始检查参数是否为@Creator")

      parameters.filter {
        val resolve = it.type.resolve()
        val toTypeName = resolve.toTypeName()
        !toTypeName.isBaseType()
      }.onEach {
        val resolve = it.type.resolve()
        val toTypeName = resolve.toTypeName()
        // toTypeName=kotlin.collections.List<uii.ang.domain.Nation> isList=true hasParseReturn=true
        // toTypeName=$toTypeName isList=${toTypeName.isList()}
        logger.warn("\t找到参数 ${it.name?.getShortName()} 是否List对象=${toTypeName.isList()} 完整类型为=$toTypeName ")
        val creatorDataClass = findHasCreatorAnnoClass(it, logger)
        logger.warn("\t\t此参数为@Creator注解数据类=${creatorDataClass != null}")
        creatorDataClass?.let { cd ->
          if (upperName != null && upperTypeName != null) {
            tmpMap[upperName] = upperTypeName
          }
          traverse(cd, it.name, toTypeName, logger)
        }
      }
//      }
    }
    traverse(classDeclaration, null, null, logger)
    if (chainMap.isEmpty()) {
      chainMap[classDeclaration.simpleName] = classDeclaration.toClassName()
    }
    logger.warn("查找${classDeclaration.simpleName.getShortName()}的ParseReturn链条结果 ${chainMap.count()} 层")
    chainMap.forEach { (t, u) ->
      logger.warn("\t参数名：${t.getShortName()} 类型：${u}")
    }
    return chainMap
  }
}