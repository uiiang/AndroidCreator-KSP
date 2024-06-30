package uii.ang.creator.template.showcase

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.*
import uii.ang.creator.processor.Const.koinModuleClassName
import uii.ang.creator.processor.Const.koinModuleFunClassName
import uii.ang.creator.processor.CreatorData
import uii.ang.creator.processor.ProcessorHelper

/**
 * 生成该模块下的 dataModule 类，用于koin注入，一个模块只有一个 dataModule
 * 该模块下的网络 XxxRepository，XxxRepositoryImpl和数据处理 XxxDatabase相关类都在此注入
 */
// 生成如下代码
//internal val dataModule = module {
//
//  single<AlbumRepository> { AlbumRepositoryImpl(get(), get()) }
//
//  single { get<Retrofit>().create(AlbumRetrofitService::class.java) }
//
//  single {
//    Room.databaseBuilder(
//      get(),
//      AlbumDatabase::class.java,
//      "Albums.db",
//    ).build()
//  }
//
//  single { get<AlbumDatabase>().albums() }
//}
class DataModuleHelper(logger: KSPLogger, data: CreatorData) : ProcessorHelper(logger, data) {

  fun genClassBuilder(): PropertySpec.Builder {
    return PropertySpec.builder("dataModuleGen", koinModuleClassName)
      .addModifiers(KModifier.INTERNAL)
      .initializer("%T ", koinModuleFunClassName)
  }
}