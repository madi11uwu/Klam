# Klam

## Pruebas de clientes mediante la capa de negocio

La clase `pe.edu.pucp.app.PruebaClientesBL`, en el módulo `klam-app`, prueba
`ClinicaHospital` y `PacienteParticular` usando sus interfaces BL.

1. Recargar los proyectos Maven en IntelliJ.
2. Ejecutar `PruebaClientesBL.main()` sin argumentos para comprobar las
   validaciones locales. Este modo no conecta con la base de datos.
3. Después del merge de los procedimientos almacenados, instalarlos en una
   base de pruebas y configurar `klam-dbmanager/src/main/resources/db.properties`.
4. Ejecutar el mismo programa con `--crud` en **Program arguments** para
   insertar, buscar por ID, listar, modificar y eliminar un registro de cada
   tipo. También verifica la persistencia de sus campos y el rechazo de
   documentos duplicados. Los registros llevan nombres de prueba y documentos
   aleatorios; si un documento ya existe, el BL rechaza la inserción.

El programa imprime verificaciones y un resumen; termina con una excepción si
hay fallos. La baja admite que el SP oculte el registro o lo devuelva inactivo,
y comprueba que no aparezca activo al listar. Si ocurre un fallo intermedio,
intenta dar de baja únicamente el registro creado por esa prueba. Con eliminación
lógica, los registros de prueba permanecen inactivos en la BD.

Compilar todos los módulos necesarios:

```text
mvn -pl klam-app -am compile
```

### Alcance de las validaciones

- RF08 del catálogo exige DNI y comprobante bancario al registrar una
  **solicitud de cirugía**. El CRUD valida el DNI; admite un paciente con
  `pagoConfirmado = false`. La carga del comprobante y su aprobación pertenecen
  al flujo de solicitudes/documentos.
- Se conserva la validación de formato y unicidad de DNI/RUC. El nombre es
  obligatorio; se controlan las longitudes del esquema SQL y el formato del
  correo cuando se proporciona. Dirección, correo y teléfono siguen siendo
  opcionales según el esquema.
- El periodo de crédito admite hasta 30 caracteres. El catálogo no define
  plazos permitidos ni exige crédito por tener consignación; no se agregan esas
  restricciones.
- Estas operaciones de mantenimiento no incorporan transacciones explícitas.
  Las operaciones compuestas, como documentos con líneas, se manejan en sus
  correspondientes servicios.
