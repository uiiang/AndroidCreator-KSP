// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json            = Json { allowStructuredMapKeys = true }
// val printerTemplate = json.parse(PrinterTemplate.serializer(), jsonString)

package uii.ang.domain

import uii.ang.creator.annotation.*

//@Serializable
@Creator(
  generateApiType = apiTypeKtor,
  generateApiService = true,
  generateApiModel = true,
  generateResponse = true,
  generateRetrofitService = true,
  url = "/s_api/GetStorage",
  method = requestMethodPost,
  methodName = "getPrinterTemplate",
  parameters = [
    Parameter(
      paramName = "msgType",
      paramType = "String",
      paramQueryType = requestParamTypeBody,
      paramDefault = "GetStorage"
    ),
    Parameter(
      paramName = "Type",
      paramType = "Int",
      paramQueryType = requestParamTypeBody,
      paramDefault = "22"
    ),
  ]
)
data class PrinterTemplate(
  val requestID: String? = null,

  @ParseRoot
  @ParseReturn
  val Storges: List<Storges>? = null,

  val error: Long? = null
)

@Creator(
  generateApiType = apiTypeKtor,
  generateApiModel = true,
  generatorEntityModel = true,
)
data class Storges(
  val imageData: String? = null,

  val key: String? = null,

  val storageID: Long? = null,

  val type: Long? = null,

  val value: String? = null
)
