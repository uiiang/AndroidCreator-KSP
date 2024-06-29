package uii.ang.creator.processor


import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

//  typeClassName=String
//	typeName=kotlin.String // kotlin.collections.List<uii.ang.domain.Album>
//	isNullable=false
//	className=startPage
//	arguments count=0
//	kDoc=
//	isParseRoot=false
data class PropertyDescriptor(
  val sourceClassName: ClassName,// 属性所属的原类
  val typeClassName: ClassName,//参数的类型className
  val typeName: TypeName,//参数的类型包含完整包名类名,包括?
  val wrapperTypeName: TypeName,
  val isNullable: Boolean,
  val className: KSName,//变量
  val arguments: List<KSTypeArgument>,//泛型参数
  val mandatoryForConstructor: Boolean,
  val kDoc: String,
  val isParseRoot: Boolean = false, // 是否标有@ParseRoot注解，在生成Response时做为参数传入
  val isParseReturn: Boolean = false, // 是否标有@ParseReturn注解， 在生成Repository时做为返回参数
  val parseReturnChain: List<PropertyDescriptor> = emptyList(),
  val resolve: KSType
) {
  override fun toString(): String {
    return "PropertyDescriptor" +
            "\tsourceClassName=${sourceClassName.simpleName}\n" +
            "\ttypeClassName=${typeClassName.simpleName}\n" +
            "\ttypeName=${typeName}\n" +
            "\tisNullable=${isNullable}\n" +
            "\tclassName=${className.getShortName()}\n" +
            "\targuments count=${arguments.count()}\n" +
            "\tkDoc=$kDoc\n" +
            "\tisParseRoot=$isParseRoot\n"+
            "\tisParseReturn=$isParseReturn\n"
  }
}
