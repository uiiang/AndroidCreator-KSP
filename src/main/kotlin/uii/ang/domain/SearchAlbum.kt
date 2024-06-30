package uii.ang.domain

import uii.ang.creator.annotation.*

@Creator(
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
  ]
)
data class SearchAlbum(
  @ParseRoot
  val results: Results
)

@Creator(generateApiModel = true)
data class Results(
  val opensearchQuery: OpensearchQuery,
  val opensearchTotalResults: String,
  val opensearchStartIndex: String,
  val opensearchItemsPerPage: String,
  val albummatches: Albummatches,
  val attr: Attr
)

@Creator(generateApiModel = true)
data class Albummatches(
  val album: List<Album>
)

@Creator(generateApiModel = true)
data class Album(
  val name: String,
  val artist: String,
  val url: String,
  @ParseReturn
  val image: List<Image>,
  val streamable: String,
  val mbid: String
)

@Creator(generateApiModel = true)
data class Image(
  val text: String,
  val size: String
)

@Creator(generateApiModel = true)
data class Attr(
  val attrFor: String
)

@Creator(generateApiModel = true)
data class OpensearchQuery(
  val text: String,
  val role: String,
  val searchTerms: String,
  val startPage: String
)
