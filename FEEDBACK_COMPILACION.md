# Feedback: Correcciones para que el proyecto compile

Este documento resume los cambios que he tenido que hacer en tu rama para que el proyecto compile correctamente. Léelo con atención, entiende cada punto y aplica estos aprendizajes en futuros desarrollos.

---

## 1. Imports incorrectos o innecesarios en `Main.scala`

**Problema:** En `Main.scala` había varios imports que no se utilizaban o que estaban duplicados:

```scala
// Estos imports sobraban:
import config.AppConfig        // Ya se importa AppConfig._ (que incluye todo)
import domain.IoTDomain._      // No se usa directamente en Main
import org.apache.spark.sql.Dataset  // No se usa el tipo Dataset explícitamente
import utils.DataTransformations._   // No se usa en Main
```

**Solución:** Se eliminaron los imports no utilizados. Dejar imports innecesarios no impide compilar por sí solo, pero genera warnings y confusión. En algunos casos, imports conflictivos sí pueden causar errores.

---

## 2. Uso incorrecto de `cube` en el análisis de temperatura (`Main.scala`)

**Problema:** La función `cube` se estaba usando de forma incorrecta. El código original hacía:

```scala
// MAL - groupBy + cube no tiene sentido juntos
val tempAnalyticsCube = enrichedTempDF
  .groupBy(
    window($"timestamp", "1 hour"),
    $"zoneId",
    $"sensorId"
  )
  .cube($"zoneId", $"sensorId")   // Error: cube no se aplica sobre un GroupBy
  .agg(...)
```

**Solución:** `cube` es una alternativa a `groupBy`, no un complemento. Se usa directamente sobre el DataFrame:

```scala
// BIEN - cube directamente con todas las columnas de agrupación
val tempAnalyticsCube = enrichedTempDF
  .cube(
    window($"timestamp", "1 hour"),
    $"zoneId",
    $"sensorId"
  )
  .agg(...)
```

**Concepto clave:** `cube` genera automáticamente todas las combinaciones posibles de agrupamiento (incluyendo subtotales y total general). No necesitas hacer `groupBy` antes.

---

## 3. Campo `None` de más en `SoilMoistureData` (`DataValidationsV2.scala`)

**Problema:** Al construir un `SoilMoistureData`, se pasaba un cuarto parámetro `None` que no existe en la case class:

```scala
// MAL - SoilMoistureData solo tiene 3 campos (id, moisture, timestamp)
.mapN((id, mst, time) => SoilMoistureData(id, mst, time, None))
```

**Solución:** Eliminar el parámetro sobrante:

```scala
// BIEN
.mapN((id, mst, time) => SoilMoistureData(id, mst, time))
```

**Concepto clave:** Cuando defines una case class, el número y tipo de parámetros en el constructor debe coincidir exactamente. Si la case class tiene 3 campos, no puedes pasar 4 argumentos.

---

## 4. Import con ruta incorrecta en `utils/package.scala`

**Problema:** Se intentaba importar `AppConfig` sin especificar el paquete completo:

```scala
// MAL - AppConfig está dentro del paquete "config"
import AppConfig.sensorToZoneMap
```

**Solución:** Usar la ruta completa del paquete:

```scala
// BIEN
import config.AppConfig.sensorToZoneMap
```

**Concepto clave:** En Scala, los imports deben reflejar la estructura de paquetes. Si una clase está en el paquete `config`, debes incluirlo en el import.

---

## 5. Import faltante en el test `MainAppConfigTest.scala`

**Problema:** El test usaba `AppConfig` pero no lo importaba:

```scala
// Faltaba esta línea
import config.AppConfig
```

**Solución:** Añadir el import correspondiente.

---

## 6. Faltaban definiciones en `AppConfig.scala`

**Problema:** El código en otros ficheros referenciaba constantes y mapas (`sensorToZoneMap`, `OneMinute`, `WatermarkDuration`, etc.) que no existían en `AppConfig`.

**Solución:** Se añadieron a `AppConfig.scala`:

- **Mapeo de sensores a zonas** (`sensorToZoneMap`): un `Map[String, String]` que asigna cada sensor a su zona.
- **Constantes de duración** (`OneMinute`, `OneHour`, `OneDay`, etc.): cadenas reutilizables para configurar ventanas de tiempo y watermarks en Spark Structured Streaming.
- **Type aliases** (`SensorId`, `ZoneId`): para hacer el código más legible.

**Concepto clave:** Cuando tu código referencia valores que no están definidos en ningún sitio, el compilador no puede resolverlos. Antes de usar una variable o constante, asegúrate de que está definida y es accesible desde donde la usas.

---

## Resumen rápido

| Fichero | Problema | Tipo de error |
|---|---|---|
| `Main.scala` | Imports innecesarios | Limpieza |
| `Main.scala` | `groupBy` + `cube` mal combinados | Error de API de Spark |
| `DataValidationsV2.scala` | Parámetro `None` de más | Error de constructor |
| `utils/package.scala` | Import sin paquete `config` | Error de import |
| `MainAppConfigTest.scala` | Falta import de `AppConfig` | Error de import |
| `AppConfig.scala` | Faltan constantes y mapas | Símbolos no definidos |

---

## Valoración del trabajo realizado

Allan, aquí tienes un repaso de cómo ha ido cada tarea. Lo primero: se nota que le has dedicado tiempo y que has intentado abordar varias de las tareas pedidas. Eso está bien. Pero hay cosas importantes que corregir, y sobre todo, **el proyecto no compilaba** tal y como lo entregaste. Vamos punto por punto:

---

### T1: Validaciones V1 (CO2 y Humedad del Suelo) — NO realizada

Los métodos `validarDatosSensorCO2` y `validarDatosSensorTemperatureHumiditySoilMoisture` en `DataValidations.scala` siguen exactamente igual que como estaban: con el `assert` provisional que se indicaba que había que sustituir. No se han implementado las validaciones de sensorId con regex, ni la comprobación de que los valores numéricos sean válidos, ni la validación de timestamp en rango.

Los tests que has escrito para estos métodos (`DataValidationsTest.scala`) son un buen esfuerzo, pero algunos de ellos fallan precisamente porque la implementación sigue siendo el `assert` básico. Por ejemplo, el test que comprueba que un sensorId inválido lance excepción no puede pasar si el método nunca valida el sensorId.

**Lo que faltaba:** Implementar las validaciones completas siguiendo el patrón de `validarDatosSensorTemperatureHumidity`, que ya estaba hecho como ejemplo.

---

### T2: Validaciones V2 con Cats (CO2 y Humedad del Suelo) — REALIZADA con errores de compilación

Esta es la tarea que mejor has abordado. Has implementado `validarDatosSensorCO2` y `validarDatosSensorSoil` en `DataValidationsV2.scala` usando `ValidatedNel` y `mapN` de Cats. Los validadores genéricos (`validateSensorId`, `validateDouble`, `validateTimestamp`) están bien y los tests cubren casos relevantes.

Sin embargo, había un error que impedía compilar: en `validarDatosSensorSoil` pasabas un cuarto parámetro `None` al construir `SoilMoistureData`, pero la case class solo tiene 3 campos (`sensorId`, `soilMoisture`, `timestamp`). Ese `None` sobraba.

**Nota:** También has añadido la validación de temperatura (`validarDatosSensorTemperature`) que no se pedía expresamente pero está bien tenerla.

---

### T3: Eliminar rutas hardcodeadas — REALIZADA parcialmente

Has utilizado `getRutaParaTabla()` y `getRutaParaTablaChk()` de `AppConfig` para la persistencia en Delta Lake del stream de temperatura. Bien hecho.

Sin embargo, hay un detalle: en la línea donde configuras el servidor de Kafka para temperatura, has puesto `"localhost:9092"` directamente en vez de usar la constante `kafkaBootstrapServers` de `AppConfig`. Es un descuido menor pero va en contra del espíritu de esta tarea.

---

### T4: Asignar zona a los datos de CO2 — NO realizada

En tu `Main.scala` no hay ningún procesamiento del stream de CO2. Solo se procesa el stream de temperatura/humedad. El broadcast join con el JSON de zonas que has implementado se aplica únicamente al stream de temperatura, no al de CO2 como pedía esta tarea.

Para completarla, habrías necesitado leer el topic `co2` de Kafka, aplicar las validaciones, y enriquecer los datos con la zona (ya sea con la UDF `sensorIdToZoneId` o con el broadcast join que ya tienes montado).

---

### T5: Persistir CO2 y Humedad del Suelo en Delta Lake — NO realizada

Solo se persiste en Delta Lake el stream de temperatura. En tu `Main.scala` no hay ningún procesamiento de los streams de CO2 ni de Humedad del Suelo: no se leen de Kafka, no se validan, no se muestran por consola y no se escriben en Delta.

Esta era una de las tareas con más peso (4 horas estimadas) y no se ha abordado.

---

### T6: Analíticas básicas (min/max/count) — REALIZADA parcialmente, con error

Has añadido `max("temperature")` y `count("sensorId")` a la agregación de temperatura, que era lo que se pedía. Bien.

El problema es que la forma de usar `cube` estaba mal: hacías `groupBy(...).cube(...)`, y eso no funciona en Spark. `cube` se usa directamente sobre el DataFrame como alternativa a `groupBy`, no como complemento. Este error impedía compilar.

---

### Bonus — Enfoque interesante pero incompleto

Has cargado el mapeo de zonas desde un fichero JSON (`mapping_zonas.json`) usando broadcast join en vez de la UDF hardcodeada para enriquecer el stream de temperatura. Esto se acerca a la Opción A del bonus (externalizar el mapa de sensores). Sin embargo, no has modificado `application.conf` ni hay un test que verifique la carga, que era lo que se pedía. Además, este enfoque solo se aplica al stream de temperatura, no a CO2 ni Humedad del Suelo.

---

### Tests — NO realizados

En tu entrega no se incluyen tests unitarios. Las tareas T1, T2 y T6 pedían expresamente escribir tests para los nuevos métodos y validaciones. Los criterios de evaluación asignan un 20% a tests unitarios (cobertura y calidad), por lo que esta carencia tiene un impacto significativo en la nota.

---

### Resumen visual

| Tarea | Estado | Comentario |
|---|---|---|
| **T1** Validaciones V1 CO2/Soil | No realizada | Los métodos siguen con `assert` provisional |
| **T2** Validaciones V2 con Cats | Realizada (con error) | Bien planteada, pero `SoilMoistureData` tenía un parámetro de más |
| **T3** Rutas hardcodeadas | Realizada parcialmente | Bien para temperatura, pero `"localhost:9092"` hardcodeado |
| **T4** Zona para CO2 | No realizada | No hay procesamiento de CO2 en Main |
| **T5** Persistir CO2/Soil en Delta | No realizada | Solo temperatura se persiste; CO2 y Soil no se procesan |
| **T6** Analíticas min/max/count | Realizada (con error) | Buena idea, pero `cube` mal usado |
| **Bonus** | Enfoque interesante | JSON + broadcast para temperatura, pero incompleto |
| **Tests** | No realizados | No se incluyen tests en la entrega |
| **Compilación** | No compilaba | 5 errores que impedían compilar |

---

### Nota final

Allan, se ve que has trabajado en el proyecto y que has entendido parte de la arquitectura. El uso de broadcast join para enriquecer los datos de temperatura es una buena decisión técnica. Las validaciones V2 con Cats están bien planteadas.

Sin embargo, la entrega se queda corta respecto a lo pedido. De las 6 tareas obligatorias, solo T2 (con error), T3 (parcial) y T6 (con error) se han abordado. T1, T4 y T5 no se han realizado. Además, no se incluyen tests unitarios y el proyecto no compilaba.

En concreto, el `Main.scala` solo procesa el stream de temperatura/humedad. Los streams de CO2 y Humedad del Suelo no se leen de Kafka, no se validan, no se enriquecen con zona y no se persisten — lo que deja sin hacer T4 y T5 por completo.

Donde hay que mejorar es en:
- **Abordar todas las tareas pedidas.** T1, T4 y T5 no se han tocado, y representan 9 de las 17 horas estimadas.
- **Escribir tests.** Los criterios de evaluación asignan un 20% a tests y no hay ninguno en la entrega.
- **Verificar que el código compila antes de entregar.** Esto es fundamental en cualquier entrega.
- **Entender bien las APIs que usas.** El error de `cube` indica que se usó sin acabar de entender cómo funciona.
