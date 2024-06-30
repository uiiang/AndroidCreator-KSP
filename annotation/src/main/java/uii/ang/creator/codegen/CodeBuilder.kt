package uii.ang.creator.codegen

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName


data class CollectCodeDesc(val qualifiedName: CodeBuilder.Companion.QualifiedName, val codeBlock: CodeBlock.Builder)

class CodeBuilder private constructor(
  private val builder: FileSpec.Builder,
  private val typeBuilder: TypeSpec.Builder?,
  private val propertyBuilder: PropertySpec.Builder?
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
    if (propertyBuilder != null) {
      builder.addProperty(propertyBuilder.build())
    }
    return builder.build()
  }


  companion object {

    private val cache = mutableMapOf<QualifiedName, CodeBuilder>()
    private val collectCache = mutableListOf<CollectCodeDesc>()

    fun allCollect(): List<CollectCodeDesc> = collectCache
    fun allBuilder(): Iterable<CodeBuilder> = cache.values
    fun all(): Map<QualifiedName, CodeBuilder> = cache
    fun clear() {
      cache.clear()
      collectCache.clear()
    }

    fun putCollectCodeBlock(packageName: String, fileName: String, collectCode: CodeBlock.Builder,
                            logger: KSPLogger
    ) {
      val qualifiedName = QualifiedName(packageName, fileName)
      val collectCodeDesc = CollectCodeDesc(qualifiedName, collectCode)
      collectCache.add(collectCodeDesc)
      logger.warn("putCollectCodeBlock ioc ${collectCache.count()}")
    }

    fun getOrCreate(
      packageName: String,
      fileName: String,
      typeBuilderProvider: () -> TypeSpec.Builder? = { null },
      propertyBuilderProvider: () -> PropertySpec.Builder? = { null },
    ): CodeBuilder {
      val qualifiedName = QualifiedName(packageName, fileName)
      return cache[qualifiedName] ?: CodeBuilder(
        builder = FileSpec.builder(qualifiedName.packageName, qualifiedName.fileName),
        typeBuilder = typeBuilderProvider.invoke(),
        propertyBuilder = propertyBuilderProvider.invoke()
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