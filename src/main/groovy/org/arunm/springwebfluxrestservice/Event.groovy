package org.arunm.springwebfluxrestservice

import groovy.transform.ToString
import groovy.transform.TupleConstructor

import java.time.LocalDate

@TupleConstructor
@ToString
class Event {
    long id
    LocalDate date
}

