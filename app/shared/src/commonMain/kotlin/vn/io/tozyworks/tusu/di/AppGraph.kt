package vn.io.tozyworks.tusu.di

import vn.io.tozyworks.tusu.Greeting
import vn.io.tozyworks.tusu.data.db.TusuDatabase

interface AppGraph {
    val greeting: Greeting
    val tusuDatabase: TusuDatabase
}
