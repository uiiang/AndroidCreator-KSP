package uii.ang.domain

import uii.ang.creator.annotation.Creator
import uii.ang.creator.annotation.Parameter
import uii.ang.creator.annotation.requestMethodPost

@Creator(
  generateResponse = true,
  responseClassName = "GetAlbumInfoResponse",
  generateRetrofitService = true,
  retrofitServiceClassName = "AlbumRetrofitService",
  method = requestMethodPost,
  url = "./?method=album.getInfo",
  methodName = "getAlbumInfoAsync",
  parameters = [
    Parameter(paramName = "phrase", paramType = "String", paramQueryType = "Field"),
    Parameter(paramName = "limit", paramType = "Int", paramQueryType = "Query", paramDefault = "11")
  ]
)
data class AlbumDetail(
  val album: Album
)

@Creator
data class Album(
  val artist: String?,
  val tags: Tags,
  val name: String,
  val image: List<Image>?,
  val tracks: Tracks,
  val listeners: String,
  val playcount: String,
  val url: String,
  val wiki: Wiki
)

@Creator(
  generateResponse = true,
  responseClassName = "GetImageResponse",
  generateRetrofitService = true,
  retrofitServiceClassName = "AlbumRetrofitService",
  method = requestMethodPost,
  url = "./?method=image.getInfo",
  methodName = "getImageAsync",
  returnResponseClassName = "GetImageResponse",
  parameters = [
    Parameter(paramName = "phrase", paramType = "String", paramQueryType = "Field")
  ]
)
data class Image(
  val size: String,
  val text: String
)

@Creator
data class Tags(
  val tag: List<Tag>
)

@Creator
data class Tag(
  val url: String,
  val name: String
)

@Creator
data class Tracks(
  val track: List<Track>
)

@Creator
data class Track(
  val streamable: Streamable,
  val duration: Long,
  val url: String,
  val name: String,
  val attr: Attr,
  val artist: Artist
)

@Creator
data class Artist(
  val url: String,
  val name: String,
  val mbid: String
)

@Creator
data class Attr(
  val rank: Long
)

@Creator
data class Streamable(
  val fulltrack: String,
  val text: String
)

@Creator
data class Wiki(
  val published: String,
  val summary: String,
  val content: String
)
