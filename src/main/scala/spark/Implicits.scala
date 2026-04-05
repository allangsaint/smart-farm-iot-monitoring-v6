package spark

object Implicits {
  final case class Memory(value: MemorySize, unit: ITCapacityUnit) {
    override def toString: String = s"$value$unit"
  }

  type MemorySize     = Int
  type ITCapacityUnit = String // MB, GB, KB (unidades de medida de memoria)

  implicit class IntWithMemorySize(value: MemorySize) {
    // Este implicit permite que cualquier valor de tipo `Int` pueda llamar directamente a `.Gb` o `.Mb`.
    // Por ejemplo, `42.Gb` crea un objeto Memory(42, "g").
    // Es una característica potente de Scala para crear DSLs (Lenguajes Específicos de Dominio).
    def Gb: Memory = Memory(value, "g")
    def Mb: Memory = Memory(value, "m")
    // Se pueden añadir más unidades si fuera necesario (Kb, Tb, etc.)
  }
}

