package uii.ang.creator.annotation


/**
 * 用于标注RetrofitImpl中返回哪个数据
 * 比如一个数据类内含有其它数据类，并可能有多层数据类，而要使用的可能是三四层之后某个数据
 * 则在该数据上标注此注解，
 *
 * example:
 * data class Results(
 *   val albummatches: Albummatches,
 * )
 * data class Albummatches(
 *   val album: List<Album>
 * )
 * data class Album(
 *   @ParseReturn
 *   val image: List<Image>,
 * )
 *
 * 我们只需要Album下的List<Image>数据，上面那些不考虑的情况下，就在image上标注此注解
 * RepositoryImpl 中则会生成代码
 * override suspend fun searchAlbum(album: String, limit: Int): Result<List<Image>>
 * 返回的数据是 Result<List<Image>>
 * 方法内转换数据时
 * val result = apiResult.data
 *     		.results
 *     		.albummatches
 *     		.album
 *     		.image
 *     		.map { it.toDomainModel() }
 * 会自动找到 results 下指向image的调用链并调用toDomainModel方法
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class ParseReturn


/**
 * 用于标注Response构造函数传入哪个参数
 *
 * 比如一个数据类中有多个参数，标注此注解的参数则会转化为ApiModel并传入到Response中解析数据
 *
 * internal data class GetAlbumInfoResponse(
 *
 *     @SerialName("album") val album: AlbumApiModel,
 *
 * )
 *
 * 标注到Album上，参数转化为AlbumApiModel
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class ParseRoot