// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json           = Json { allowStructuredMapKeys = true }
// val visitorExactly = json.parse(VisitorExactly.serializer(), jsonString)

package uii.ang.domain

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import uii.ang.creator.annotation.Creator
import uii.ang.creator.annotation.Parameter
import uii.ang.creator.annotation.ParseReturn
import uii.ang.creator.annotation.ParseRoot
import uii.ang.creator.annotation.apiTypeKtor
import uii.ang.creator.annotation.requestMethodPost
import uii.ang.creator.annotation.requestParamTypeBody

@Creator(
    generateApiType = apiTypeKtor,
    generateApiService = true,
    url = "/s_api/GetVisitorExactly",
    method = requestMethodPost,
    methodName = "getVisitorExactly",
    parameters = [
        Parameter(
            paramName = "msgType",
            paramType = "String",
            paramQueryType = requestParamTypeBody,
            paramDefault = "GetVisitorExactly"
        ),
        Parameter(
            paramName = "Vphone",
            paramType = "String",
            paramQueryType = requestParamTypeBody,
        ),
        Parameter(
            paramName = "Vcountrycode",
            paramType = "String",
            paramQueryType = requestParamTypeBody,
        ),
    ]
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