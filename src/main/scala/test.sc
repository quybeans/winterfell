// scalastyle:off

class A { def foo(i:Int) = print(i) }

trait B extends A { override def foo(i:Int) = super.foo( i + 1 ) }

trait C extends A { override def foo(i:Int) = super.foo( i * 2) }

trait D extends A { override def foo(i:Int) = super.foo( i * i ) }

val x = new A with B with C with D

x.foo(3)
