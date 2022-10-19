package concepts.classes_and_objects.classes

import kotlin.reflect.KProperty

/**
 * Delegated properties : Delegation is passing responsibilities to other objects.
 * Property are useful in case of common kind of property, instead of implementing common logic every time, with
 * property delegate we can implement logic once and reuse logic.
 *
 * get() and set() corresponding to property is delegated to getValue() and setValue() operator
 *
 * The syntax : var <property_name>: <Type> by <expression>
 */
class Student {
    var firstName: String? = null
        set(value) {
            if (value != null && value.length > 1)
                field = value.trim()
        } // Using setter
    var lastName: String? by NameDelegate() // Using delegate property

    override fun toString(): String {
        return "$firstName $lastName"
    }
}

class NameDelegate {
    var formattedValue: String? = null

    operator fun setValue( //operator overloading of setValue
        thisRef: Any?, //calling object of type Any?
        property: KProperty<*>,
        value: String?
    ) {
        if (value != null && value.length > 1)
            formattedValue = value.trim()
    }

    operator fun getValue(thisReference: Any?, property: KProperty<*>): String? {
        return formattedValue
    }
}

fun main() {
    val student = Student()
    student.firstName = " Vidyesh"
    student.lastName = "Churi "
    println(student)
}