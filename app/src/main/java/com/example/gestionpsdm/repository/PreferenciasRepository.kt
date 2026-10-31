package com.example.gestionpsdm.repository

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.edit

private val Context.zarorDataStore by preferencesDataStore(
    name = "configuracion"
)

class PreferenciasRepository(
    context: Context
) {
    private val contexto = context.applicationContext
   private val claveSoloHabilitados = booleanPreferencesKey(
        "solo_espacios_habilitados"
    )
   val soloHabilitados: Flow<Boolean> =
        contexto.zarorDataStore.data
            .map { preferencias ->
                preferencias[claveSoloHabilitados] ?: false
            }

    suspend fun guardarSoloHabilitados(valor: Boolean){

        contexto.zarorDataStore.edit { preferencias ->
            preferencias[claveSoloHabilitados] = valor
        }
    }
}