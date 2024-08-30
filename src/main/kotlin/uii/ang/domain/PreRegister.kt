// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json        = Json { allowStructuredMapKeys = true }
// val preRegister = json.parse(PreRegister.serializer(), jsonString)

package uii.ang.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uii.ang.creator.annotation.Creator
import uii.ang.creator.annotation.Parameter
import uii.ang.creator.annotation.ParseReturn
import uii.ang.creator.annotation.apiTypeKtor
import uii.ang.creator.annotation.requestMethodPost
import uii.ang.creator.annotation.requestParamPostObjTypeArray
import uii.ang.creator.annotation.requestParamPostObjTypeObject
import uii.ang.creator.annotation.requestParamTypeBody
import uii.ang.ivisitor.base.data.ktor.BaseResponseObj

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
    parameters = [
        Parameter(
            paramName = "msgType",
            paramType = "String",
            paramQueryType = requestParamTypeBody,
            paramDefault = "PreRegister"
        ),
        Parameter(
            paramName = "ApplyTimeUTC",
            paramType = "String",
            paramQueryType = requestParamTypeBody,
            paramDefault = ""
        ),
        Parameter(
            paramName = "ApplyTimeZone",
            paramType = "Int",
            paramQueryType = requestParamTypeBody,
            paramDefault = "0"
        ),
        Parameter(
            paramName = "Ecountrycode",
            paramType = "String",
            paramQueryType = requestParamTypeBody,
            paramDefault = ""
        ),
        Parameter(
            paramName = "Edept",
            paramType = "String",
            paramQueryType = requestParamTypeBody,
            paramDefault = ""
        ),
        Parameter(
            paramName = "Ename",
            paramType = "String",
            paramQueryType = requestParamTypeBody,
            paramDefault = ""
        ),
        Parameter(
            paramName = "Eemail",
            paramType = "String",
            paramQueryType = requestParamTypeBody,
            paramDefault = ""
        ),
        Parameter(
            paramName = "Ephone",
            paramType = "String",
            paramQueryType = requestParamTypeBody,
            paramDefault = ""
        ),
        Parameter(
            paramName = "Vacccount",
            paramType = "Int",
            paramQueryType = requestParamTypeBody,
        ),
        Parameter(
            paramName = "Vbranch",
            paramType = "String",
            paramQueryType = requestParamTypeBody,
            paramDefault = ""
        ),
        Parameter(
            paramName = "Vcountrycode",
            paramType = "String",
            paramQueryType = requestParamTypeBody,
            paramDefault = ""
        ),
        Parameter(
            paramName = "Vid",
            paramType = "String",
            paramQueryType = requestParamTypeBody,
            paramDefault = ""
        ),
        Parameter(
            paramName = "Vidtype",
            paramType = "Int",
            paramQueryType = requestParamTypeBody,
        ),
        Parameter(
            paramName = "Vname",
            paramType = "String",
            paramQueryType = requestParamTypeBody,
            paramDefault = ""
        ),
        Parameter(
            paramName = "Vphone",
            paramType = "String",
            paramQueryType = requestParamTypeBody,
            paramDefault = ""
        ),
        Parameter(
            paramName = "Vpurpose",
            paramType = "String",
            paramQueryType = requestParamTypeBody,
            paramDefault = ""
        ),
        Parameter(
            paramName = "Vtype",
            paramType = "Int",
            paramQueryType = requestParamTypeBody,
            paramDefault = ""
        ),
        Parameter(
            paramName = "Vunit",
            paramType = "String",
            paramQueryType = requestParamTypeBody,
            paramDefault = ""
        ),
        Parameter(
            paramName = "language",
            paramType = "String",
            paramQueryType = requestParamTypeBody,
            paramDefault = ""
        ),
        Parameter(
            paramName = "channel",
            paramType = "String",
            paramQueryType = requestParamTypeBody,
            paramDefault = "4"
        ),
        //    访客信息里面有车牌，但是有可能此次没开车。
        //    那只需要在PreRegister消息里不赋值VPlate,
        //    如果访客此次开车了，则需赋值VPlate
        //    如果改了车牌号，则需先调用AddVisitor去更新掉访客属性里面的Vplate
        Parameter(
            paramName = "Vplate",
            paramType = "String",
            paramQueryType = requestParamTypeBody,
        ),
        Parameter(
            paramName = "VReserved",
            paramType = "String",
            paramQueryType = requestParamTypeBody,
        ),
        Parameter(
            paramName = "uName",
            paramType = "String",
            paramQueryType = requestParamTypeBody,
            paramPostObjName = "_user",
            paramPostObjType = requestParamPostObjTypeObject
        ),
        Parameter(
            paramName = "uTypeID",
            paramType = "String",
            paramQueryType = requestParamTypeBody,
            paramPostObjName = "_user",
            paramPostObjType = requestParamPostObjTypeObject
        ),
        Parameter(
            paramName = "_date",
            paramType = "String",
            paramQueryType = requestParamTypeBody,
            paramPostObjName = "date",
            paramPostObjType = requestParamPostObjTypeArray
        ),
    ],
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
