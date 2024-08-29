package uii.ang.domain

import uii.ang.creator.annotation.*
import kotlinx.serialization.Serializable

@Creator(
  generateApiType = apiTypeKtor,
  generateApiModel = true,
  generateResponse = true,
  generateRetrofitService = true,
  retrofitServiceClassName = "Album",
  method = requestMethodPost,
  methodName = "searchAlbum",
  url = "./?method=album.search",
  parameters = [
    Parameter(paramName = "album", paramType = "String", paramQueryType = "Query"),
    Parameter(paramName = "limit", paramType = "Int", paramQueryType = "Query", paramDefault = "60")
  ],
  getCallFailureFuncPath = "getCallFailure",
  checkResponseSuccessFuncPath = "checkResponseSuccess"
)
data class SearchAlbum(
  @ParseRoot
  val results: Results
)

@Serializable
data class Results(
  val opensearchQuery: OpensearchQuery,
  val opensearchTotalResults: String,
  val opensearchStartIndex: String,
  val opensearchItemsPerPage: String,
  val albummatches: Albummatches,
  val attr: Attr
)

@Serializable
data class Albummatches(
  @ParseReturn
//  @ToDatabase
  val album: List<Album>
)

@Creator(
  generateApiType = apiTypeKtor,
  generateApiModel = true,
//  generateResponse = true,
//  generateRetrofitService = true,
//  generatorEntityModel = true,
  retrofitServiceClassName = "Album",
  method = requestMethodPost,
  methodName = "getAlbum",
  url = "./?method=album.getInfo",
  parameters = [
    Parameter(paramName = "artist", paramType = requestParamDataTypeString, paramQueryType = requestParamTypeField),
    Parameter(
      paramName = "album",
      paramType = requestParamDataTypeInt,
      paramQueryType = requestParamTypeMap,
      paramDefault = "0"
    ),
    Parameter(paramName = "mbid", paramType = requestParamDataTypeString, paramQueryType = requestParamTypeMap)
  ],
  getCallFailureFuncPath = "getCallFailure",
  checkResponseSuccessFuncPath = "checkResponseSuccess"
)
@Serializable
data class Album(
  @Query(queryMethodName = "queryAlbumByName", queryType = queryTypeEquals)
  val name: String,
  val artist: String,
  val url: String,
  val image: List<Image>?,
  val streamable: String,
  val mbid: String
)

@Serializable
data class Image(
  val text: String,
  val size: String
)

@Serializable
data class Attr(
  val attrFor: String
)

@Serializable
data class OpensearchQuery(
  val text: String,
  val role: String,
  val searchTerms: String,
  val startPage: String
)
