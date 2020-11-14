package com.wowlet.entities.responce

import com.squareup.moshi.Json

data class ResultSuccess(
    @field:Json(name = "context")
    val context: ResultContext,
    @field:Json(name = "value")
    val value: Int,
)