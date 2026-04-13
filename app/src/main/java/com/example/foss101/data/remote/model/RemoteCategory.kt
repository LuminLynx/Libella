package com.example.foss101.data.remote.model

import com.example.foss101.model.Category

data class RemoteCategory(
    val id: String,
    val name: String,
    val description: String
)

fun RemoteCategory.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        description = description
    )
}
