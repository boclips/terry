package com.boclips.terry.infrastructure.outgoing.policy

class FakePolicyRepository : PolicyRepository {
    val policies = mutableListOf<String>()

    override fun create(storageName: String): String? {
        val arn = getPolicyArn(storageName)
        policies.add(arn)
        return arn
    }

    override fun delete(policyName: String): Boolean =
        policies.remove(getPolicyArn(policyName))


    private fun getPolicyArn(storageName: String) = "arn:aws:::s3:${storageName}"

    fun existsFor(channelStorageName: String) = policies.contains(getPolicyArn(channelStorageName))
}
