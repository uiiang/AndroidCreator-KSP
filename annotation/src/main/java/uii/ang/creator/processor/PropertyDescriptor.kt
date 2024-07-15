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
  /**
   * 属性所属的原类
   */
  val sourceClassName: ClassName,// 属性所属的原类
  /**
   * 参数的类型className
   *
   * exp: typeClassName=String
   */
  val typeClassName: ClassName,//参数的类型className
  /**
   * 参数的类型包含完整包名类名, 如果可空则包括?
   *
   * exp: kotlin.String // kotlin.collections.List<uii.ang.domain.Album>
   */
  val typeName: TypeName,//参数的类型包含完整包名类名,包括?
  /**
   * 参数被包裹后的类，如果是list型就转成List<xxxApiModel>如果是普通类型就直接返回
   */
  val apiModelWrapperTypeName: TypeName, // 参数被包裹后的类，如果是list型就转成List<xxxApiModel>如果是普通类型就直接返回
  /**
   * 参数被包裹后的类，如果是list型就转成List<xxxEntityModel>如果是普通类型就直接返回
   */
  val entityModelWrapperTypeName: TypeName, // 参数被包裹后的类，如果是list型就转成List<xxxEntityModel>如果是普通类型就直接返回
  /**
   * 该参数是否可空
   */
  val isNullable: Boolean,
  /**
   * 该参数的变量名
   *
   * exp:startPage
   */
  val className: KSName,//变量
  /**
   * 该参数如果是泛型， 则用List包含所有的泛型参数
   */
  val arguments: List<KSTypeArgument>,//泛型参数
  val mandatoryForConstructor: Boolean,
  val kDoc: String,
  /**
   * 是否标有@ParseRoot注解，在生成Response时做为参数传入
   */
  val isParseRoot: Boolean = false, // 是否标有@ParseRoot注解，在生成Response时做为参数传入
  /**
   * ParseReturn注解， 在生成Repository时做为返回参数
   */
  val isParseReturn: Boolean = false, // 是否标有@ParseReturn注解， 在生成Repository时做为返回参数
  /**
   * 如果标有ParseReturn注解，则在List中包含完整的调用链条
   */
  val parseReturnChain: List<PropertyDescriptor> = emptyList(),
  /**
   * 是否基本类型
   */
  val isBaseType:Boolean = false,
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
