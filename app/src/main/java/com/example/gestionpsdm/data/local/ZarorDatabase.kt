
package com.example.gestionpsdm.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        GastoEntity::class,
        IngresoEntity::class,
        EspacioEntity::class,
        ReservaEntity::class,
        PagoEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class ZarorDatabase : RoomDatabase() {

    abstract fun gastoDao(): GastoDao

    abstract fun ingresoDao(): IngresoDao

    abstract fun espacioDao(): EspacioDao

    abstract fun reservaDao(): ReservaDao

    abstract fun pagoDao(): PagoDao

    companion object {

        @Volatile
        private var INSTANCE: ZarorDatabase? = null

        fun obtenerDatabase(context: Context): ZarorDatabase {

            val instanciaActual = INSTANCE

            if (instanciaActual != null) {
                return instanciaActual
            }

            return synchronized(this) {

                val instanciaExistente = INSTANCE

                if (instanciaExistente != null) {
                    instanciaExistente
                } else {

                    val database = Room.databaseBuilder(
                        context.applicationContext,
                        ZarorDatabase::class.java,
                        "zaror_database"
                    )
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                        .addCallback(
                            object : RoomDatabase.Callback() {

                                override fun onCreate(
                                    db: SupportSQLiteDatabase
                                ) {
                                    super.onCreate(db)

                                    // Gastos ficticios iniciales
                                    db.execSQL(
                                        """
                                        INSERT INTO gastos
                                        (descripcion, monto, fecha, categoria)
                                        VALUES
                                        ('Pago de electricidad', 40000, '01/11/2026', 'SERVICIOS_BASICOS'),
                                        ('Productos de limpieza', 25000, '02/11/2026', 'LIMPIEZA'),
                                        ('Compra de insumos', 20000, '03/11/2026', 'INSUMOS'),
                                        ('Reparación de instalaciones', 30000, '04/11/2026', 'MANTENIMIENTO'),
                                        ('Mejoras del quincho', 35000, '05/11/2026', 'MEJORAS'),
                                        ('Gastos administrativos', 10000, '06/11/2026', 'OTROS')
                                        """.trimIndent()
                                    )

                                    // Ingresos ficticios iniciales
                                    db.execSQL(
                                        """
                                        INSERT INTO ingresos
                                        (descripcion, monto, fecha)
                                        VALUES
                                        ('Arriendo de quincho', 160000, '02/11/2026'),
                                        ('Arriendo de piscina', 100000, '04/11/2026'),
                                        ('Otros arriendos', 40000, '06/11/2026')
                                        """.trimIndent()
                                    )
                                    //insertar espacios iniciales
                                    db.execSQL("INSERT INTO espacios(id, nombre, capacidadMaxima, habilitado) VALUES (1, 'Quincho', 25, 1), (2, 'Sala multiuso', 40, 0), (3, 'Piscina', 60, 1)")
                                }
                            }
                        )
                        .build()

                    INSTANCE = database
                    database
                }
            }
        }
    }
}
