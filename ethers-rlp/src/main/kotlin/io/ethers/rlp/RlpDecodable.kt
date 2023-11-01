package io.ethers.rlp

interface RlpDecodable<T> {

    /**
     * Decode [data] and return instance of [T].
     */
    fun rlpDecode(data: ByteArray) = rlpDecode(RlpDecoder(data))

    /**
     * Decode [rlp] into an instance of [T], or null if decoding fails.
     */
    fun rlpDecode(rlp: RlpDecoder): T?
}
