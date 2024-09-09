// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json           = Json { allowStructuredMapKeys = true }
// val visitorExactly = json.parse(VisitorExactly.serializer(), jsonString)

package uii.ang.domain

import kotlinx.serialization.*
import uii.ang.creator.annotation.*

@Creator(
    generateApiType = apiTypeKtor,
    generateApiService = true,
    url = "/s_api/GetVisitorExactly",
    method = requestMethodGet,
    methodName = "getVisitorExactly",
    parameters = [
        Parameter(
            paramName = "msgType",
            paramType = "String",
            paramQueryType = requestParamTypePath,
            paramDefault = "GetVisitorExactly"
        ),
        Parameter(
            paramName = "Vphone",
            paramType = "String",
            paramQueryType = requestParamTypePath,
        ),
        Parameter(
            paramName = "Vcountrycode",
            paramType = "String",
            paramQueryType = requestParamTypeQuery,
        ),
        Parameter(
            paramName = "uName",
            paramType = "String",
            paramQueryType = "BaseRequestBodyUser",
        ),
        Parameter(
            paramName = "uTypeID",
            paramType = "String",
            paramQueryType = "BaseRequestBodyUser",
        ),
    ],
    getCallFailureFuncPath = "getCallFailure",
    checkResponseSuccessFuncPath = "checkResponseSuccess"
    )
//@ParseRoot
@ParseReturn
@Serializable
data class VisitorExactly (
    @SerialName("RequestID")
    val requestID: String? = null,

    @SerialName("VLastChangedUTC")
    val vLastChangedUTC: String? = null,

    @SerialName("VVID")
    val vvid: Long? = null,

    @SerialName("Vcountrycode")
    val vcountrycode: String? = null,

    @SerialName("Vidtype")
    val vidtype: Long? = null,

    @SerialName("Vlastid")
    val vlastid: Long? = null,

    @SerialName("Vlastname")
    val vlastname: String? = null,

    @SerialName("Vlasttime")
    val vlasttime: String? = null,

    @SerialName("VlasttimeUTC")
    val vlasttimeUTC: String? = null,

    @SerialName("VlasttimeZone")
    val vlasttimeZone: Long? = null,

    @SerialName("Vname")
    val vname: String? = null,

    @SerialName("Vphone")
    val vphone: String? = null,

    @SerialName("Vtype")
    val vtype: Long? = null,

    @SerialName("Vunit")
    val vunit: String? = null,

    val error: Long? = null
)
