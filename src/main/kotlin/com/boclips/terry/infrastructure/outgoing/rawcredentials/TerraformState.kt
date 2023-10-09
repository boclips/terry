package com.boclips.terry.infrastructure.outgoing.rawcredentials

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonIgnoreProperties(ignoreUnknown = true)
data class TerraformState(
    val resources: List<TerraformResource>
)

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    visible = true,
    defaultImpl = IgnoredResource::class
)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = AwsIamAccessKey::class,
        name = "aws_iam_access_key"
    )
)
@JsonIgnoreProperties(ignoreUnknown = true)
sealed class TerraformResource()

object IgnoredResource : TerraformResource()

data class AwsIamAccessKey(
    val instances: List<AwsIamAccessKeyInstance>
) : TerraformResource()

@JsonIgnoreProperties(ignoreUnknown = true)
data class AwsIamAccessKeyInstance(
    val attributes: AwsIamAccessKeyAttributes
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AwsIamAccessKeyAttributes(
    val id: String,
    val secret: String,
    val user: String
)
