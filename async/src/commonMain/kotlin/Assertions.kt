import kotlin.test.fail

object Assertions {

    suspend fun assertThrowsAsync(block: suspend () -> Unit) = try {
        block()
        fail("Did not get expected exception.")
    } catch (expected: Exception) {
        expected
    }

}