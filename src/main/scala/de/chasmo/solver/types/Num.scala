package de.chasmo.solver
package types


/**
 * A Number that os displayed
 */
sealed trait Num {
  val next: Num
  val value: Int
  val wins: Boolean = false

  override def toString = "[%4s]" format value
}

object Num extends PartialFunction[Int, Num] {
  val numMappings = Map(
    2 -> `2`,
    4 -> `4`,
    8 -> `8`,
    16 -> `16`,
    32 -> `32`,
    64 -> `64`,
    128 -> `128`,
    256 -> `256`,
    512 -> `512`,
    1024 -> `1024`,
    2048 -> `2048`
  )

  def isDefinedAt(x: Int) = numMappings.contains(x)
  def apply(v1: Int) = numMappings(v1)
  def unapply(n: Num): Option[Int] = Some(n.value)
}
/** Why numbers? Because we can! */
case object `16384` extends Num {
  val value = 16384
  val next = this

  override val wins = true
}
case object `8192` extends Num {
  val value = 8192
  val next = `16384`

  override val wins = true
}
case object `4096` extends Num {
  val value = 4096
  val next = `8192`

  override val wins = true
}
case object `2048` extends Num {
  val value = 2048
  val next = `4096`

  override val wins = true
}
case object `1024` extends Num {
  val value = 1024
  val next = `2048`
}
case object `512` extends Num {
  val value = 512
  val next = `1024`
}
case object `256` extends Num {
  val value = 256
  val next = `512`
}
case object `128` extends Num {
  val value = 128
  val next = `256`
}
case object `64` extends Num {
  val value = 64
  val next = `128`
}
case object `32` extends Num {
  val value = 32
  val next = `64`
}
case object `16` extends Num {
  val value = 16
  val next = `32`
}
case object `8` extends Num {
  val value = 8
  val next = `16`
}
case object `4` extends Num {
  val value = 4
  val next = `8`
}
case object `2` extends Num {
  val value = 2
  val next = `4`
}
