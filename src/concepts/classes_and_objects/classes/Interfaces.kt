package concepts.classes_and_objects.classes

/**
 * Interface :
 *
 * Properties in interface are abstract by default.
 *
 * In case of interface properties and methods are open by nature.
 */

fun main() {
    val button = Button()
    println(button.name)
    button.onTouch()
    button.onClick()
}

interface Interface { // We cannot create instance of interface.
    var name: String // Properties in interface are abstract by default.
    fun onTouch() // Abstract and open by default.
    fun onClick() { // Normal methods are public and open by default but not final.
        println("Button was clicked in Interface.")
    }
}

interface Interface2 { // We cannot create instance of interface.
    var name: String // Properties in interface are abstract by default.
    fun onTouch() {
        println("Button was touched in Interface2.")
    }

    fun onClick() { // Normal methods are public and open by default but not final.
        println("Button was clicked in Interface2.")
    }
}

class Button : Interface, Interface2 {
    override var name: String = "Dummy name"

    override fun onTouch() {
        super.onTouch()
        println("Button was touched in Button class.")
    }

    override fun onClick() {
        super<Interface>.onClick()
        super<Interface2>.onClick()
        println("Button was clicked in Button class")
    }
}