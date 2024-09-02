package uii.ang.domain.request

import kotlin.Int
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
public data class PreRegisterRequestBody_user(
  @SerialName("uName")
  public val uName: String?,
  @SerialName("uTypeID")
  public val uTypeID: String?,
)

@Serializable
public data class PreRegisterRequestBodydate(
  @SerialName("date")
  public val date: String?,
)

@Serializable
public data class PreRegisterRequestBody(
  @SerialName("msgType")
  public val msgType: String = "PreRegister",
  @SerialName("ApplyTimeUTC")
  public val ApplyTimeUTC: String?,
  @SerialName("ApplyTimeZone")
  public val ApplyTimeZone: Int = 0,
  @SerialName("Ecountrycode")
  public val Ecountrycode: String?,
  @SerialName("Edept")
  public val Edept: String?,
  @SerialName("Ename")
  public val Ename: String?,
  @SerialName("Eemail")
  public val Eemail: String?,
  @SerialName("Ephone")
  public val Ephone: String?,
  @SerialName("Vacccount")
  public val Vacccount: Int?,
  @SerialName("Vbranch")
  public val Vbranch: String?,
  @SerialName("Vcountrycode")
  public val Vcountrycode: String?,
  @SerialName("Vid")
  public val Vid: String?,
  @SerialName("Vidtype")
  public val Vidtype: Int?,
  @SerialName("Vname")
  public val Vname: String?,
  @SerialName("Vphone")
  public val Vphone: String?,
  @SerialName("Vpurpose")
  public val Vpurpose: String?,
  @SerialName("Vtype")
  public val Vtype: Int?,
  @SerialName("Vunit")
  public val Vunit: String?,
  @SerialName("language")
  public val language: String?,
  @SerialName("channel")
  public val channel: String = "4",
  @SerialName("Vplate")
  public val Vplate: String?,
  @SerialName("VReserved")
  public val VReserved: String?,
  @SerialName("date")
  public val date: PreRegisterRequestBodydate?,
  @Transient
  private val uName: String?,
  @Transient
  private val uTypeID: String?,
) {
  @SerialName("_user")
  public var _user: PreRegisterRequestBody_user = PreRegisterRequestBody_user(uName, uTypeID)
}
