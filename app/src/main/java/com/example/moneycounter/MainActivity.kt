package com.example.moneycounter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneycounter.ui.theme.MoneyCounterTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoneyCounterTheme {
                val viewModel = viewModel<MainViewModel>()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        MyApp()
                    }
                }
            }
        }

        lifecycleScope.launch {
            delay(5000)
            val intent = Intent(this@MainActivity, AnotherActivity::class.java)
            startActivity(intent)
            finish()// this will finish the main activity
        }

        CoroutineScope(Dispatchers.Main).launch {
            task1()
        }

        CoroutineScope(Dispatchers.Main).launch {
            task2()
        }


        CoroutineScope(Dispatchers.IO).launch {
            printFollowers()
        }

        GlobalScope.launch(Dispatchers.Main) {
            execute()
        }

        GlobalScope.launch(Dispatchers.Main) {
            execute2()
        }

        GlobalScope.launch {
            executeTask()
        }

        //runblocking
        //run it on intellij
        GlobalScope.launch {
            delay(1000)
            println("world")
        }// this will launch a coroutine in parallel, it won't block the thread
        println("hello")// here the program will just print hello and will end because the coroutine is running in parallel and it does on find any else thing to run on this thread
        //one way to print world is we ourselves block the thread by
        Thread.sleep(2000)// this concept is used in threading

        //2nd option is for coroutine

        runBlocking {
            launch {// runBlocking will automatically make a Coroutine scope
                delay(1000)
                println("world")
            }// runblocking will block the thread until all its coroutines are completed
            println("hello") // here hello will be printed first and then world after 1 sec
        }


    }

    private suspend fun executeTask(){
        Log.d("temp", "executeTask: Before")
        GlobalScope.launch {
            delay(1000)
            Log.d("temp", "executeTask: Inside")
        }//this is non blocking nature ,coroutine will be launched and the "After" line is executed and then after 1 sec "inside" is executed by that coroutine
        Log.d("temp", "executeTask: After")


        Log.d("temp2", "executeTask: Before")
        withContext(Dispatchers.IO){
            delay(1000)
            Log.d("temp2", "executeTask: Inside")
        }// this is of blocking nature,it will suspend the coroutine, "after" will get printed when this coroutine is completely executed
        // note it just blocks the coroutine and not the thread
        Log.d("temp2", "executeTask: After")
    }

    private suspend fun execute2(){
        val parentJob = CoroutineScope(Dispatchers.IO).launch {
            for(i in 1..100){
                if(isActive){// condition to check
                    delay(5)
                    executeLongRunningTask()
                    Log.d("val-i", "execute2: ${i.toString()}")
                }
            }
        }

        delay(500)
        Log.d("val-i", "execute2: Cancelling Job")
        parentJob.cancel()//the thread still executes the longrunning task although we canceled the coroutine , so to avoid this we put a check condition
        parentJob.join()
        Log.d("val-i", "execute2: parent completed")
    }

    private fun executeLongRunningTask(){
        for(i in 1..1000000000){

        }
    }

    private suspend fun execute(){
        val parentJob = GlobalScope.launch(Dispatchers.Main) {
            Log.d("pc", "parent: $coroutineContext")

            Log.d("pc", "execute: Parent Started")

            var follow = getInstaFollowers() // coroutine waits for a function call

            Log.d("pc", "execute: $follow")

            val childJob = launch(Dispatchers.IO) {
                try{
                    Log.d("pc", "child: $coroutineContext")

                    Log.d("pc", "execute: child Started")
                    delay(8000)
                    Log.d("pc", "execute: child ended")

                }catch (e: CancellationException){
                    Log.d("pc", "execute: Child job cancelled")
                }
            }// by default child coroutine will inherit parent coroutine context, but we can also explicitly define, like done here i.e. Dispatchers.IO
            //coroutine dosen't  wait for a child coroutine instead it launches the coroutine and proceeds ahead

            delay(3000)
            childJob.cancel()
            Log.d("pc", "execute: Parent Ended")
        }
        // same here coroutine will be lauched and excetuion moves ahead ...if it finds cancel it will cancel the coroutine or
        // if it finds join it will wait for the coroutine

//        delay(1000)
//        parentJob.cancel()
        parentJob.join() // this will keep our coroutine suspended unless the parentjob execution is completed
        // join() will block the thread/coroutine on which join() is called until the associated coroutine completes its execution.
        Log.d("pc", "execute: Parent Completed")
    }

    private suspend fun printFollowers(){
        var fbFollowers = 0
        var instaFollowers = 0

//        method 1
        val job = CoroutineScope(Dispatchers.IO).launch {
            fbFollowers = getFbFollowers()
        }// this gonna take 1 sec

        val jobb = CoroutineScope(Dispatchers.IO).launch {
            instaFollowers = getInstaFollowers()
        }// this gonna take 2 sec

        //total it will take 2 sec as they are on diff coroutine

        job.join()//this will pause the execution until and unless the job coroutine completes its execution
        jobb.join()

        Log.d("follow", "printFollowers1: ${fbFollowers.toString()}")
        Log.d("follow", "printFollowers1: ${instaFollowers.toString()}")


//        method 2

        val job2 = CoroutineScope(Dispatchers.IO).async {
            getFbFollowers()
            "hello"//it will take whatever we return at the last
        }

        Log.d("follow", "printFollowers2: ${job2.await()}")


//        method 3

        CoroutineScope(Dispatchers.IO).launch {
            var fb = getFbFollowers() // here this will take one sec
            var insta = getInstaFollowers() // and this will take 2 sec , hence total of 3 sec , not very optimised
            Log.d("follow", "printFollowers3: $fb, $insta")
        }// here each line will wait until the above one is executed




//        method 4

        CoroutineScope(Dispatchers.IO).launch {
            var fb = async {  getFbFollowers() }// here this will take one sec
            var insta = async {  getInstaFollowers() } // and this will take 2 sec but they both will run parallely hence a total of 2 sec
            Log.d("follow", "printFollowers4: ${insta.await()}")
            Log.d("follow", "printFollowers4: ${fb.await()}")
        }// here each line will wait until the above one is executed



    }


    private suspend fun getFbFollowers(): Int{
        delay(5000)
        return 54
    }

    private suspend fun getInstaFollowers(): Int{
        delay(8000)
        return 117
    }
}

suspend fun task1(){
    Log.d("TAG", "task1: Starting task 1")
//    yield() // fnc will be suspended here
    delay(1000)
    Log.d("TAG", "task1: Ending task 1")
}

suspend fun task2(){
    Log.d("TAG", "task1: Starting task 2")
//    yield()
    delay(2000)
    Log.d("TAG", "task1: Ending task 2")
}

@Composable
fun MyApp() {
    var moneyCounter by remember { mutableIntStateOf(0)}
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = Color(0xFF546E7A)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$moneyCounter",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 35.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            Spacer(
                modifier = Modifier
                .height(30.dp)
            )
            CreateCircle(moneyCounter){ newValue  ->
                moneyCounter = newValue
                Log.d("Name", "onCreate: ${Thread.currentThread().name}")
            }

            Spacer(
                modifier = Modifier
                    .height(30.dp)
            )

            Button(
                onClick = {
                    // this will create a new thread to execute this, and the main thread will be free hence the app will not crash
//                    thread(start = true) {
//                        for (i in 1..100000000L){
//
//                        }
//                        Log.d("Name", "onCreate: ${Thread.currentThread().name}")
//                    }

                    // implementing using coroutines
                    CoroutineScope(Dispatchers.IO).launch {
                        Log.d("Name1", "MyApp: ${Thread.currentThread().name}")
                    }

                    GlobalScope.launch(Dispatchers.Main){
                        Log.d("Name2", "MyApp: ${Thread.currentThread().name}")
                    }

                    MainScope().launch(Dispatchers.Default){
                        Log.d("Name3", "MyApp: ${Thread.currentThread().name}")
                    }
                }
            ) {
                Text(
                    "Execute Task"
                )
            }
        }
    }
}


@Composable
fun CreateCircle(moneyCounter: Int = 0, updateMoneyCounter: (Int) -> Unit) {

//    var moneyCounter2 = remember {
//        mutableIntStateOf(100)
//    }
    Card(
        modifier = Modifier
            .padding(3.dp)
            .size(105.dp)
            .clickable {
                updateMoneyCounter(moneyCounter + 1)
//                moneyCounter2.value +=100
            },
        shape = CircleShape,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            Text("Tap")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    MoneyCounterTheme {
        MyApp()
    }
}