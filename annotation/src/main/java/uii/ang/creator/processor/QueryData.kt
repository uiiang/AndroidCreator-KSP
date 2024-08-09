package uii.ang.creator.processor

import com.google.devtools.ksp.symbol.KSAnnotation
import com.squareup.kotlinpoet.ClassName
import uii.ang.creator.annotation.Query
import uii.ang.creator.annotation.queryTypeEquals
import uii.ang.creator.processor.CreatorData.AnnotationData

class QueryData(
  val annotationData: AnnotationData,
  /**
   * 参数的类型className
   *
   * exp: typeClassName=String
   */
  val typeClassName: ClassName,//参数的类型className
//  val logger: KSPLogger
) : AnnotatedBaseData {
  val paramName: String = annotationData.paramName
  val queryMethodName: String = annotationData.queryMethodName
  val queryType: String = annotationData.queryType

  data class AnnotationData(
    val queryType: String = queryTypeEquals,
    // 查询数据库的方法名，多个字段相同方法名拼在一个方法里
    val queryMethodName: String,
    val paramName: String,
  ) {
    companion object {
      fun from(annotation: KSAnnotation): AnnotationData {
        return AnnotationData(
          paramName = annotation.parent.toString(),
          queryType = annotation.arguments.first { it.name?.asString() == Query::queryType.name }.value as String,
          queryMethodName = annotation.arguments.first { it.name?.asString() == Query::queryMethodName.name }.value as String,
        )
      }
    }
  }

  override fun toString(): String {
    return "QueryData\n" +
            "\tqueryMethodName = $queryMethodName\n" +
            "\tqueryType = $queryType\n" +
            "\tparamName = $paramName\n" +
            "\tparamType = ${typeClassName.simpleName}"
  }
}