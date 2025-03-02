package com.icm2510.listitem.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class UsersList (
    val results: List<User> // Esta lista hace match con el JSON que devuelve la API
)