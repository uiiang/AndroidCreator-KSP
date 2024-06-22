package uii.ang.creator.processor


import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

data class PropertyDescriptor(
  val typeClassName: ClassName,//参数的类型className
  val typeName: TypeName,//参数的类型包含完整包名类名,包括?
  val isNullable: Boolean,
  val className: KSName,//变量
  val arguments: List<KSTypeArgument>,//泛型参数
  val mandatoryForConstructor: Boolean,
  val kDoc: String,
)
