import kotlinx.js.JsPlainObject

@JsPlainObject
sealed external interface ExampleJavascriptObject {
    var firstThing: String
    var optionalThing: Int?
}
