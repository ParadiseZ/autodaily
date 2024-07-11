package com.smart.autodaily

import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val str = "click(+100,0)"
        val test = "click"
        val or = listOf("click(+100,0)", "CLICK", "click","click(-300,+10)")
        or.forEach{
            when(it){
                test ->{
                    println(test)
                }
               else ->{
                   when{
                       it.startsWith(test) ->{
                           val (x,y) = it.substring(test.length+1, it.length-1).split(",")
                           println("sub：${x.toFloat()}, ${y.toFloat()}")
                       }
                   }
               }
            }
        }
        println("--over addition_isCorrect--")
    }
}