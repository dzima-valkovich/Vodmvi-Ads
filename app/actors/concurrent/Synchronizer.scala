package actors.concurrent

//the class wait while two insiders objects has been installed
//after that perform user function
case class Synchronizer[X, Y](userFunction: (X, Y) => Unit) {
  private var count: Int = 0
  private var a: X = _
  private var b: Y = _

  private def foo(): Unit = {
    if (count == 2) {
      userFunction(a, b)
    }
  }

  def objectA: X = a

  def objectB: Y = b

  def synchronizeObjectA(o: X): Unit = {
    count += 1
    a = o
    foo()
  }

  def synchronizeObjectB(o: Y): Unit = {
    count += 1
    b = o
    foo()
  }
}
