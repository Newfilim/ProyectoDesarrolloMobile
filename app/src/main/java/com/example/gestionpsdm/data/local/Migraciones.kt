
package com.example.gestionpsdm.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS espacios(id INTEGER NOT NULL PRIMARY KEY, nombre TEXT NOT NULL, capacidadMaxima INTEGER NOT NULL, habilitado INTEGER NOT NULL)")

        db.execSQL("INSERT INTO espacios(id, nombre, capacidadMaxima, habilitado) VALUES (1, 'Quincho', 25, 1), (2, 'Sala multiuso', 40, 0), (3, 'Piscina', 60, 1)")

    }
}


val MIGRATION_2_3 = object : Migration(2, 3) {

    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS reservas (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                idUsuario INTEGER NOT NULL,
                idEspacio INTEGER NOT NULL,
                fecha TEXT NOT NULL,
                horaInicio TEXT NOT NULL,
                horaFin TEXT NOT NULL,
                estado TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

}
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS pagos (
                idReserva INTEGER NOT NULL PRIMARY KEY,
                estado TEXT NOT NULL,
                comprobante TEXT
            )""".trimIndent()
        )
    }
}

