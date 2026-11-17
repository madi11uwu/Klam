# RF15 · Consumo real y cierre por cirugía — contrato de datos

Responsable: Rol 5. Consumidor principal: Rol 6 (RF18 y RF19, reportes). Todos los endpoints exigen
`Authorization: Bearer <token>` de un **ADMINISTRADOR** (401 sin sesión, 403 con otro rol).
Base: `/api/v1`.

## Endpoints

| Método | Ruta | Para qué |
| --- | --- | --- |
| GET | `/consumos?q=&desde=AAAA-MM-DD&hasta=AAAA-MM-DD` | Cirugías **FINALIZADAS** (clínicas y pacientes particulares) con estado de documentación y de consumo |
| GET | `/consumos/{idCirugia}` | Detalle: despachado, consumido y diferencia por consumible, más totales |
| PUT | `/consumos/{idCirugia}` | Guarda cantidades consumidas; con `confirmar: true` el consumo queda completo |

Errores: `{"error": "mensaje"}` con 400 (validación o regla de negocio), 404 (cirugía inexistente) o 500 (base de datos).

## Estados

- `estadoOrdenCompra`: `NO_APLICA` (paciente particular) · `POR_ADJUNTAR` · `PENDIENTE` · `VALIDADO` · `RECHAZADO`
- `estadoConsumo`: `NO_DISPONIBLE` (falta documentación de cierre o bandeja) · `POR_REGISTRAR` · `COMPLETO`
- `documentacionCierreCompleta`: clínica → OC validada; paciente particular → comprobante de pago validado.
- Secuencia clínica/hospital: cirugía culminada → OC cargada → OC validada → registro de consumo. El paciente particular no lleva OC.

## Ejemplo: `GET /consumos/8`

```json
{
  "idCirugia": 8,
  "fechaHoraInicio": "2026-10-28T08:00:00",
  "tipoProcedimiento": "Cirugía de columna",
  "tipoCliente": "CLINICA_HOSPITAL",
  "clienteNombre": "Clínica Ejemplo",
  "idBandeja": 3,
  "requiereOrdenCompra": true,
  "estadoOrdenCompra": "VALIDADO",
  "documentacionCierreCompleta": true,
  "estadoConsumo": "COMPLETO",
  "doctorNombre": "Dr. Ejemplo",
  "pacienteNombre": "Paciente Ejemplo",
  "clinicaNombre": "Clínica Ejemplo",
  "archivoOrdenCompra": "/docs/oc-cir-008.pdf",
  "motivoRechazoOrdenCompra": null,
  "totalDespachado": 6,
  "totalConsumido": 4,
  "diferencia": 2,
  "consumibles": [
    { "idConsumible": 1, "nombre": "Tornillo pedicular", "medida": "6 mm",  "cantidadDespachada": 4, "cantidadConsumida": 3, "diferencia": 1 },
    { "idConsumible": 2, "nombre": "Fresa cortante",     "medida": "70 mm", "cantidadDespachada": 2, "cantidadConsumida": 1, "diferencia": 1 }
  ]
}
```

(Valores de ejemplo; la forma de la respuesta es la real.) `diferencia = cantidadDespachada − cantidadConsumida`.
Solo las cirugías con `estadoConsumo = COMPLETO` tienen cantidades definitivas; en las demás `cantidadConsumida` es 0 o un guardado parcial.

## Ejemplo: `PUT /consumos/8`

```json
{
  "idCirugia": 8,
  "confirmar": true,
  "lineas": [
    { "idConsumible": 1, "cantidadConsumida": 3 },
    { "idConsumible": 2, "cantidadConsumida": 1 }
  ]
}
```

Reglas: `idCirugia` del cuerpo debe coincidir con la ruta · cantidad entre 0 y lo despachado · sin consumibles repetidos ·
al confirmar deben venir **todos** los consumibles de la bandeja · todo se guarda en una transacción (o se guardan todas las líneas o ninguna) ·
con el consumo completo ya no se puede modificar (400).

## Para el Rol 6

- RF19 (por cirugía): `GET /consumos/{idCirugia}` ya trae cabecera (fecha, procedimiento, paciente, médico, cliente) y la tabla despachado/consumido/diferencia.
- RF18 (por periodo): `GET /consumos?desde=&hasta=` da las cirugías del periodo; los consumibles se leen del detalle de cada una. Para agrupar por consumible y tipo de cirugía conviene sumar solo cirugías `COMPLETO`.
- Si prefieren un endpoint plano (una fila por consumible, con filtros de tipo de cirugía y consumible), se agrega sobre `buscar_consumos`; hay que acordarlo antes de calcular.

## Matriz de pruebas (esperado / obtenido)

| # | Caso | Esperado | Obtenido |
| --- | --- | --- | --- |
| 1 | Guardar cantidades válidas, recargar | 200; las cantidades persisten; sigue `POR_REGISTRAR` | |
| 2 | Confirmar con todas las líneas | 200; `estadoConsumo = COMPLETO` | |
| 3 | Editar con consumo completo | 400 "ya fue marcado como completo" | |
| 4 | Cantidad negativa (límite −1) | 400 | |
| 5 | Cantidad = despachada (límite) | 200 | |
| 6 | Cantidad = despachada + 1 | 400 "no puede superar lo despachado" | |
| 7 | Confirmar sin todas las líneas | 400 "Indique la cantidad consumida de …" | |
| 8 | Clínica con OC pendiente/rechazada | `NO_DISPONIBLE`; PUT → 400 "no está VALIDADA" | |
| 9 | Paciente particular sin OC, comprobante validado | `POR_REGISTRAR`; PUT → 200 | |
| 10 | Cirugía no finalizada | GET detalle → 400 | |
| 11 | Cirugía inexistente | 404 | |
| 12 | Vendedor/cliente llama al endpoint | 403 | |
| 13 | Consumible repetido en el cuerpo | 400 | |
| 14 | `idCirugia` del cuerpo distinto al de la ruta | 400 | |
