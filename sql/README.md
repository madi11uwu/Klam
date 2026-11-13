# SQL de BIOKLAM

La carpeta `sql/` contiene cuatro partes para explicar e instalar la base desde cero:

| Archivo o carpeta | Contenido |
| --- | --- |
| `DDL_KLAM.sql` | Esquema completo: tablas, columnas, claves, índices y restricciones. |
| `procedimientos/` | Procedimientos de usuarios, clientes, inventario, cirugías y documentos. Los dos archivos de inventario son complementarios y no repiten definiciones. |
| `DML_KLAM.sql` | Datos de demostración: personal con hashes marcadores, nueve pacientes ficticios, clínicas ficticias, bandejas cortas y largas, consumibles estándar, cirugías, auditoría y otros ejemplos. |
| `DROPS.sql` | **Destructivo**: elimina todas las tablas y sus datos. No forma parte de la instalación. |

## Instalación desde cero

Se requiere MySQL 8.0.16 o posterior, InnoDB y permisos para crear el esquema `klam` y los procedimientos. Ejecutar desde la raíz del repositorio con el cliente `mysql`, que interpreta `DELIMITER`. Usar una base nueva y vacía; no ejecutar estos comandos sobre una instalación en uso.

```text
mysql -u USUARIO -p < sql/DDL_KLAM.sql
mysql -u USUARIO -p < sql/procedimientos/usuarios-procedimientos.sql
mysql -u USUARIO -p < sql/procedimientos/clientes-procedimientos.sql
mysql -u USUARIO -p < sql/procedimientos/inventario-procedimientos.sql
mysql -u USUARIO -p < sql/procedimientos/inventario-procedimientos-extra.sql
mysql -u USUARIO -p < sql/procedimientos/cirugias-procedimientos.sql
mysql -u USUARIO -p < sql/procedimientos/documentos-procedimientos.sql
```

Para la demostración académica, cargar al final los datos de prueba:

```text
mysql -u USUARIO -p < sql/DML_KLAM.sql
```

El DDL crea el esquema y las tablas en orden compatible con sus claves foráneas. Los procedimientos se instalan después de las tablas, y el DML inserta primero los registros referenciados. Ningún script adicional de modificación de tablas es necesario para una instalación nueva.

Los hashes de personal del DML son marcadores inválidos: las credenciales utilizables deben aprovisionarse de forma segura fuera del repositorio. Registrar un cliente comercial no crea una cuenta de acceso. La columna `paciente_particular.nombre` guarda el nombre completo porque el esquema actual no tiene una columna separada de apellidos. Las rutas `/docs/...` del DML son referencias ilustrativas y no incluyen archivos físicos.

El proyecto no contiene una ruta automática de actualización de bases existentes. Para una instalación en uso, respaldar y comparar primero su esquema con `DDL_KLAM.sql`; no volver a ejecutar el DDL, el DML ni `DROPS.sql` sobre ella. Las versiones anteriores de los scripts de desarrollo permanecen recuperables en el historial Git, en el commit `e3a0302`.
