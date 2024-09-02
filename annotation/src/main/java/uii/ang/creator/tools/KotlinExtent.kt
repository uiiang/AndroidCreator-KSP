package uii.ang.creator.tools


import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import uii.ang.creator.annotation.Creator
import uii.ang.creator.annotation.Parameter
import uii.ang.creator.processor.CreatorData
import java.util.*
import javax.lang.model.element.AnnotationMirror

class KotlinExtent {
}

fun Class<*>.isBaseType(): Boolean {
  return this.kotlin == Int::class ||
          this.kotlin == Float::class ||
          this.kotlin == Double::class ||
          this.kotlin == Long::class ||
          this.kotlin == Boolean::class ||
          this.kotlin == Short::class ||
          this.kotlin == Byte::class ||
          this.kotlin == Char::class
}


operator fun CodeBlock.plus(other: CodeBlock): CodeBlock {
  return CodeBlock.Builder().add(this).add(other).build()
}

fun List<AnnotationSpec>.get(typeName: TypeName): AnnotationSpec? =
  find { it.typeName == typeName }

internal fun AnnotationSpec.value(property: String): Any? =
  tag<AnnotationMirror>()
    ?.elementValues
    ?.entries
    ?.singleOrNull { it.key.simpleName.toString() == property }
    ?.value
    ?.value

fun TypeName.isAssignable(initializer: TypeName): Boolean {
  if (this == initializer) return true
  if (this.asNotNullable() != initializer.asNotNullable()) return false
  return this.isNullable
}

fun <T : TypeName> T.asNotNullable(): T = nullable(false)
fun <T : TypeName> T.asNullable(): T = nullable(true)
fun <T : TypeName> T.nullable(nullable: Boolean): T = copy(nullable = nullable) as T

fun TypeName.isOneOf(vararg typeNames: TypeName): Boolean {
  val thisNotNullable = (this as? ParameterizedTypeName)?.rawType ?: this.asNotNullable()
  return typeNames.any { typeName -> thisNotNullable == typeName.asNotNullable() }
}

val TypeSpec.isData: Boolean get() = modifiers.contains(KModifier.DATA)

fun TypeName.isAny(): Boolean =
  isOneOf(ANY)

fun TypeName.isString(): Boolean =
  isOneOf(STRING)

fun TypeName.isBoolean(): Boolean =
  isOneOf(BOOLEAN)

fun TypeName.isList(): Boolean =
  isOneOf(LIST, COLLECTION, ITERABLE)

fun TypeName.isMap(): Boolean =
  isOneOf(MAP)

fun TypeName.isLong(): Boolean =
  isOneOf(LONG)

fun TypeName.isChar(): Boolean =
  isOneOf(CHAR)

fun TypeName.isFloat(): Boolean =
  isOneOf(FLOAT)

fun TypeName.isDouble(): Boolean =
  isOneOf(DOUBLE)

fun TypeName.isInt(): Boolean =
  isOneOf(INT)

fun TypeName.isShort(): Boolean =
  isOneOf(SHORT)

fun TypeName.isByte(): Boolean =
  isOneOf(BYTE)

fun TypeName.isBaseType(): Boolean =
  isOneOf(STRING, BOOLEAN, LONG, CHAR, FLOAT, DOUBLE, INT, SHORT)

fun TypeName.primitiveDefaultInit(): String? {
  return when {
    isChar() -> "\'?\'"
    isByte() -> "0"
    isShort() -> "0"
    isInt() -> "0"
    isLong() -> "0L"
    isFloat() -> "0f"
    isDouble() -> "0.0"
    isBoolean() -> "false"
    isString() -> "\"\""
    else -> null
  }

}

fun Parameter.Companion.from(annotation: KSAnnotation) = Parameter(
  paramName = annotation.arguments.first { it.name?.asString() == Parameter::paramName.name }.value as String,
  paramType = annotation.arguments.first { it.name?.asString() == Parameter::paramType.name }.value as String,
  paramDefault = annotation.arguments.first { it.name?.asString() == Parameter::paramDefault.name }.value as String,
  paramQueryType = annotation.arguments.first { it.name?.asString() == Parameter::paramQueryType.name }.value as String,
  paramPostObjName = annotation.arguments.first { it.name?.asString() == Parameter::paramPostObjName.name }.value as String,
  paramPostObjType = annotation.arguments.first { it.name?.asString() == Parameter::paramPostObjType.name }.value as String,
)

fun KSValueParameter.typeClassDeclaration(): KSClassDeclaration? = this.type.resolve().classDeclaration()

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

fun KSPropertyDeclaration.isNullable(): Boolean {
  return type.resolve().isMarkedNullable
}


fun KSAnnotation.isCreatorAnnotation(): Boolean {
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

@OptIn(KspExperimental::class)
inline fun <reified T: Annotation> KSAnnotated.getAnnotation(): T? {
  return this.getAnnotationsByType(T::class).firstOrNull()
}

@OptIn(KspExperimental::class)
inline fun <reified T: Annotation>KSAnnotated.hasAnnotation(): Boolean {
  return this.isAnnotationPresent(T::class)
}

fun KSAnnotated.hasAnnotation(className: ClassName): Boolean {
  return this.annotations.find {
    it.annotationType.resolve().declaration.qualifiedName?.asString() == className.canonicalName
  } != null
}











