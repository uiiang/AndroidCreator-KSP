package uii.ang.domain

import uii.ang.creator.annotation.Creator
import uii.ang.creator.annotation.ParseRoot
import uii.ang.creator.annotation.requestMethodPost

@Creator(
    generateApiModel = true,
    generateResponse = true,
//    responseClassName = "Nationality",
    generateRetrofitService = true,
//    retrofitServiceClassName = "Nationality",
    url = "/s_api/GetNationality",
    method = requestMethodPost,
    methodName = "getNationality",
)
data class Nationality (
    val requestID: String,
    val error: Long,
    @ParseRoot
    val nation: List<Nation>
)
@Creator(generateApiModel = true)
data class Nation (
    val cn: String,
    val code: String,
    val code2: String,
    val countrycode: String? = null,
    val en: String,
    val favourite: Long? = null
)
