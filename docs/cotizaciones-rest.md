# Contratos REST de cotizaciones (etapa 3C)

Las rutas de la tabla son relativas a `/api/v1`. Todas las rutas requieren `Authorization: Bearer <token>` de `POST /login`. No se aceptan IDs de cliente o usuario en el cuerpo como prueba de identidad. Los errores usan `{"error":"mensaje"}`. La base debe tener aplicada previamente `sql/cotizaciones-solicitud-incremental.sql` en un entorno autorizado; este cambio no ejecuta la migración.

| Método y ruta | Rol | Entrada | Éxito | Errores |
| --- | --- | --- | --- | --- |
| `POST /solicitudes/{id}/cotizaciones` | `ADMINISTRADOR` | `multipart/form-data`: `archivo` PDF/PNG/JPEG de hasta 5 MB y `precioPactado` decimal tomado del ERP | `201` `{idCotizacion,version,estado}` | `400` archivo/precio inválido; `401` sin sesión; `403` sin rol; `404` solicitud inexistente; `409` regla BL; `500` almacenamiento/BD |
| `GET /cotizaciones/solicitud/{id}` | Administrador o clínica titular | Sin cuerpo | `200` arreglo de resúmenes, incluidas versiones anteriores | `401`, `403`, `404`, `500` |
| `GET /cotizaciones/solicitud/{id}/vigente` | Administrador o clínica titular | Sin cuerpo | `200` resumen | `401`, `403`, `404`, `500` |
| `GET /cotizaciones/{id}/archivo` | Administrador o clínica titular | Sin cuerpo | `200` bytes con tipo MIME y descarga privada | `401`, `403`, `404`, `500` |
| `POST /cotizaciones/{id}/aceptacion` | Clínica titular | Sin cuerpo | `200` `{idCotizacion,estado,version}` | `401`, `403`, `404`, `400` regla BL, `500` |
| `POST /cotizaciones/{id}/rechazo` | Clínica titular | `application/json`: `{"motivo":"texto opcional"}` (máximo 150 caracteres) | `200` `{idCotizacion,estado,version}` | `401`, `403`, `404`, `400` regla BL, `500` |
| `GET /solicitudes/{id}/elegibilidad-cirugia` | Administrador | Sin cuerpo | `200` `{puedeRegistrarCirugia,requiereCotizacion,estadoDocumental,estadoCotizacion,motivoBloqueo}` | `401`, `403`, `404`, `409`, `500` |

El resumen de cotización contiene `idCotizacion`, `idSolicitud`, `version`, `vigente`, `estado`, `precioPactado`, `fechaEmision`, `fechaDecision`, `idCuentaDecision`, `motivoRechazo` y `archivoDisponible`. No expone la ruta física. La versión nueva es `EMITIDA`; se acepta o rechaza solo si sigue vigente y pertenece a la clínica autenticada. Una decisión no registra una cirugía. El estado `VENCIDA` se conserva; aún no existe una fecha de expiración automática en Modelo/SQL.

La carga y cada decisión escriben un evento `COTIZACION` en RF04 dentro de la misma transacción. El evento identifica cotización y versión en su descripción, y registra solicitud, rol, usuario, responsable, acción y estados. El archivo físico se elimina si falla el registro en MySQL.