package uii.ang.domain


import uii.ang.creator.annotation.Creator
import uii.ang.creator.annotation.Parameter
import uii.ang.creator.annotation.ParseReturn
import uii.ang.creator.annotation.ParseRoot
import uii.ang.creator.annotation.requestMethodPost

@Creator(
  generateApiModel = true,
  generateResponse = true,
  generateRetrofitService = true,
  url = "/s_api/GetBranch",
  method = requestMethodPost,
  methodName = "getBranch",
  parameters = [
    Parameter(
      paramName = "msgType",
      paramType = "String",
      paramQueryType = "Map",
      paramDefault = "GetBranch"
    ),
  ]
)
data class Branchs (
  val requestID: String,
  @ParseRoot
  @ParseReturn
  val branch: List<Branch>,
  val error: Long
)


@Creator(generateApiModel = true,
  generatorEntityModel = true,)
data class Branch (
  val adminEmails: String? = null,
  val emailGroupID: Long,
  val emailInstanceID: Long,
  val floors: List<Floor>? = null,
  val mailSuffixs: List<MailSuffix>,
  val ppTypeCode: String? = null,
  val smsGroupID: Long,
  val smsInstanceID: Long,
  val status: Long,
  val website: String,
  val addrCN: String? = null,
  val addrEn: String? = null,
  val cn: String,
  val code: String,
  val en: String,
  val enableEmail: Long,
  val enableSMS: Long,
  val lat: Long,
  val lng: Long,
  val nation: BranchNation? = null,
  val show: Long,
  val timeZone: TimeZone? = null,
  val defaultHost: DefaultHost? = null
)

@Creator(generateApiModel = true,
  generatorEntityModel = true,)
data class DefaultHost (
  val ehid: Long,
  val eemail: String,
  val ephone: String,
  val name: String
)

@Creator(generateApiModel = true,
  generatorEntityModel = true,)
data class Floor (
  val branchCode: String,
  val cfid: Long,
  val description: String,
  val floorAddUTime: String,
  val floorCode: String,
  val floorName: String,
  val floorUnit: String
)

@Creator(generateApiModel = true,
  generatorEntityModel = true,)
data class MailSuffix (
  val addUTime: String,
  val branchCode: String,
  val description: String,
  val msid: Long,
  val mailSuffix: String,
  val status: Long
)

@Creator(generateApiModel = true,
  generatorEntityModel = true,)
data class BranchNation (
  val cn: String,
  val code: String,
  val code2: String,
  val countrycode: String,
  val en: String,
  val favourite: Long
)

@Creator(generateApiModel = true,
  generatorEntityModel = true,)
data class TimeZone (
  val timeZoneDES: String,
  val value: Long
)
