package stdlib

object ObjectsHelper {

  class SecretAgent(val name: String) {
    def shoot(n: Int) {
      SecretAgent.decrementBullets(n)
    }
  }

  object SecretAgent {
    //This is encapsulated!
    var bullets: Int = 3000

    private def decrementBullets(count: Int) {
      if (bullets - count <= 0) bullets = 0
      else bullets = bullets - count
    }
  }

}
