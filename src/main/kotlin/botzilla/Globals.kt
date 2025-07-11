package org.pois_noir.botzilla

// Header
internal const val HEADER_LENGTH: Int = 6    // JVM is fucking retarded, and it uses Int for its array size
internal const val STATUS_CODE_INDEX: Int = 0
internal const val OPERATION_CODE_INDEX: Int = 1
internal const val MESSAGE_LENGTH_INDEX: Int = 2

// Hash
internal const val HASH_LENGTH: Int = 32

// Component Operation Codes
internal const val USER_MESSAGE_OPERATION_CODE: UByte = 0u

// Server Codes
internal const val REGISTER_COMPONENT_OPERATION_CODE: UByte = 255u
internal const val GET_COMPONENT_OPERATION_CODE: UByte = 254u
internal const val GET_COMPONENTS_OPERATION_CODE: UByte = 255u

// Status Codes
internal const val OK_STATUS: UByte = 0u