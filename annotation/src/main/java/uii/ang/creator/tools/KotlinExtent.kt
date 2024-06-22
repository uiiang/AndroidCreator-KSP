package uii.ang.creator.tools

class KotlinExtent {
}
fun Class<*>.isBaseType():Boolean{
  return this.kotlin==Int::class ||
          this.kotlin ==Float::class||
          this.kotlin ==Double::class||
          this.kotlin==Long::class||
          this.kotlin==Boolean::class||
          this.kotlin==Short::class||
          this.kotlin==Byte::class||
          this.kotlin==Char::class
}
