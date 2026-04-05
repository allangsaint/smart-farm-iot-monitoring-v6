### Guía de Cobertura de Tests (Scoverage)

Para asegurar la calidad del código, hemos añadido el plugin `sbt-scoverage`. Este plugin nos permite medir qué parte de nuestro código fuente está siendo ejercitada por nuestros tests unitarios.

#### Cómo generar el reporte de cobertura

Ejecuta el siguiente comando en la terminal desde la raíz del proyecto:

```bash
sbt coverage test coverageReport
```

Este comando realiza tres acciones:
1. `coverage`: Instrumenta el código fuente (añade "marcadores" para saber qué se ejecuta).
2. `test`: Ejecuta todos los tests unitarios.
3. `coverageReport`: Genera los reportes visuales basados en la ejecución de los tests.

#### Dónde ver los resultados

Una vez finalizado, puedes encontrar el reporte detallado en formato HTML en:

`target/scala-2.13/scoverage-report/index.html`

Ábrelo con cualquier navegador para ver qué líneas de código han sido cubiertas (en verde) y cuáles faltan por probar (en rojo).

#### Notas importantes para el entorno de desarrollo

- Se ha configurado `allowUnsafeScalaLibUpgrade := true` en `build.sbt` para permitir la instrumentación de cobertura en este entorno, ya que algunas librerías (como Spark/Delta) pueden requerir versiones específicas de Scala que el plugin todavía está integrando.
- Para limpiar los datos de cobertura anteriores y volver a compilar sin instrumentación, usa: `sbt coverageOff clean`.
