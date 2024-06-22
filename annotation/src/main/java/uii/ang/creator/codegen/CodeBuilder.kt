package uii.ang.creator.codegen

import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName

class CodeBuilder private constructor(
  private val builder: FileSpec.Builder,
  private val typeBuilder: TypeSpec.Builder?
) {
  fun addConstructor(funConstructor: FunSpec.Builder) {
    typeBuilder?.primaryConstructor(funConstructor.build())
  }

  fun addFunction(funSpec: FunSpec, toType: Boolean) {
    if (toType) {
      typeBuilder?.addFunction(funSpec)
    } else {
      builder.addFunction(funSpec)
    }
//    if (originating != null) this.originating += originating
  }
  fun addImport(type: KSType, alias: String) {
    builder.addAliasedImport(type.toClassName(), alias)
  }

  fun build(): FileSpec {
    if (typeBuilder != null) {
      builder.addType(typeBuilder.build())
    }
    return builder.build()
  }

  companion object {

    private val cache = mutableMapOf<QualifiedName, CodeBuilder>()

    fun allBuilder(): Iterable<CodeBuilder> = cache.values
    fun all():Map<QualifiedName, CodeBuilder> = cache
    fun clear() {
      cache.clear()
    }

    fun getOrCreate(
      packageName: String,
      fileName: String,
      typeBuilderProvider: () -> TypeSpec.Builder? = { null }
    ): CodeBuilder {
      val qualifiedName = QualifiedName(packageName, fileName)
      return cache[qualifiedName] ?: CodeBuilder(
        builder = FileSpec.builder(qualifiedName.packageName, qualifiedName.fileName),
        typeBuilder = typeBuilderProvider.invoke(),
//        originating = mutableSetOf(),
      ).also {
        cache[qualifiedName] = it
      }
    }

    data class QualifiedName(
      val packageName: String,
      val fileName: String
    )
  }
}