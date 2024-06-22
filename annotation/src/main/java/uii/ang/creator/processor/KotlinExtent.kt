package uii.ang.creator.processor

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import uii.ang.creator.annotation.Creator
import uii.ang.creator.annotation.Parameter
import java.util.Locale

fun Parameter.Companion.from(annotation: KSAnnotation) = Parameter(
  paramName = annotation.arguments.first { it.name?.asString() == Parameter::paramName.name }.value as String,
  paramType = annotation.arguments.first { it.name?.asString() == Parameter::paramType.name }.value as String,
  paramDefault = annotation.arguments.first { it.name?.asString() == Parameter::paramDefault.name }.value as String,
  paramQueryType = annotation.arguments.first { it.name?.asString() == Parameter::paramQueryType.name }.value as String,
)

fun KSValueParameter.typeClassDeclaration(): KSClassDeclaration?
  = this.type.resolve().classDeclaration()

fun CreatorData.getPackageName(): String =
  sourceClassDeclaration.packageName.asString()

fun CreatorData.getApiModelClass(): ClassName {
  return ClassName(
    getPackageName(),
    sourceClassDeclaration.simpleName.getShortName() + "ApiModel"
  )
}

fun CreatorData.getResponseClass(): ClassName {
  return ClassName(
    getPackageName(),
    annotationData.responseClassName.ifEmpty { "${sourceClassDeclaration.simpleName.getShortName()}Response" })
}

fun String.capitalizeAndAddSpaces(): String {
  val tmpStr = replace(Regex("[A-Z]")) { " " + it.value.lowercase(Locale.getDefault()) }
  return tmpStr.replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
  } + "."
}

fun String.firstCharUpperCase(): String {
  return replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
}

fun String.firstCharLowerCase(): String {
  return replaceFirstChar { if (it.isUpperCase()) it.lowercaseChar() + "" else it.toString() }
}


fun TypeName.isBaseType(): Boolean {
  val typeName = toString().replace("?","")
  return typeName == "kotlin.Long" ||
          typeName == "kotlin.Double" ||
          typeName == "kotlin.Float" ||
          typeName == "kotlin.Int" ||
          typeName == "kotlin.Short" ||
          typeName == "kotlin.Char" ||
          typeName == "kotlin.Byte" ||
          typeName == "kotlin.String"
}

fun TypeName.isList():Boolean{
  return (toString().startsWith("kotlin.collections.List"))
}
fun KSPropertyDeclaration.isNullable(): Boolean {
  return type.resolve().isMarkedNullable
}


fun getListGenericsCreatorAnnotation(propertyDesc: PropertyDescriptor):
        KSNode? {
  val ksType = propertyDesc.arguments.first()
    .type?.resolve()
  val annoList = ksType?.declaration?.annotations
    ?.filter {
      it.isCreatorAnnotation()
    } ?: emptySequence()
  return if(annoList.count()>0) annoList.first().parent else null
}

fun KSAnnotation.isCreatorAnnotation():Boolean {
  return shortName.getShortName() == Creator::class.simpleName
}

fun KSType.isNullable(): Boolean {
  return this.isMarkedNullable || this.nullability == Nullability.NULLABLE || this.nullability == Nullability.PLATFORM
}

fun KSType.classDeclaration(): KSClassDeclaration? = when (this.declaration) {
  is KSTypeAlias -> (this.declaration as KSTypeAlias).type.resolve().classDeclaration()
  is KSClassDeclaration -> this.declaration as KSClassDeclaration
  else -> null
}



