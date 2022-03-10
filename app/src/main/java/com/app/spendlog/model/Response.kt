package com.app.spendlog.model

data class Response(
	val jsonMember10: List<JsonMember10Item?>? = null
)

data class JsonMember10Item(
	val date: String? = null,
	val amount: String? = null,
	val time: String? = null,
	val spendType: String? = null
)

