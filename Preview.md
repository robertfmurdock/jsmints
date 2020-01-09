# <img src="test-mint-2.png" alt="Logo" width="100"> testmints

Testmints is a low-cal solution to bring a tiny bit of sugar to your Kotlin Multiplatform tests.

    @Test
    fun plusOne() = setup(object {
        val input: Int = Random.nextInt()
        val expected = input + 1
    }) exercise {
        input.plusOne()
    } verify { result ->
        assertEquals(expected, result)
    }
