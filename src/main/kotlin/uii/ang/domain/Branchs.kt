package uii.ang.domain


import kotlinx.serialization.*
import uii.ang.creator.annotation.*

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
@Serializable
data class Branchs(
  @SerialName("RequestID")
  val requestID: String,

  @ParseRoot
  @ParseReturn
  @SerialName("branch")
  val branch: List<Branch>,
  @SerialName("error")
  val error: Long
)

@Creator(
  generateApiModel = true,
  generatorEntityModel = true,
)
@Serializable
data class Branch(
  @SerialName("AdminEmails")
  val adminEmails: String? = null,

  @SerialName("EmailGroupID")
  val emailGroupID: Long,

  @SerialName("EmailInstanceID")
  val emailInstanceID: Long,

  @SerialName("Floors")
  val floors: List<Floor>? = null,

  @SerialName("MailSuffixs")
  val mailSuffixs: List<MailSuffix>,

  @SerialName("PPTypeCode")
  val ppTypeCode: String? = null,

  @SerialName("SMSGroupID")
  val smsGroupID: Long,

  @SerialName("SMSInstanceID")
  val smsInstanceID: Long,

  @SerialName("Status")
  val status: Long,

  @SerialName("Website")
  val website: String,

  @SerialName("addr_cn")
  val addrCN: String? = null,

  @SerialName("addr_en")
  val addrEn: String? = null,

  val cn: String,
  val code: String,
  val en: String,
  val enableEmail: Long,
  val enableSMS: Long,
  val lat: Double,
  val lng: Double,
  val nation: BranchNation? = null,
  val show: Long,

  @SerialName("TimeZone")
  val timeZone: TimeZone? = null,

  @SerialName("DefaultHost")
  val defaultHost: DefaultHost? = null
)


@Creator(
  generateApiModel = true,
  generatorEntityModel = true,
)
@Serializable
data class DefaultHost(
  @SerialName("EHID")
  val ehid: Long,

  @SerialName("Eemail")
  val eemail: String,

  @SerialName("Ephone")
  val ephone: String,

  @SerialName("Name")
  val name: String
)


@Creator(
  generateApiModel = true,
  generatorEntityModel = true,
)
@Serializable
data class Floor(
  @SerialName("BranchCode")
  val branchCode: String,

  @SerialName("CFID")
  val cfid: Long,

  @SerialName("Description")
  val description: String,

  @SerialName("FloorAddUTime")
  val floorAddUTime: String,

  @SerialName("FloorCode")
  val floorCode: String,

  @SerialName("FloorName")
  val floorName: String,

  @SerialName("FloorUnit")
  val floorUnit: String
)


@Creator(
  generateApiModel = true,
  generatorEntityModel = true,
)
@Serializable
data class MailSuffix(
  @SerialName("AddUTime")
  val addUTime: String,

  @SerialName("BranchCode")
  val branchCode: String,

  @SerialName("Description")
  val description: String,

  @SerialName("MSID")
  val msid: Long,

  @SerialName("MailSuffix")
  val mailSuffix: String,

  @SerialName("Status")
  val status: Long
)


@Creator(
  generateApiModel = true,
  generatorEntityModel = true,
)
@Serializable
data class BranchNation(
  val cn: String,
  val code: String,
  val code2: String,
  val countrycode: String,
  val en: String,
  val favourite: Long
)


@Creator(
  generateApiModel = true,
  generatorEntityModel = true,
)
@Serializable
data class TimeZone(
  @SerialName("TimeZoneDes")
  val timeZoneDES: String,

  @SerialName("Value")
  val value: Long
)
