package com.example.gradues.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_NAME = "gradues.db"
        private const val DATABASE_VERSION = 2
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        crearTablasIndependientes(db)
        crearTablasDependientesDirectas(db)
        crearTablasTrabajoGraduacion(db)
        crearTablasSeguimiento(db)

        insertarDatosIniciales(db)
    }

    private fun crearTablasIndependientes(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS empresa (
                idEmpresa INTEGER PRIMARY KEY,
                nombreEmpresa TEXT,
                rubroEmpresa TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS grupo_tge (
                idGrupoTGE INTEGER PRIMARY KEY,
                nombreCurso TEXT,
                cicloAcademico TEXT,
                cupoMaximo INTEGER,
                fechaCreacion TEXT,
                fechaFinal TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS modalidad (
                idModalidad INTEGER PRIMARY KEY,
                tipoModalidad TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS modulo_menu (
                idModuloMenu INTEGER PRIMARY KEY,
                nombreModuloMenu TEXT,
                descripcionModuloMenu TEXT,
                estadoModuloMenu TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS rol (
                idRol INTEGER PRIMARY KEY,
                nombreRol TEXT,
                descripcionRol TEXT
            )
            """.trimIndent()
        )
    }

    private fun crearTablasDependientesDirectas(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS personero (
                idPersonero INTEGER PRIMARY KEY,
                idEmpresa INTEGER,
                nombrePersonero TEXT,
                cargoPersonero TEXT,
                FOREIGN KEY(idEmpresa) REFERENCES empresa(idEmpresa)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS opcion_menu (
                idOpcionMenu INTEGER PRIMARY KEY,
                idModuloMenu INTEGER,
                codigoOpcionMenu TEXT,
                nombreOpcionMenu TEXT,
                rutaPantalla TEXT,
                tipoOpcion TEXT,
                estadoOpcionMenu TEXT,
                FOREIGN KEY(idModuloMenu) REFERENCES modulo_menu(idModuloMenu)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS usuario (
                idUsuario INTEGER PRIMARY KEY,
                idRol INTEGER,
                nombreUsuario TEXT,
                contrasenia TEXT,
                primerNombreUsuario TEXT,
                segundoNombreUsuario TEXT,
                primerApellidoUsuario TEXT,
                segundoApellidoUsuario TEXT,
                correoUsuario TEXT,
                carnetUsuario TEXT,
                duiUsuario TEXT,
                carreraUsuario TEXT,
                estadoUsuario TEXT,
                FOREIGN KEY(idRol) REFERENCES rol(idRol)
            )
            """.trimIndent()
        )
    }

    private fun crearTablasTrabajoGraduacion(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS rol_opcion_menu (
                idRolOpcionMenu INTEGER PRIMARY KEY,
                idRol INTEGER,
                idOpcionMenu INTEGER,
                puedeVer INTEGER,
                puedeEditar INTEGER,
                puedeCrear INTEGER,
                puedeEliminar INTEGER,
                FOREIGN KEY(idRol) REFERENCES rol(idRol),
                FOREIGN KEY(idOpcionMenu) REFERENCES opcion_menu(idOpcionMenu)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS trabajo_graduacion (
                idTrabajoGraduacion INTEGER PRIMARY KEY,
                idModalidad INTEGER,
                idDocenteResponsable INTEGER,
                nombreTrabajo TEXT,
                cicloAcademico TEXT,
                estadoTrabajo TEXT,
                fechaInicioTrabajo TEXT,
                fechaFinalTrabajo TEXT,
                FOREIGN KEY(idModalidad) REFERENCES modalidad(idModalidad),
                FOREIGN KEY(idDocenteResponsable) REFERENCES usuario(idUsuario)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS solicitud_modalidad (
                idSolicitudModalidad INTEGER PRIMARY KEY,
                idUsuario INTEGER NOT NULL,
                idModalidad INTEGER NOT NULL,
                idTrabajoGraduacion INTEGER,
                idGrupoTGESolicitado INTEGER,
                idEmpresaSolicitada INTEGER,
                codigoAgrupacionSolicitud TEXT,
                nombreTrabajoPropuesto TEXT,
                fechaSolicitud TEXT NOT NULL,
                estadoSolicitud TEXT NOT NULL,
                observacionSolicitud TEXT,
                FOREIGN KEY(idUsuario) REFERENCES usuario(idUsuario),
                FOREIGN KEY(idModalidad) REFERENCES modalidad(idModalidad),
                FOREIGN KEY(idTrabajoGraduacion) REFERENCES trabajo_graduacion(idTrabajoGraduacion),
                FOREIGN KEY(idGrupoTGESolicitado) REFERENCES grupo_tge(idGrupoTGE),
                FOREIGN KEY(idEmpresaSolicitada) REFERENCES empresa(idEmpresa)
            )
            """.trimIndent()
                )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS alumno_trabajo (
                idAlumnoTrabajo INTEGER PRIMARY KEY,
                idUsuario INTEGER,
                idTrabajoGraduacion INTEGER,
                estadoAlumnoTrabajo TEXT,
                FOREIGN KEY(idUsuario) REFERENCES usuario(idUsuario),
                FOREIGN KEY(idTrabajoGraduacion) REFERENCES trabajo_graduacion(idTrabajoGraduacion)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS asignacion_jurado (
                idAsignacionJurado INTEGER PRIMARY KEY,
                idUsuario INTEGER,
                idTrabajoGraduacion INTEGER,
                fechaAsignacion TEXT,
                FOREIGN KEY(idUsuario) REFERENCES usuario(idUsuario),
                FOREIGN KEY(idTrabajoGraduacion) REFERENCES trabajo_graduacion(idTrabajoGraduacion)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS documento (
                idDocumento INTEGER PRIMARY KEY,
                idTrabajoGraduacion INTEGER,
                tipoDocumento TEXT,
                tituloDocumento TEXT,
                urlDocumento TEXT,
                estadoDocumento TEXT,
                observacionDocumento TEXT,
                versionDocumento INTEGER,
                fechaSubida TEXT,
                FOREIGN KEY(idTrabajoGraduacion) REFERENCES trabajo_graduacion(idTrabajoGraduacion)
            )
            """.trimIndent()
        )

        db.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS grupo_tgi (
                idGrupoTGI INTEGER PRIMARY KEY,
                idTrabajoGraduacion INTEGER,
                codigoGrupoTGI TEXT,
                fechaCreacion TEXT,
                fechaFinal TEXT,
                FOREIGN KEY(idTrabajoGraduacion) REFERENCES trabajo_graduacion(idTrabajoGraduacion)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS propuesta_perfil (
                idPropuesta INTEGER PRIMARY KEY,
                idTrabajoGraduacion INTEGER,
                tituloPropuesta TEXT,
                descripcionPropuesta TEXT,
                estadoPropuesta TEXT,
                observacionPropuesta TEXT,
                urlArchivo TEXT,
                fechaRegistro TEXT,
                FOREIGN KEY(idTrabajoGraduacion) REFERENCES trabajo_graduacion(idTrabajoGraduacion)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS subgrupo_tge (
                idSubgrupoTGE INTEGER PRIMARY KEY,
                idGrupoTGE INTEGER,
                idTrabajoGraduacion INTEGER,
                nombreSubgrupo TEXT,
                temaAsignado TEXT,
                FOREIGN KEY(idGrupoTGE) REFERENCES grupo_tge(idGrupoTGE),
                FOREIGN KEY(idTrabajoGraduacion) REFERENCES trabajo_graduacion(idTrabajoGraduacion)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS proyecto_pasantia (
                idProyectoPasantia INTEGER PRIMARY KEY,
                idTrabajoGraduacion INTEGER,
                idEmpresa INTEGER,
                fechaInicioPasantia TEXT,
                fechaFinalPasantia TEXT,
                estadoPasantia TEXT,
                FOREIGN KEY(idTrabajoGraduacion) REFERENCES trabajo_graduacion(idTrabajoGraduacion),
                FOREIGN KEY(idEmpresa) REFERENCES empresa(idEmpresa)
            )
            """.trimIndent()
        )
    }

    private fun crearTablasSeguimiento(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS bitacora (
                idBitacora INTEGER PRIMARY KEY,
                idProyectoPasantia INTEGER,
                fechaActividad TEXT,
                tituloActividad TEXT,
                descripcionActividad TEXT,
                totalHorasTrabajadas INTEGER,
                estadoBitacora TEXT,
                observacionBitacora TEXT,
                FOREIGN KEY(idProyectoPasantia) REFERENCES proyecto_pasantia(idProyectoPasantia)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS memoria_resumen (
                idMemoriaResumen INTEGER PRIMARY KEY,
                idProyectoPasantia INTEGER,
                contenidoResumen TEXT,
                urlDocumento TEXT,
                estadoMemoria TEXT,
                observacionMemoria TEXT,
                FOREIGN KEY(idProyectoPasantia) REFERENCES proyecto_pasantia(idProyectoPasantia)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS nota_etapa (
                idNotaEtapa INTEGER PRIMARY KEY,
                idAlumnoTrabajo INTEGER,
                numeroEtapa INTEGER,
                nota REAL,
                observacionNota TEXT,
                fechaRegistro TEXT,
                FOREIGN KEY(idAlumnoTrabajo) REFERENCES alumno_trabajo(idAlumnoTrabajo)
            )
            """.trimIndent()
        )
    }

    private fun insertarDatosIniciales(db: SQLiteDatabase) {
        insertarCatalogos(db)
        insertarUsuarios(db)
        insertarTrabajosGraduacion(db)
        insertarRelacionesAcademicas(db)
        insertarSeguimiento(db)
    }

    private fun insertarCatalogos(db: SQLiteDatabase) {
        db.execSQL("INSERT OR IGNORE INTO rol VALUES (1, 'Alumno', 'Alumno de la Universidad')")
        db.execSQL("INSERT OR IGNORE INTO rol VALUES (2, 'Docente', 'Docente de la Universidad')")
        db.execSQL("INSERT OR IGNORE INTO rol VALUES (3, 'Administrador', 'Usuario administrador del sistema')")
        db.execSQL("INSERT OR IGNORE INTO rol VALUES (4, 'Jurado', 'Miembro jurado evaluador')")

        db.execSQL("INSERT OR IGNORE INTO modalidad VALUES (1, 'Investigación')")
        db.execSQL("INSERT OR IGNORE INTO modalidad VALUES (2, 'Curso de especialización')")
        db.execSQL("INSERT OR IGNORE INTO modalidad VALUES (3, 'Pasantía profesional')")

        db.execSQL("INSERT OR IGNORE INTO empresa VALUES (1, 'Crowley Shared Services S.A.', 'Servicios')")
        db.execSQL("INSERT OR IGNORE INTO empresa VALUES (2, 'Pollo Campero', 'Restaurantes')")
        db.execSQL("INSERT OR IGNORE INTO empresa VALUES (3, 'Almacenes Siman', 'Tienda por departamentos')")

        db.execSQL("INSERT OR IGNORE INTO grupo_tge VALUES (1, 'Curso de Desarrollo Web', 'Ciclo I', 30, '2026-04-01', NULL)")
        db.execSQL("INSERT OR IGNORE INTO grupo_tge VALUES (2, 'Gestión de Proyectos Tecnológicos', 'Ciclo I', 30, '2026-04-01', NULL)")

        db.execSQL("INSERT OR IGNORE INTO modulo_menu VALUES (1, 'Alumno', 'Opciones disponibles para alumnos', 'Activo')")
        db.execSQL("INSERT OR IGNORE INTO modulo_menu VALUES (2, 'Docente', 'Opciones disponibles para docentes', 'Activo')")
        db.execSQL("INSERT OR IGNORE INTO modulo_menu VALUES (3, 'Administrador', 'Opciones disponibles para administradores', 'Activo')")

        db.execSQL("INSERT OR IGNORE INTO opcion_menu VALUES (1, 1, 'DASH_ALU', 'Dashboard alumno', 'DashboardAlumnoActivity', 'Pantalla', 'Activo')")
        db.execSQL("INSERT OR IGNORE INTO opcion_menu VALUES (2, 1, 'MI_GRUPO', 'Mi grupo', 'DetalleTrabajoAlumnoActivity', 'Pantalla', 'Activo')")
        db.execSQL("INSERT OR IGNORE INTO opcion_menu VALUES (3, 1, 'MIS_NOTAS', 'Mis notas', 'DetalleNotasAlumnoActivity', 'Pantalla', 'Activo')")
        db.execSQL("INSERT OR IGNORE INTO opcion_menu VALUES (4, 1, 'MIS_PROPUESTAS', 'Mis propuestas', 'DetallePropuestasAlumnoActivity', 'Pantalla', 'Activo')")
        db.execSQL("INSERT OR IGNORE INTO opcion_menu VALUES (5, 1, 'MIS_BITACORAS', 'Mis bitácoras', 'BitacorasAlumnoActivity', 'Pantalla', 'Activo')")

        db.execSQL("INSERT OR IGNORE INTO opcion_menu VALUES (6, 2, 'DASH_DOC', 'Dashboard docente', 'DashboardDocenteActivity', 'Pantalla', 'Activo')")
        db.execSQL("INSERT OR IGNORE INTO opcion_menu VALUES (7, 2, 'GRUP_INV', 'Grupos de investigación', 'GruposActivity', 'Pantalla', 'Activo')")
        db.execSQL("INSERT OR IGNORE INTO opcion_menu VALUES (8, 2, 'CUR_ESP', 'Cursos de especialización', 'CursosActivity', 'Pantalla', 'Activo')")
        db.execSQL("INSERT OR IGNORE INTO opcion_menu VALUES (9, 2, 'PAS_DOC', 'Proyectos de pasantía', 'ListadoGruposPasantiaActivity', 'Pantalla', 'Activo')")

        db.execSQL("INSERT OR IGNORE INTO rol_opcion_menu VALUES (1, 1, 1, 1, 0, 0, 0)")
        db.execSQL("INSERT OR IGNORE INTO rol_opcion_menu VALUES (2, 1, 2, 1, 0, 0, 0)")
        db.execSQL("INSERT OR IGNORE INTO rol_opcion_menu VALUES (3, 1, 3, 1, 0, 0, 0)")
        db.execSQL("INSERT OR IGNORE INTO rol_opcion_menu VALUES (4, 1, 4, 1, 0, 0, 0)")
        db.execSQL("INSERT OR IGNORE INTO rol_opcion_menu VALUES (5, 1, 5, 1, 0, 0, 0)")

        db.execSQL("INSERT OR IGNORE INTO rol_opcion_menu VALUES (6, 2, 6, 1, 1, 1, 1)")
        db.execSQL("INSERT OR IGNORE INTO rol_opcion_menu VALUES (7, 2, 7, 1, 1, 1, 1)")
        db.execSQL("INSERT OR IGNORE INTO rol_opcion_menu VALUES (8, 2, 8, 1, 1, 1, 1)")
        db.execSQL("INSERT OR IGNORE INTO rol_opcion_menu VALUES (9, 2, 9, 1, 1, 1, 1)")

        db.execSQL("INSERT OR IGNORE INTO rol_opcion_menu VALUES (10, 3, 6, 1, 1, 1, 1)")
        db.execSQL("INSERT OR IGNORE INTO rol_opcion_menu VALUES (11, 3, 7, 1, 1, 1, 1)")
        db.execSQL("INSERT OR IGNORE INTO rol_opcion_menu VALUES (12, 3, 8, 1, 1, 1, 1)")
        db.execSQL("INSERT OR IGNORE INTO rol_opcion_menu VALUES (13, 3, 9, 1, 1, 1, 1)")
    }

    private fun insertarUsuarios(db: SQLiteDatabase) {
        db.execSQL(
            """
            INSERT OR IGNORE INTO usuario VALUES (
                1, 2, 'Ing. Cesar Augusto', '1234',
                'Cesar', 'Augusto', 'González', 'Rodríguez',
                'cg24001@ues.edu.sv', 'CG24001', NULL,
                NULL, 'Activo'
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT OR IGNORE INTO usuario VALUES (
                2, 1, 'Eduardo Enrique López Rodríguez', '1234',
                'Eduardo', 'Enrique', 'López', 'Rodríguez',
                'lr21008@ues.edu.sv', 'LR21008', NULL,
                'Ingeniería de Sistemas Informáticos', 'Activo'
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT OR IGNORE INTO usuario VALUES (
                3, 1, 'Allan Augusto Anduray Portillo', '1234',
                'Allan', 'Augusto', 'Anduray', 'Portillo',
                'ap20025@ues.edu.sv', 'AP20025', NULL,
                'Ingeniería de Sistemas Informáticos', 'Activo'
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT OR IGNORE INTO usuario VALUES (
                4, 1, 'Oscar Mauricio Ángel Córdova', '1234',
                'Oscar', 'Mauricio', 'Ángel', 'Córdova',
                'ac21011@ues.edu.sv', 'AC21011', NULL,
                'Ingeniería de Sistemas Informáticos', 'Activo'
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT OR IGNORE INTO usuario VALUES (
                5, 1, 'Luisa Elizabeth Escobar Martínez', '1234',
                'Luisa', 'Elizabeth', 'Escobar', 'Martínez',
                'em22001@ues.edu.sv', 'EM22001', NULL,
                'Ingeniería de Sistemas Informáticos', 'Activo'
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT OR IGNORE INTO usuario VALUES (
                6, 1, 'Eduardo Javier Hernández Regalado', '1234',
                'Eduardo', 'Javier', 'Hernández', 'Regalado',
                'hr23040@ues.edu.sv', 'HR23040', NULL,
                'Ingeniería de Sistemas Informáticos', 'Activo'
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT OR IGNORE INTO usuario VALUES (
                7, 3, 'Administrador General', '1234',
                'Administrador', NULL, 'General', NULL,
                'admin001@ues.edu.sv', 'ADMIN001', NULL,
                NULL, 'Activo'
            )
            """.trimIndent()
        )
    }

    private fun insertarTrabajosGraduacion(db: SQLiteDatabase) {
        db.execSQL(
            """
            INSERT OR IGNORE INTO trabajo_graduacion VALUES (
                1, 1, 1,
                'Sistema de seguimiento académico',
                'Ciclo I',
                'Activo',
                '2026-04-01',
                NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT OR IGNORE INTO trabajo_graduacion VALUES (
                2, 2, 1,
                'Curso de Desarrollo Web',
                'Ciclo I',
                'Activo',
                '2026-04-01',
                NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT OR IGNORE INTO trabajo_graduacion VALUES (
                3, 3, 1,
                'Pasantía profesional en empresa colaboradora',
                'Ciclo I',
                'Activo',
                '2026-04-01',
                NULL
            )
            """.trimIndent()
        )
    }

    private fun insertarRelacionesAcademicas(db: SQLiteDatabase) {
        db.execSQL("INSERT OR IGNORE INTO alumno_trabajo VALUES (1, 2, 1, 'Activo')")
        db.execSQL("INSERT OR IGNORE INTO alumno_trabajo VALUES (2, 3, 1, 'Activo')")
        db.execSQL("INSERT OR IGNORE INTO alumno_trabajo VALUES (3, 4, 2, 'Activo')")
        db.execSQL("INSERT OR IGNORE INTO alumno_trabajo VALUES (4, 5, 2, 'Activo')")
        db.execSQL("INSERT OR IGNORE INTO alumno_trabajo VALUES (5, 6, 3, 'Activo')")

        db.execSQL(
            """
    INSERT OR IGNORE INTO solicitud_modalidad VALUES (
        1, 2, 1, 1,
        NULL, NULL,
        'TGI-01',
        'Sistema de seguimiento académico',
        '2026-05-21',
        'Aprobada',
        'Solicitud aprobada para modalidad de investigación'
    )
    """.trimIndent()
        )

        db.execSQL(
            """
    INSERT OR IGNORE INTO solicitud_modalidad VALUES (
        2, 4, 2, 2,
        1, NULL,
        'SUB-01',
        'Sistema de gestión de contenido para estudiantes',
        '2026-05-21',
        'Aprobada',
        'Solicitud aprobada para curso de especialización'
    )
    """.trimIndent()
        )

        db.execSQL(
            """
    INSERT OR IGNORE INTO solicitud_modalidad VALUES (
        3, 6, 3, 3,
        NULL, 1,
        'PAS-01',
        'Pasantía profesional en empresa colaboradora',
        '2026-05-21',
        'Pendiente',
        'Solicitud pendiente de revisión para pasantía'
    )
    """.trimIndent()
        )

        db.execSQL("INSERT OR IGNORE INTO grupo_tgi VALUES (1, 1, 'TGI-01', '2026-04-01', NULL)")
        db.execSQL("INSERT OR IGNORE INTO subgrupo_tge VALUES (1, 1, 2, 'Subgrupo 01', 'Sistema de gestión de contenido para estudiantes')")

        db.execSQL("INSERT OR IGNORE INTO personero VALUES (1, 1, 'Eliner Villafuerte', 'Supervisor')")
        db.execSQL("INSERT OR IGNORE INTO proyecto_pasantia VALUES (1, 3, 1, '2026-04-01', NULL, 'Activo')")

        db.execSQL("INSERT OR IGNORE INTO asignacion_jurado VALUES (1, 1, 1, '2026-05-23')")
    }

    private fun insertarSeguimiento(db: SQLiteDatabase) {
        db.execSQL("INSERT OR IGNORE INTO propuesta_perfil VALUES (1, 1, 'Sistema web para control de tutorías', 'Propuesta inicial del grupo', 'Descartada', 'Sin observaciones', NULL, '2026-04-10')")
        db.execSQL("INSERT OR IGNORE INTO propuesta_perfil VALUES (2, 1, 'Sistema de seguimiento académico', 'Propuesta seleccionada por el docente', 'Seleccionada', 'Sin observaciones', NULL, '2026-04-11')")
        db.execSQL("INSERT OR IGNORE INTO propuesta_perfil VALUES (3, 1, 'App móvil para evaluación de tesis', 'Propuesta con observación', 'Con observación', 'Revisar alcance', NULL, '2026-04-12')")

        db.execSQL("INSERT OR IGNORE INTO documento VALUES (1, 1, 'Capítulo I', 'Capítulo I - Sistema de seguimiento académico', NULL, 'En revisión', NULL, 1, '2026-05-10')")

        db.execSQL("INSERT OR IGNORE INTO memoria_resumen VALUES (1, 1, 'Memoria inicial de labores', NULL, 'Pendiente', NULL)")

        db.execSQL("INSERT OR IGNORE INTO bitacora VALUES (1, 1, '2026-04-10', 'Configuración inicial', 'Configuración del entorno de desarrollo', 4, 'Revisada', 'Sin observaciones')")
        db.execSQL("INSERT OR IGNORE INTO bitacora VALUES (2, 1, '2026-04-12', 'Reunión de seguimiento', 'Reunión con supervisor para definir funcionalidades', 3, 'Revisada', 'Sin observaciones')")
        db.execSQL("INSERT OR IGNORE INTO bitacora VALUES (3, 1, '2026-04-14', 'Desarrollo', 'Desarrollo de módulo de usuarios', 5, 'Pendiente', NULL)")

        db.execSQL("INSERT OR IGNORE INTO nota_etapa VALUES (1, 1, 1, 8.70, 'Buen avance', '2026-05-01')")
        db.execSQL("INSERT OR IGNORE INTO nota_etapa VALUES (2, 1, 2, 8.05, 'Revisar observaciones', '2026-05-15')")
        db.execSQL("INSERT OR IGNORE INTO nota_etapa VALUES (3, 1, 3, NULL, NULL, NULL)")
        db.execSQL("INSERT OR IGNORE INTO nota_etapa VALUES (4, 1, 4, NULL, NULL, NULL)")

        db.execSQL("INSERT OR IGNORE INTO nota_etapa VALUES (5, 3, 1, 8.50, 'Buen trabajo', '2026-05-01')")
        db.execSQL("INSERT OR IGNORE INTO nota_etapa VALUES (6, 3, 2, 9.00, 'Excelente avance', '2026-05-15')")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS nota_etapa")
        db.execSQL("DROP TABLE IF EXISTS memoria_resumen")
        db.execSQL("DROP TABLE IF EXISTS bitacora")
        db.execSQL("DROP TABLE IF EXISTS proyecto_pasantia")
        db.execSQL("DROP TABLE IF EXISTS subgrupo_tge")
        db.execSQL("DROP TABLE IF EXISTS propuesta_perfil")
        db.execSQL("DROP TABLE IF EXISTS grupo_tgi")
        db.execSQL("DROP TABLE IF EXISTS documento")
        db.execSQL("DROP TABLE IF EXISTS asignacion_jurado")
        db.execSQL("DROP TABLE IF EXISTS alumno_trabajo")
        db.execSQL("DROP TABLE IF EXISTS solicitud_modalidad")
        db.execSQL("DROP TABLE IF EXISTS trabajo_graduacion")
        db.execSQL("DROP TABLE IF EXISTS rol_opcion_menu")
        db.execSQL("DROP TABLE IF EXISTS usuario")
        db.execSQL("DROP TABLE IF EXISTS opcion_menu")
        db.execSQL("DROP TABLE IF EXISTS personero")
        db.execSQL("DROP TABLE IF EXISTS rol")
        db.execSQL("DROP TABLE IF EXISTS modulo_menu")
        db.execSQL("DROP TABLE IF EXISTS modalidad")
        db.execSQL("DROP TABLE IF EXISTS grupo_tge")
        db.execSQL("DROP TABLE IF EXISTS empresa")

        onCreate(db)
    }
}