// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json        = Json { allowStructuredMapKeys = true }
// val preRegister = json.parse(PreRegister.serializer(), jsonString)

package uii.ang.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uii.ang.creator.annotation.Creator
import uii.ang.creator.annotation.ParseReturn
import uii.ang.creator.annotation.apiTypeKtor
import uii.ang.creator.annotation.requestMethodPost
import uii.ang.base.data.ktor.BaseResponseObj

@Serializable

@Creator(
    generateApiType = apiTypeKtor,
    generateApiService = true,
//    generateApiModel = true,
//    generateResponse = true,
////    responseClassName = "Nationality",
//    generateRetrofitService = true,
//    retrofitServiceClassName = "Nationality",
    url = "/s_api/PreRegister",
    method = requestMethodPost,
    methodName = "preRegister",
    getCallFailureFuncPath = "getCallFailure",
    checkResponseSuccessFuncPath = "checkResponseSuccess",
    isDynamicBaseUrl = true,
)
@ParseReturn
data class PreRegister (
    @SerialName("EH_ID")
    val ehID: Long? = null,

    @SerialName("VV_ID")
    val vvID: Long? = null,

    @SerialName("Vcount")
    val vcount: Long? = null,

    val regcode: String? = null,
    val time: String? = null
) : BaseResponseObj()
