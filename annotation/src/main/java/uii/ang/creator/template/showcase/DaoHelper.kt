package uii.ang.creator.template.showcase

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import uii.ang.creator.processor.Const.databasePackageName
import uii.ang.creator.processor.Const.listClassName
import uii.ang.creator.processor.Const.roomDeleteClassName
import uii.ang.creator.processor.Const.roomInsertClassName
import uii.ang.creator.processor.Const.roomQueryClassName
import uii.ang.creator.processor.Const.roomRoomDatabaseClassName
import uii.ang.creator.processor.Const.roomUpdateClassName
import uii.ang.creator.processor.CreatorData
import uii.ang.creator.processor.ProcessorHelper
import uii.ang.creator.processor.QueryData
import uii.ang.creator.tools.firstCharLowerCase

class DaoHelper(
  logger: KSPLogger,
  data: CreatorData
) : ProcessorHelper(logger, data) {


  val buildFuncTypeInsert = "insert"
  val buildFuncTypeInsertAll = "insertAll"
  val buildFuncTypeUpdate = "update"
  val buildFuncTypeUpdateAll = "updateAll"
  val buildFuncTypeDelete = "delete"
  val buildFuncTypeDeleteAll = "deleteAll"

  //
//  @Dao
//  internal interface AlbumDao {
//
//    @Query("SELECT * FROM albums")
//    suspend fun getAll(): List<AlbumEntityModel>
//
//    @Query("SELECT * FROM albums where artist = :artistName and name = :albumName and mbId = :mbId")
//    suspend fun getAlbum(artistName: String, albumName: String, mbId: String?): AlbumEntityModel
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertAlbums(albums: List<AlbumEntityModel>)
//  }
  fun genClassBuilder(): TypeSpec.Builder {
    val queryFuncCode = genDaoQueryFuncCode()
    val classBuilder = TypeSpec.interfaceBuilder(roomDaoInterfaceClassName)
//      .addModifiers(KModifier.INTERNAL)
      .addFunctions(queryFuncCode)
    return classBuilder
  }

  fun genDatabaseClass(): TypeSpec.Builder {
    val classBuilder = TypeSpec.classBuilder(ClassName(databasePackageName, "ProjDatabase"))
      .addModifiers(KModifier.ABSTRACT)
      .superclass(roomRoomDatabaseClassName)
    return classBuilder
  }

  fun genInDatabaseCodeBlock(): FunSpec.Builder {
    return FunSpec.builder(roomDaoInterfaceClassName.simpleName.firstCharLowerCase())
      .addModifiers(KModifier.ABSTRACT)
      .returns(roomDaoInterfaceClassName)
  }

  private fun genDaoQueryFuncCode(): List<FunSpec> {
//    data.primaryConstructorParameters.forEach {
//      logger.warn(it.toString())
//      it.queryData.forEach { query->
//        logger.warn("在属性${it.typeClassName.simpleName} 中找到query注解，查询类型为${query.annotationData.queryType} 方法名为${query.annotationData.queryMethodName}")
//      }
//    }
    val funSpecList = mutableListOf<FunSpec>()
    funSpecList.add(buildSqlGetAllFunction())
    funSpecList.add(buildSqlUpdateAndInsertFunction(buildFuncTypeInsert))
    funSpecList.add(buildSqlUpdateAndInsertFunction(buildFuncTypeInsertAll))
    funSpecList.add(buildSqlUpdateAndInsertFunction(buildFuncTypeUpdate))
    funSpecList.add(buildSqlUpdateAndInsertFunction(buildFuncTypeUpdateAll))
    funSpecList.add(buildSqlUpdateAndInsertFunction(buildFuncTypeDelete))
    funSpecList.add(buildSqlUpdateAndInsertFunction(buildFuncTypeDeleteAll))
    val queryDataList = data.primaryConstructorParameters
      .flatMap { param ->
        param.queryData
      }
//    queryDataList.forEach { it->
//      logger.warn("queryDataList ${ it.queryMethodName }")
//    }
    val methodNameGroupby = queryDataList.groupBy { it.queryMethodName }
    if (methodNameGroupby.isNotEmpty()) {
      val funcList = methodNameGroupby.map { (t, u) ->
        logger.warn("genDaoQueryFuncCode key ${t}")
        buildSqlFunction(u, t)
      }.toList()
      funSpecList.addAll(funcList)
    }
    return funSpecList
  }

  private fun buildSqlUpdateAndInsertFunction(buildType: String): FunSpec {
    val classNameStr = data.sourceClassDeclaration.simpleName.getShortName()
    val funcSpec = when (buildType) {
      buildFuncTypeUpdate -> FunSpec.builder("update")
        .addParameter(classNameStr.lowercase(), entityModelClassName)
        .addAnnotation(AnnotationSpec.builder(roomUpdateClassName).build())

      buildFuncTypeUpdateAll -> FunSpec.builder("update")
        .addParameter(classNameStr.lowercase() + "List", listClassName.parameterizedBy(entityModelClassName))
        .addAnnotation(AnnotationSpec.builder(roomUpdateClassName).build())

      buildFuncTypeInsert -> FunSpec.builder("insert")
        .addParameter(classNameStr.lowercase(), entityModelClassName)
        .addAnnotation(AnnotationSpec.builder(roomInsertClassName).build())

      buildFuncTypeInsertAll -> FunSpec.builder("insert")
        .addParameter(classNameStr.lowercase() + "List", listClassName.parameterizedBy(entityModelClassName))
        .addAnnotation(AnnotationSpec.builder(roomInsertClassName).build())

      buildFuncTypeDelete -> FunSpec.builder("delete")
        .addParameter(classNameStr.lowercase(), entityModelClassName)
        .addAnnotation(AnnotationSpec.builder(roomDeleteClassName).build())

      buildFuncTypeDeleteAll -> FunSpec.builder("delete")
        .addParameter(classNameStr.lowercase() + "List", listClassName.parameterizedBy(entityModelClassName))
        .addAnnotation(AnnotationSpec.builder(roomDeleteClassName).build())

      else -> FunSpec.builder("insert")
        .addParameter(classNameStr.lowercase(), listClassName.parameterizedBy(entityModelClassName))
        .addAnnotation(AnnotationSpec.builder(roomInsertClassName).build())
    }
    val genFunction = funcSpec
      .addModifiers(KModifier.ABSTRACT, KModifier.SUSPEND)
    return genFunction.build()
  }

  private fun buildSqlGetAllFunction(): FunSpec {
    val sqlStr = "\"SELECT * FROM ${data.sourceClassDeclaration.simpleName.getShortName().lowercase()} \""
    val sqlCodeBlock = CodeBlock.builder().addStatement(sqlStr)
    val queryAnno = AnnotationSpec.builder(roomQueryClassName)
      .addMember(sqlCodeBlock.build())
    val genFunction = FunSpec.builder("getAll")
      .addModifiers(KModifier.ABSTRACT, KModifier.SUSPEND)
      .addAnnotation(queryAnno.build())
      .returns(listClassName.parameterizedBy(entityModelClassName))
    return genFunction.build()
  }

  private fun buildSqlFunction(
    u: List<QueryData>,
    t: String
  ): FunSpec {
    val sqlParam = u.map { query ->
      logger.warn("\tvalue $query")
      "${query.paramName} = :${query.paramName}"
    }
    val paramSpecList = u.map { query ->
      ParameterSpec.builder(query.paramName, query.typeClassName).build()
    }
    val sqlStr = "\"SELECT * FROM ${data.sourceClassDeclaration.simpleName.getShortName().lowercase()} " +
            "${sqlParam.joinToString(" and ")} \""
    val sqlCodeBlock = CodeBlock.builder().addStatement(sqlStr)
    val queryAnno = AnnotationSpec.builder(roomQueryClassName)
      .addMember(sqlCodeBlock.build())
    val genFunction = FunSpec.builder(t)
      .addModifiers(KModifier.ABSTRACT, KModifier.SUSPEND)
      .addAnnotation(queryAnno.build())
      .addParameters(paramSpecList)
      .returns(listClassName.parameterizedBy(entityModelClassName))
    return genFunction.build()
  }
}