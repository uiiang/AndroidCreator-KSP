// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json      = Json { allowStructuredMapKeys = true }
// val eventList = json.parse(EventList.serializer(), jsonString)

package uii.ang.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uii.ang.base.data.ktor.BaseResponseObj
import uii.ang.creator.annotation.*

@Creator(
  generateApiType = apiTypeKtor,
  generateApiService = true,
  url = "/event1905/event_list",
  method = requestMethodGet,
  methodName = "getEventList",
  getCallFailureFuncPath = "getCallFailure",
  checkResponseSuccessFuncPath = "checkResponseSuccess",
  isDynamicBaseUrl = false,
  isSupportPage = true,
  pageParamName = "page",
  pageSize = 30,
  prefetchDistance = 2,
  parameters = [
    Parameter(
      paramName = "usercode",
      paramType = "String",
      paramQueryType = requestParamTypeQuery,
    ),
    Parameter(
      paramName = "access_token",
      paramType = "String",
      paramQueryType = requestParamTypeQuery,
    )
  ]
)
@Serializable
data class EventList(
  @SerialName("data")
  val data: Data? = null,
) : BaseResponseObj()

@Serializable
data class Data(
  val pagenum: Long? = null,
  val page: Long? = null,
  val pagesize: Long? = null,
  val count: String? = null,
  @ParseReturn
  val list: List<ListElement>? = null,
  val isbtm: Long? = null
)

@Serializable
data class ListElement(
  @SerialName("event_id")
  val eventID: String? = null,

  val title: String? = null,
  val thumb: String? = null,

  @SerialName("thumb_flack")
  val thumbFlack: String? = null,

  @SerialName("start_date")
  val startDate: String? = null,

  @SerialName("end_date")
  val endDate: String? = null,

  /**
   * 1未报名， 2已报名审核中，3已结束 4报名成功 5未中奖 7，已结束未中奖？，8报名已取消
   */
  val status: Long? = null,
  val address: String? = null,
  val needlogin: Long? = null,

  @SerialName("tag_list")
  val tagList: List<String>? = null,

  @SerialName("event_info_url")
  val eventInfoURL: String? = null
)
