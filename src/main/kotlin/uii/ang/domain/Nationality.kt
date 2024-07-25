//package uii.ang.domain
//
//import uii.ang.creator.annotation.*
//
//@Creator(
//  generateApiModel = true,
//  generateResponse = true,
////    responseClassName = "Nationality",
//  generateRetrofitService = true,
////    retrofitServiceClassName = "Nationality",
//  url = "/s_api/GetNationality",
//  method = requestMethodPost,
//  methodName = "getNationality",
//)
//data class Nationality(
//  val requestID: String,
//  val error: Long,
//  @ParseRoot
//  @ParseReturn
////  @ToDatabase
//  val nation: List<Nation>
//)
//
//@Creator(
//  generateApiModel = true,
//  generatorEntityModel = true,
//)
//data class Nation(
//  val cn: String,
//  @Query(queryMethodName = "queryNationByCode", queryType = queryTypeEquals)
//  val code: String,
//  @Query(queryMethodName = "queryNationByCode", queryType = queryTypeEquals)
//  val code2: String,
//  val countrycode: String? = null,
//  val en: String,
//  @Query(queryMethodName = "queryNationByFavourite", queryType = queryTypeEquals)
//  @Query(queryMethodName = "queryNationByCode", queryType = queryTypeEquals)
//  val favourite: Long? = null
//)
