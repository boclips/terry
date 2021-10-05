package com.boclips.terry.infrastructure.outgoing.policy

class FakePolicyRepository : PolicyRepository {
    private val policies = mutableListOf<String>()

    override fun createOrGet(storageName: String): String? {
        val arn = getPolicyArn(storageName)
        policies.add(arn)
        return arn
    }

    override fun delete(policyArn: String): Boolean =
        policies.remove(policyArn)


    private fun getPolicyArn(storageName: String) = "arn:aws:iam::${storageName}"

    fun existsFor(channelStorageName: String) = policies.contains(getPolicyArn(channelStorageName))
}
