# SQL de BIOKLAM

La carpeta `sql/` contiene los scripts principales para instalar la base desde cero:

| Archivo o carpeta | Contenido |
| --- | --- |
| `DDL_KLAM.sql` | Esquema completo: tablas, columnas, claves, índices y restricciones. |
| `procedimientos/` | Procedimientos de usuarios, clientes, inventario, cirugías, documentos y reportes. Los dos archivos de inventario son complementarios y no repiten definiciones. |
| `DML_KLAM.sql` | Datos de demostración: 40 cuentas de personal, diez pacientes ficticios, diez clínicas y hospitales, 20 cuentas ficticias de clientes, bandejas, consumibles, cirugías y auditoría. |
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
mysql -u USUARIO -p < sql/procedimientos/reportes-procedimientos.sql
```

Para la demostración académica, cargar al final los datos de prueba:

```text
mysql -u USUARIO -p < sql/DML_KLAM.sql
```

El DDL crea el esquema y las tablas en orden compatible con sus claves foráneas. Los procedimientos se instalan después de las tablas, y el DML inserta primero los registros referenciados. Ningún script adicional de modificación de tablas es necesario para una instalación nueva. Para actualizar una base existente, se debe comparar su esquema antes de aplicar cambios; el DDL no actualiza tablas ya creadas.

Las 60 cuentas de demostración usan `BioKlam2026!`; son solo para laboratorio y deben sustituirse antes de cualquier uso real. Las cuentas de representantes de instituciones son ficticias y no implican autorización de ellas. Registrar un cliente comercial no crea una cuenta de acceso. La columna `paciente_particular.nombre` guarda el nombre completo porque el esquema actual no tiene una columna separada de apellidos. Las rutas `/docs/...` del DML son referencias ilustrativas y no incluyen archivos físicos.

Los cuatro procedimientos de `reportes-procedimientos.sql` se conservan: `buscar_consumos_reporte` abastece los reportes REST y los otros tres se usan en las plantillas Jasper RF18/RF19. El reporte RF18 solo cuenta en su frecuencia las cirugías con consumo positivo del insumo y ordena por cantidad consumida.

El DML principal no registra consumos reales por cirugía. RF18 y RF19 mostrarán resultados cuando se registren consumos completos mediante la aplicación.
