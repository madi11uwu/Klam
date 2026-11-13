# SQL de BIOKLAM

## Alcance

`DDL_KLAM.sql` define el esquema definitivo para una **instalación nueva**. Incluye las columnas y relaciones incorporadas anteriormente por RF05, RF07, RF08, RF13 y la identificación del paciente de la cirugía. `DML_KLAM.sql` contiene datos **solo de demostración académica**; ninguna fila del DML es indispensable para crear el esquema. Los nueve pacientes de ejemplo tienen identificadores y contactos ficticios. Como `paciente_particular` posee una sola columna `nombre`, allí se guarda el nombre completo; no se añadieron columnas de apellidos.

`procedimientos/` contiene las definiciones vigentes, separadas por responsabilidad. Los dos archivos de inventario son complementarios: el principal contiene CRUD de equipos, consumibles y bandejas; `inventario-procedimientos-extra.sql` contiene consultas de disponibilidad heredadas, despacho, retorno y consumo. Se mantienen separados porque no duplican definiciones.

`migrations/legacy/` conserva cambios para **bases anteriores**, además del script histórico de demostración `rol2_historial_auditoria.sql`. No forma parte de una instalación nueva. `clientes-credenciales.sql` incluye datos de acceso de demostración y operaciones que reemplazan contraseñas: **no ejecutarlo como preparación de una base actual**. `DROPS.sql` elimina las tablas y todos sus datos; es destructivo y nunca se ejecuta automáticamente.

## Instalación nueva

Se requiere MySQL 8.0.16 o posterior, InnoDB, un usuario con permisos para crear el esquema y procedimientos, y un cliente SQL que respete `DELIMITER` (por ejemplo, la consola `mysql`). Los comandos siguientes se ejecutan desde la raíz del repositorio, en una base vacía y aislada. `DDL_KLAM.sql` crea `klam` y activa ese esquema; cada archivo de procedimientos selecciona el mismo esquema.

```text
mysql -u USUARIO -p < sql/DDL_KLAM.sql
mysql -u USUARIO -p < sql/procedimientos/usuarios-procedimientos.sql
mysql -u USUARIO -p < sql/procedimientos/clientes-procedimientos.sql
mysql -u USUARIO -p < sql/procedimientos/inventario-procedimientos.sql
mysql -u USUARIO -p < sql/procedimientos/inventario-procedimientos-extra.sql
mysql -u USUARIO -p < sql/procedimientos/cirugias-procedimientos.sql
mysql -u USUARIO -p < sql/procedimientos/documentos-procedimientos.sql
```

Para cargar los ejemplos, solo si la base está vacía:

```text
mysql -u USUARIO -p < sql/DML_KLAM.sql
```

El DML contiene marcadores de hash inválidos en las cuentas de personal, deliberadamente no utilizables para iniciar sesión. Aprovisionar credenciales reales por un mecanismo seguro y fuera del repositorio. El alta comercial de pacientes o clínicas no crea automáticamente una `cuenta_usuario`. Las rutas `/docs/...` del DML son referencias ilustrativas y no proporcionan archivos físicos.

El orden anterior primero crea todas las tablas, luego los procedimientos auxiliares y finalmente los datos. Los procedimientos del mismo archivo se crean en el orden de sus dependencias. Las claves foráneas del DML apuntan solo a filas insertadas antes en ese archivo. No se requieren migraciones históricas para una instalación nueva.

## Actualizar una instalación existente

No ejecutar el DDL completo ni el DML sobre una base con datos. Respaldar y comprobar la versión real del esquema y de cada procedimiento; después seleccionar **solo** las migraciones faltantes de `migrations/legacy/`, en el orden de sus dependencias, ajustándolas a esa versión. Algunas migraciones recrean procedimientos que ya han evolucionado; tras la estructura necesaria, instalar las definiciones vigentes desde `procedimientos/` con el orden anterior. No ejecutar `clientes-credenciales.sql` ni `rol2_historial_auditoria.sql` como migraciones automáticas. Verificar firmas con los DAO antes de desplegar Java. No se ejecutó ninguna migración ni se modificó la base conectada durante esta consolidación.

## Migraciones históricas conservadas

| Archivo | Cambio ya consolidado para una instalación nueva |
| --- | --- |
| `migracion-documentos-cliente.sql` | Relación opcional de `documento_ingreso` con `cuenta_usuario`. |
| `rf08-vendedor-solicitud-migracion.sql` | Vendedor registrador y procedimientos de solicitud. |
| `rf13-consumibles-previstos-migracion.sql` | Tabla de estándar y tabla de previstos por cirugía. Para bases antiguas, copia los despachos anteriores al estándar solo tras revisar los datos. |
| `cirugia-plantillas-compartidas-migracion.sql` | Índice no único de bandeja y procedimientos de cirugía que permiten compartir la plantilla. |
| `cirugia-paciente-migracion.sql` | Nombre y documento del paciente intervenido en `cirugia`. |
| `clientes-credenciales.sql` | Tabla de cuentas de cliente; contiene carga de credenciales demostrativas que no debe ejecutarse en una base existente. |
| `rol2_historial_auditoria.sql` | Demostración histórica con modificaciones de datos; no es migración de esquema. |

Las comprobaciones de esta tarea son estáticas. La ejecución de instalación completa queda pendiente de una base temporal aislada y autorizada.
