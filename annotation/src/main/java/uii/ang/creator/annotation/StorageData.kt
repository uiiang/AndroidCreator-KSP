package uii.ang.creator.annotation

const val DATA_TYPE_STRING = "String"
const val DATA_TYPE_INT = "Int"
const val DATA_TYPE_FLOAT = "Float"
const val DATA_TYPE_BOOLEAN = "Boolean"
const val DATA_TYPE_LONG = "Long"
const val DATA_TYPE_DOUBLE = "Double"
const val DATA_TYPE_BYTEARRAY = "ByteArray"

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY)
annotation class StorageData(
  /**
   * 生成的方法名
   */
  val methodName: String,
  /**
   * 存取的数据类型
   */
  val dataType: String = DATA_TYPE_STRING,
)