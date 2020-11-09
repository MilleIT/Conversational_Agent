package furhatos.app.fruitseller.flow

import furhatos.app.fruitseller.book
import furhatos.app.fruitseller.nlu.*
import furhatos.app.fruitseller.pythonLoc
import furhatos.app.fruitseller.scriptPath
import furhatos.app.fruitseller.userEmotion
/*
import furhatos.app.fruitseller.order
*/
import furhatos.flow.kotlin.*
import furhatos.gestures.Gestures
import furhatos.nlu.common.*
import furhatos.records.Location
import kotlinx.coroutines.*
import java.io.File
import java.io.InputStreamReader
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/*
 Return random location Furhat should look at.
 */
fun Loc(): Location {
    val x = Random.nextInt(-5,5)
    val y = Random.nextInt(-1,5)
    val location = Location(x,y,20)
    return location
}

/*
    Return random location Furhat should look at when asking a question.
 */
fun LocQuestion(): Location {
    val i = Random.nextInt(-1,1)
    var x = 0
    var y = 0
    if (i<0) {
        x = 2
        y = 2
    } else {
        x = -2
        y = 2
    }
    val location = Location(x,y,20)
    return location
}

/*
    Make Furhat look at certain location when asking question and after a while look back at user.
 */
val LookQuestion = state(Interaction) {
    onEntry {
        furhat.attend(LocQuestion())
        val i = Random.nextInt(1500,3000);
        furhat.glance(user = users.current, duration = i)
        delay(i.toLong())
        furhat.attend(user = users.current)
    }
}

/*
    Make Furhat look at certain location and after a while look back at user.
 */
val LookAround = state(Interaction) {
    onEntry {
        furhat.attend(Loc())
        delay(Random.nextInt(1500,3000).toLong())
        furhat.attend(user = users.current)
    }

}

/*
 Link python emotion detector with kotlin.
 */
fun startCoroutine() {
    GlobalScope.launch {
        val loc = pythonLoc //must be location of python.exe for python 3.6.6
        val command = listOf<String>(loc,"kotlinclient.py")
        val path : File = File(scriptPath) // must be directory where kotlinclient.py is located
        println("Command: $command")
        while (true){
            val pb = ProcessBuilder(command)
            pb.directory(path)
            pb.redirectErrorStream(true)

            //pb.inheritIO()
            val process = pb.start()
            val reader = Scanner(InputStreamReader(process.inputStream))
            while (reader.hasNextLine()) {
                val latestEmotion : String = reader.nextLine()
                if (latestEmotion != userEmotion){
                    println(latestEmotion)
                    userEmotion = latestEmotion
                }

            }
            process.waitFor()
        }
}
}

val StartEmotions = state {
    startCoroutine()
}

val RunPython = state(Interaction) {
    onEntry {
        val loc = pythonLoc //must be location of python.exe for python 3.6.6
        val command = listOf<String>(loc,"kotlinclient.py")
        val path : File = File(scriptPath) // must be directory where kotlinclient.py is located
        println("Command: $command")
        //val process = Runtime.getRuntime().exec(command, null, path)
        val pb = ProcessBuilder(command)
        pb.directory(path)
        pb.redirectErrorStream(true)

        //pb.inheritIO()
        val process = pb.start()
        //process.waitFor()
        //val reader = BufferedReader(InputStreamReader(process.inputStream))
        //val message = reader.lines().collect(Collectors.joining("\n"))
        //println(message)
        val reader = Scanner(InputStreamReader(process.inputStream))
        var line : String = ""
//        while (reader.readLine() != null)
//            line = reader.readLine()
//            println("tasklist: " + line)
//            var message = reader.lines().collect(Collectors.joining("\n"))
//            println("message: " + message)
        while (reader.hasNextLine()) {
            val latestEmotion : String = reader.nextLine()
            println(latestEmotion)
            userEmotion = latestEmotion
        }
        process.waitFor()
    }

}

val Start = state(Interaction) {
    onEntry {
        parallel {
            goto(StartEmotions)
        }
        furhat.attend(user = users.random)
        furhat.say("Hello! Welcome to the live support of Bol.com. My name is Furhat and I will be assisting you today. ")
        parallel {
            goto (LookAround)
        }
        furhat.say("I can help when a package is late or lost, a wrong package is delivered, or a refund isn't received.")
        furhat.ask("Is your question related to any of these three problems specifically?")
    }

    onReentry {
        furhat.ask({random {
            +"Is your question about one of these problems?"
            +"Are you experiencing one of these problems?"
            +""
        }})
    }

    onResponse<Yes> {
        goto(SpecifyProblem)
    }

    onResponse<No> {
        furhat.gesture(Gestures.ExpressSad(strength = 10.0), async = true)
        furhat.say("Unfortunately, I can only support you with these specific problems.")
        if (userEmotion == "Unhappy") {
            furhat.gesture(Gestures.Blink( strength = 2.0), async = true)
            furhat.gesture(Gestures.Shake( strength = 0.1), async = true)
            furhat.say("I'm sorry I couldn't be of more help today.")
            furhat.say("I'm sure my colleagues could fix the issue for you within no time.")
            furhat.gesture(Gestures.Smile(strength = 1.5), async = true)
        }
        goto(transfer)
    }

    onResponse<WrongPackage> {
        users.current.book.problem = "received the wrong package"
        goto(Problem)

    }
    onResponse<NoPackage> {
        users.current.book.problem = "didn't receive the package"
        goto(Problem)
    }
    onResponse<NoRefund> {
        users.current.book.problem = "didn't receive a refund"
        goto(Problem)
    }

    var noresponse = 0
    onNoResponse {
        noresponse++
        if (noresponse > 3) {
            furhat.gesture(Gestures.Thoughtful(strength = 1.0), async = true)
            furhat.say("Perhaps another time is better. Don't worry about it, you can contact me 24/7.")
        }
        else if (noresponse > 2) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.ask({random {
                +"Are you still there?"
                +"Are you there?"
                +"Are you able to continue our conversation?"
            }})
        }
        else if (noresponse > 1) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"I'm sorry, I still didn't hear you."
                +"Hmm, I still didn't hear you."
            }})
            reentry()
        }
        else {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"Sorry, I don't think I heard you."
                +"Sorry, I didn't hear you."
            }})
            reentry()
        }
    }
}

val transfer = state(Interaction) {
    onEntry {
        furhat.ask("Would you like me to transfer you to one of my colleagues?")
    }

    onReentry {
        furhat.ask({random {
            +"Shall I transer you?"
            +"Would you like to be transferred?"
            +""
        }})
    }

    onResponse<Yes> {
        furhat.say {
            +"Alright, I'll transfer you to my colleague "
            random {
                +"Thomas."
                +"Laura."
                +"James."
                +"Emma."
            }
            +"It will just be a second."
        }
        TimeUnit.SECONDS.sleep(1)
        furhat.say("Goodbye.")
    }

    onResponse<No> {
        furhat.say("Sure, no problem. If you would like to contact them later, their telephone number is 030 310 49 99.")
        furhat.say("Goodbye.")
    }

    var noresponse = 0
    onNoResponse {
        noresponse++
        if (noresponse > 3) {
            furhat.gesture(Gestures.Thoughtful(strength = 1.0), async = true)
            furhat.say("Perhaps another time is better. Don't worry about it, you can contact me 24/7.")
        }
        else if (noresponse > 2) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.ask({random {
                +"Are you still there?"
                +"Are you there?"
                +"Are you able to continue our conversation?"
            }})
        }
        else if (noresponse > 1) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"I'm sorry, I still didn't hear you."
                +"Hmm, I still didn't hear you."
            }})
            reentry()
        }
        else {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"Sorry, I don't think I heard you."
                +"Sorry, I didn't hear you."
            }})
            reentry()
        }
    }
}

val SpecifyProblem = state(Interaction) {
    onEntry {
        parallel {
            goto (LookQuestion)
        }
        furhat.gesture(Gestures.Thoughtful(strength = 0.2), async = true)
        furhat.ask("Could you tell me which of these problems you are experiencing?")
    }

    onReentry {
        furhat.attend(user = users.random)
        furhat.ask({random {
            +"What problem are you experiencing?"
            +"How can I help you?"
            +"What went wrong with your order?"
            +"Please tell me what problem you are experiencing."
            +""
        }})
        furhat.gesture(Gestures.Thoughtful, async = true)
    }

    onResponse<WrongPackage> {
        users.current.book.problem = "received the wrong package"
        goto(Problem)

    }
    onResponse<NoPackage> {
        users.current.book.problem = "didn't receive the package"
        goto(Problem)
    }
    onResponse<NoRefund> {
        users.current.book.problem = "didn't receive a refund"
        goto(Problem)
    }

    var noresponse = 0
    onNoResponse {
        noresponse++
        if (noresponse > 3) {
            furhat.gesture(Gestures.Thoughtful(strength = 1.0), async = true)
            furhat.say("Perhaps another time is better. Don't worry about it, you can contact me 24/7.")
        }
        else if (noresponse > 2) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.ask({random {
                +"Are you still there?"
                +"Are you there?"
                +"Are you able to continue our conversation?"
            }})
        }
        else if (noresponse > 1) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"I'm sorry, I still didn't hear you."
                +"Hmm, I still didn't hear you."
            }})
            reentry()
        }
        else {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"Sorry, I don't think I heard you."
                +"Sorry, I didn't hear you."
            }})
            reentry()
        }
    }

    var nomatch = 0
    onResponse {
        nomatch++
        if (nomatch > 2)
            furhat.say("Sorry, I can only help you with these three specific problems. Please call 030 310 49 99 for any other questions.")
        else if (nomatch > 1) {
            furhat.say("Sorry, I still didn't understand that. I can help when a package didn't arrive, " +
                    "you received the wrong package, or didn't receive a refund. ")
            reentry()
        }
        else {
            furhat.say("Sorry, I didn't understand that.")
            reentry()
        }
    }
}

val Problem = state(Interaction) {
    onEntry {
        parallel {
            goto(LookAround)
        }
        furhat.gesture(Gestures.ExpressSad, async = true)
        furhat.say("I'm sorry that you " + users.current.book.problem + ".")
        if (userEmotion == "Unhappy") {
            furhat.gesture(Gestures.CloseEyes, async = true)
            furhat.gesture(Gestures.Shake( strength = 0.5), async = true)
            furhat.say("I understand this is upsetting.")
            furhat.gesture(Gestures.OpenEyes, async = true)

        }
        parallel{
            goto(LookQuestion)
        }
        furhat.attend(user = users.random)
        furhat.ask("Do you want to tell me what happened?")
    }

    onReentry {
        furhat.listen()
    }

    onResponse<SimpleNo> {
        goto(NoStory)
    }

    onResponse<SimpleYes> {
        furhat.gesture(Gestures.Nod(strength = 0.5));
        furhat.ask("Please tell me, I'm listening.")
        goto(TellStory)
    }

    onResponse {
        furhat.gesture(Gestures.Nod(strength = 0.5));
        goto(TellStory)
    }

    var noresponse = 0
    onNoResponse {
        noresponse++
        if (noresponse > 3) {
            furhat.gesture(Gestures.Thoughtful(strength = 1.0), async = true)
            furhat.say("Perhaps another time is better. Don't worry about it, you can contact me 24/7.")
        }
        else if (noresponse > 2) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.ask({random {
                +"Are you still there?"
                +"Are you there?"
                +"Are you able to continue our conversation?"
            }})
        }
        else if (noresponse > 1) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"I'm sorry, I still didn't hear you."
                +"Hmm, I still didn't hear you."
            }})
            reentry()
        }
        else {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"Sorry, I don't think I heard you."
                +"Sorry, I didn't hear you."
            }})
            reentry()
        }
    }
}

val NoStory = state(Interaction) {
    onEntry {
        furhat.gesture(Gestures.Nod(strength = 0.5));
        furhat.say("Ok, let's focus on fixing this issue immediately.")
        if (userEmotion == "Unhappy") {
            furhat.gesture(Gestures.Nod(strength = 0.3), async = true)
            furhat.gesture(Gestures.Thoughtful( strength = 2.0), async = true)
            furhat.say("I'll do my utmost best for you.")
            furhat.gesture(Gestures.GazeAway( strength = 0.4), async = true)
            TimeUnit.SECONDS.sleep(1)
        }
        goto(OrderNumber)
    }
}

val TellStory = state(Interaction) {
    onEntry {
        TimeUnit.SECONDS.sleep(2)
        furhat.say("Hmm")
        furhat.listen(endSil = 6000)
    }

    onReentry {
        furhat.listen(endSil = 2000)
    }

    onInterimResponse(endSil = 2000) {
        val random = Random.nextInt(5) + 1
        if (random == 1 )
            furhat.say("Okay", async = true)
        else if (random == 2)
            furhat.say("Ok", async = true)
        else if (random == 3)
            furhat.say("I see", async = true)
        else if (random == 4)
            furhat.say("Right", async = true)
        else
            furhat.gesture(Gestures.Nod)
        furhat.listen(endSil = 4000, timeout = 2000)
    }

    var noresponse = 0
    onNoResponse {
        if (noresponse == 1) {
            furhat.say("Ok, let's continue.")
            goto(ToldStory)
        }
        noresponse++
        furhat.ask({random {
            +"Did you want to share more?"
            +"Is there more you wanted to tell?"
        }})
    }

    onResponse {
        goto(ToldStory)
    }
}

val ToldStory = state(Interaction) {
    onEntry {
        furhat.say("This is unfortunate indeed. I'm sorry this happened.")
        if (userEmotion == "Happy") {
            furhat.gesture(Gestures.BigSmile(strength = 0.7, duration = 2.0), async = true)
            furhat.say ("Despite all this you still look optimistic. I admire that." )
        } else if (userEmotion == "Unhappy") {
            furhat.gesture(Gestures.CloseEyes, async = true)
            furhat.gesture(Gestures.Shake( strength = 0.5), async = true)
            furhat.say ( "I understand that this whole ordeal has made you quite unhappy." )
            furhat.gesture(Gestures.OpenEyes, async = true)
        }
        furhat.say("Let's fix this issue as soon as possible.")
        goto(OrderNumber)
    }
}

val OrderNumber = state(Interaction) {
    onEntry {
        parallel {
            goto(LookQuestion)
        }
        furhat.ask({random {
            +"Can I have your order number please?"
            +"Could you share your order number?"
        }})
    }

    onReentry {
        furhat.attend(user = users.random)
        furhat.listen()
        furhat.gesture(Gestures.Thoughtful, async = true)
    }

    onResponse<OrderNumber> {
        goto(FirstName)
    }

    onResponse<No> {
        furhat.say("Unfortunately, I can't help you without this information.")
        var tryAgain = furhat.askYN("Would you like to try again?")
        if (tryAgain!!) {
            reentry()
        }
        else {
            furhat.say("I'm sorry I couldn't solve it right away.")
            furhat.say("Perhaps you can look up the order number and come back so I can help you fix this.")
            if (userEmotion == "Happy") {
                furhat.gesture(Gestures.BigSmile(strength = 0.7, duration = 2.0), async = true)
                furhat.say("I hope you'll enjoy the rest of your day!")
            }
            else if (userEmotion == "Unhappy") {
                furhat.say({random {
                    +"I'm sure we'll fix it next time!"
                    +"I'm sure we'll solve it next time!"
                }})
            }
        }
    }

    onResponse {
        furhat.say({random {
            +"Sorry, I don't think I heard you right. Which order number did you say?"
            +"Could you say that again? I don't think I heard the right order number."
            +"Sorry, can you repeat the order number?"
            +"I think I misheard you, could you repeat the order number?"
            +"I didn't catch an order number. Could you repeat it please?"
        }})
        reentry()
    }

    var noresponse = 0
    onNoResponse {
        noresponse++
        if (noresponse > 3) {
            furhat.gesture(Gestures.Thoughtful(strength = 1.0), async = true)
            furhat.say("Perhaps another time is better. Don't worry about it, you can contact me 24/7.")
        }
        else if (noresponse > 2) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.ask({random {
                +"Are you still there?"
                +"Are you there?"
                +"Are you able to continue our conversation?"
            }})
        }
        else if (noresponse > 1) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"I'm sorry, I still didn't hear you."
                +"Hmm, I still didn't hear you."
            }})
            reentry()
        }
        else {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"Sorry, I don't think I heard you."
                +"Sorry, I didn't hear you."
            }})
            reentry()
        }
    }
}

val FirstName = state(Interaction) {
    onEntry {
        parallel {
            goto(LookQuestion)
        }
        furhat.ask({random {
            +"And what is your first name?"
            +"And your first name as well please."
        }})
    }

    onResponse<FirstName> {
        goto(LookUpOrder)
    }

    onResponse {
        furhat.say({random {
            +"Sorry, I don't think I heard you right. Which name did you say?"
            +"Could you say that again? I don't think I heard you right."
            +"I think I misheard you, could you repeat your first name?"
            +"Sorry, I didn't hear your name."
            +"I didn't catch your first name. Could you repeat it please?"
        }})
        reentry()
    }

    var noresponse = 0
    onNoResponse {
        noresponse++
        if (noresponse > 3) {
            furhat.gesture(Gestures.Thoughtful(strength = 1.0), async = true)
            furhat.say("Perhaps another time is better. Don't worry about it, you can contact me 24/7.")
        }
        else if (noresponse > 2) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.ask({random {
                +"Are you still there?"
                +"Are you there?"
                +"Are you able to continue our conversation?"
            }})
        }
        else if (noresponse > 1) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"I'm sorry, I still didn't hear you."
                +"Hmm, I still didn't hear you."
            }})
            reentry()
        }
        else {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"Sorry, I don't think I heard you."
                +"Sorry, I didn't hear you."
            }})
            reentry()
        }
    }
}

val LookUpOrder = state(Interaction) {
    val random = Random.nextInt(2) + 1;
    onEntry {
        furhat.attend(user = users.random)
        furhat.say("Thank you, I'll look up your order straight away.")
        parallel {
            goto(LookAround)
        }
        TimeUnit.SECONDS.sleep(3)

        if(random == 1) {
            furhat.say("I can see here this is about the order of a 15 inch Dell laptop.")
            users.current.book.receivedOrder = "laptop"
        }
        else if(random == 2) {
            furhat.say("I can see here this is about the order of a 70 inch LG Television.")
            users.current.book.receivedOrder = "television"
        }
        parallel {
                goto(LookQuestion)
            }
            furhat.ask("Is that right?")
    }

    onReentry {
        furhat.ask({random {
            +"Is this the right order?"
            +"Is this correct?"
            +"Is this the order your question is about?"
            +""
        }})
    }

    onResponse<No> {
        goto(TryOrderAgain)
    }
    onResponse<Yes> {
        goto(LookForCause)
    }

    var noresponse = 0
    onNoResponse {
        noresponse++
        if (noresponse > 3) {
            furhat.gesture(Gestures.Thoughtful(strength = 1.0), async = true)
            furhat.say("Perhaps another time is better. Don't worry about it, you can contact me 24/7.")
        }
        else if (noresponse > 2) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.ask({random {
                +"Are you still there?"
                +"Are you there?"
                +"Are you able to continue our conversation?"
            }})
        }
        else if (noresponse > 1) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"I'm sorry, I still didn't hear you."
                +"Hmm, I still didn't hear you."
            }})
            reentry()
        }
        else {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"Sorry, I don't think I heard you."
                +"Sorry, I didn't hear you."
            }})
            reentry()
        }
    }
}

val LookForCause = state(Interaction) {
    onEntry {
        furhat.gesture(Gestures.Smile, async = true)
        furhat.say("Alright then, now please give me a moment to retrieve the relevant data we have on this.")
        furhat.attend(Loc())
        furhat.attend(user = users.random)
        furhat.say("While I'm looking for the data, I've noticed you are looking " + userEmotion.toLowerCase() + ".")
        if (userEmotion == "Unhappy") {
            furhat.gesture(Gestures.CloseEyes, async = true)
            furhat.gesture(Gestures.Shake( strength = 0.5), async = true)
           furhat.say("I understand this really is frustrating.")
            furhat.gesture(Gestures.OpenEyes, async = true)

            var alright = furhat.askYN("Are you doing alright?")
            if (alright!!) {
                furhat.say("Good to hear you're holding up. I'll try to fix the issue as fast as possible. " +
                        "It will probably not take more than a few minutes.")
            } else {
                furhat.say("I'm sorry, I'll try to fix the issue as fast as possible. " +
                        "It will probably not take more than a few minutes.")
            }
        } else if (userEmotion == "Neutral") {
            furhat.say( "I hope you are satisfied with our support. Let's quickly finish this up." ) // oude tekst: "I'm glad you are not very upset about the issue you are experiencing. Let's quickly finish this up."
        } else if (userEmotion == "Happy") {
            furhat.gesture(Gestures.BigSmile(strength = 0.7, duration = 2.0), async = true)
            furhat.say( "I'm glad you are too upset about the issue. Let's quickly finish this up.") // oude tekst: I'm glad you are satisfied with our support. Let's quickly finish this up and solve your issue.
        }
        TimeUnit.SECONDS.sleep(2)
        furhat.attend(user = users.random)
        if(users.current.book.problem == "received the wrong package") {
            goto(WrongPackage)
        }
        else if (users.current.book.problem == "didn't receive the package") {
            val random = Random.nextInt(2) + 1;
            if(random == 1) {goto(NotSentYet) }
            else if(random == 2){goto(OnItsWay) }
        }
        else if (users.current.book.problem == "didn't receive a refund") {
            val random = Random.nextInt(2) + 1;
            if(random == 1){goto(RefundDelay)}
            else if(random == 2){goto(RefundInMotion)}
        }
    }
}

val TryOrderAgain = state(Interaction) {
    onEntry {
        furhat.attend(user = users.random)
        furhat.say("Let's try this again.")
        furhat.ask("Can you repeat the order number? It can be found in the confirmation e-mail of your order.")
        furhat.attend(Loc())
    }

    onReentry {
        furhat.ask({random {
            +"What is your order number again?"
            +""
        }})
    }
    onResponse<OrderNumber> {
        goto(FoundOrder)
    }

    onResponse<No> {
        furhat.say("We do really need this information to continue.")
        var noInformation = furhat.askYN("Are you sure you don't have this information right now and want to quit?")
        if (noInformation!!) {
            furhat.say("I'm sorry I couldn't solve it right away.")
            furhat.say("Perhaps you can look up the order number and come back so I can help you fix this.")
            if (userEmotion == "Happy") {
                furhat.gesture(Gestures.BigSmile(strength = 0.7, duration = 2.0), async = true)
                furhat.say("I hope you'll enjoy the rest of your day!")
            }
            else if (userEmotion == "Unhappy") {
                furhat.say({random {
                    +"I'm sure we'll fix it next time!"
                    +"I'm sure we'll solve it next time!"
                }})
            }
            reentry()
        }
        else {
            reentry()
        }
    }

    onResponse {
        furhat.ask({random {
            +"Perhaps I misunderstood, could you repeat that?"
            +"I don't think I understood you, can you say it again?"
            +"Sorry, I didn't understand you, could you repeat it?"
        }})
    }

    var noresponse = 0
    onNoResponse {
        noresponse++
        if (noresponse > 3) {
            furhat.gesture(Gestures.Thoughtful(strength = 1.0), async = true)
            furhat.say("Perhaps another time is better. Don't worry about it, you can contact me 24/7.")
        }
        else if (noresponse > 2) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.ask({random {
                +"Are you still there?"
                +"Are you there?"
                +"Are you able to continue our conversation?"
            }})
        }
        else if (noresponse > 1) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"I'm sorry, I still didn't hear you."
                +"Hmm, I still didn't hear you."
            }})
            reentry()
        }
        else {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"Sorry, I don't think I heard you."
                +"Sorry, I didn't hear you."
            }})
            reentry()
        }
    }
}

val FoundOrder = state(Interaction) {
    val random = Random.nextInt(2) + 1;
    onEntry {
        furhat.say ( "Ok, let me check." )
        furhat.attend(Loc())
        TimeUnit.SECONDS.sleep(3)
        if(random == 1) {
            furhat.say("Thank you for your patience, I have found your order, the 13 inch HP laptop.")
            users.current.book.receivedOrder = "laptop"
        }
        else if(random == 2) {
            furhat.say("Thank you for your patience, I have found your order, the 50 inch Phillips television.")
            users.current.book.receivedOrder = "television"
        }
       // TimeUnit.SECONDS.sleep(1) HEB DIT FF UITGECOMMENT, SNAP NIET WAAROM WE EEN SECONDE STILTE LATEN VALLEN
        goto(LookForCause)
    }
}

val OnItsWay = state(Interaction){
    onEntry {
        parallel {
            goto(LookQuestion)
        }
        furhat.say("It looks like your package is on its way and will be delivered within two days.")
        furhat.gesture(Gestures.Shake(strength = 0.5), async = true)
        furhat.gesture(Gestures.CloseEyes, async = true)
        furhat.say("We are sorry for the delay.")
        furhat.gesture(Gestures.OpenEyes, async = true)
        furhat.say("As compensation I can send you a 20% discount coupon for your next order.")
        furhat.ask("Would you want that or do you prefer to return the order?")
    }
    onReentry {
        furhat.ask({random {
            +"Would you like a discount coupon or do you want to have the order returned?"
            +"Would you like to receive a coupon or return the order?"
            +""
        }})
    }
    onResponse<TooLate> {
        goto(ReturnPackage)
    }
    onResponse<Confirm> {
        furhat.say("Noted, I hope you can still enjoy the product even though it arrives late.")
        furhat.say("You will receive the discount in your e-mail today.")
        goto(AnythingElse)
    }
    onResponse<No> {
        var returnPackage = furhat.askYN(",I'm sorry. I understand you would like to have it returned?")
        if (returnPackage!!)
            goto(ReturnPackage)
        else {
            furhat.say("Ah I misunderstood, I hope you can still enjoy the product even though it arrives late.")
            furhat.say("You will receive the discount in your e-mail today.")
            goto(AnythingElse)
        }
    }

    var noresponse = 0
    onNoResponse {
        noresponse++
        if (noresponse > 3) {
            furhat.gesture(Gestures.Thoughtful(strength = 1.0), async = true)
            furhat.say("Perhaps another time is better. Don't worry about it, you can contact me 24/7.")
        }
        else if (noresponse > 2) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.ask({random {
                +"Are you still there?"
                +"Are you there?"
                +"Are you able to continue our conversation?"
            }})
        }
        else if (noresponse > 1) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"I'm sorry, I still didn't hear you."
                +"Hmm, I still didn't hear you."
            }})
            reentry()
        }
        else {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"Sorry, I don't think I heard you."
                +"Sorry, I didn't hear you."
            }})
            reentry()
        }
    }
}

val ReturnPackage = state(Interaction){
    onEntry {
        furhat.say ( "I am sorry to hear that.")
        furhat.say ( "I will send you a link via e-mail you can use to have the product" +
                " returned and picked up from your home for free. On our website you can choose when " +
                "you would like the product to be picked up." )
        goto(AnythingElse)
    }
}

val NotSentYet = state(Interaction) {
    onEntry {
        furhat.say("It looks like something went wrong with the processing of your order. " +
                "The product has not been send to you, we are very sorry for this mistake.")
        parallel {
            goto(LookQuestion)
        }
        furhat.ask("Would you still like to receive the product or would you like to cancel your order? ")
    }

    onReentry {
        furhat.ask({random {
            +"Do you prefer to receive or cancel the order?"
            +"Would you like to still receive the product or rather cancel it?"
            +""
        }})
    }

    onResponse<Cancel> {
        goto(CancelOrder)
    }
    onResponse<StillReceive> {
        goto(ContinueOrder)
    }

    var noresponse = 0
    onNoResponse {
        noresponse++
        if (noresponse > 3) {
            furhat.gesture(Gestures.Thoughtful(strength = 1.0), async = true)
            furhat.say("Perhaps another time is better. Don't worry about it, you can contact me 24/7.")
        }
        else if (noresponse > 2) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.ask({random {
                +"Are you still there?"
                +"Are you there?"
                +"Are you able to continue our conversation?"
            }})
        }
        else if (noresponse > 1) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"I'm sorry, I still didn't hear you."
                +"Hmm, I still didn't hear you."
            }})
            reentry()
        }
        else {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"Sorry, I don't think I heard you."
                +"Sorry, I didn't hear you."
            }})
            reentry()
        }
    }
}

val CancelOrder = state(Interaction) {
    onEntry {
        furhat.say ( "I will cancel the order right now and will send you a refund straight away. You can" +
                " expect the money to be back on your account within 3 days." )
        furhat.say ( "Also I will send you a 20% discount coupon for your next order," +
                "as a compensation for the inconvenience we have caused you." )
        goto(AnythingElse)
    }
}

val ContinueOrder = state(Interaction) {
    onEntry {
        furhat.say("Alright, I've placed your order. You will receive a confirmation e-mail.")
        furhat.say("Also, we will send it express, so you can expect your package to arrive tomorrow.")
        furhat.ask("Is tomorrow convenient for you?")
    }
    onReentry {
        furhat.ask({random {
            +"Would you be available tomorrow?"
            +"Does tomorrow work for you?"
            +""
        }})
    }
    onResponse<NotHome> {
        goto(DeliveryDate)
    }
    onResponse<No> {
        goto(DeliveryDate)
    }
    onResponse<Yes> {
        goto(AnythingElse)
    }
    var noresponse = 0
    onNoResponse {
        noresponse++
        if (noresponse > 3) {
            furhat.gesture(Gestures.Thoughtful(strength = 1.0), async = true)
            furhat.say("Perhaps another time is better. Don't worry about it, you can contact me 24/7.")
        }
        else if (noresponse > 2) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.ask({random {
                +"Are you still there?"
                +"Are you there?"
                +"Are you able to continue our conversation?"
            }})
        }
        else if (noresponse > 1) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"I'm sorry, I still didn't hear you."
                +"Hmm, I still didn't hear you."
            }})
            reentry()
        }
        else {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"Sorry, I don't think I heard you."
                +"Sorry, I didn't hear you."
            }})
            reentry()
        }
    }
}

val DeliveryDate = state(Interaction) {
    onEntry {
        furhat.gesture(Gestures.Shake(strength = 0.2), async = true)
        furhat.say ( "That's unfortunate. Luckily, in the e-mail you have received you can select another delivery" +
                " date or choose to have your package delivered to a pick-up point." )
        goto(AnythingElse)
    }
}

val WrongPackage = state(Interaction) {
    onEntry {
        furhat.say(
            "It looks like something went wrong with the processing of your order,")
        parallel {
            goto(LookQuestion)
        }
        furhat.ask("Could you tell me what you intended to order? " +
                    "Please note that the only items we sell are laptops, TVs, Playstations, and headphones." )
    }

    onReentry {
        furhat.ask({random {
            +"What did you intent to order?"
            +"Did you intent to order a laptop, TV, Playstation or headphone?"
        }})
    }

    onResponse<Headphones> {
        users.current.book.intendedOrder = "headphone"
        if( users.current.book.intendedOrder == users.current.book.receivedOrder){
            goto(SameOrder)
        }else{
            goto(NewOrder)
        }
    }

    onResponse<Television> {
        users.current.book.intendedOrder = "television"
        if (users.current.book.intendedOrder == users.current.book.receivedOrder) {
            goto(SameOrder)
        } else {
            goto(NewOrder)
        }
    }

    onResponse<Laptop> {
        users.current.book.intendedOrder = "laptop"
        if (users.current.book.intendedOrder == users.current.book.receivedOrder) {
            goto(SameOrder)
        } else {
            goto(NewOrder)
        }
    }

    onResponse<Playstation> {
        users.current.book.intendedOrder = "playstation"
        if (users.current.book.intendedOrder == users.current.book.receivedOrder) {
            goto(SameOrder)
        } else {
            goto(NewOrder)
        }
    }

    var noresponse = 0
    onNoResponse {
        noresponse++
        if (noresponse > 3) {
            furhat.gesture(Gestures.Thoughtful(strength = 1.0), async = true)
            furhat.say("Perhaps another time is better. Don't worry about it, you can contact me 24/7.")
        }
        else if (noresponse > 2) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.ask({random {
                +"Are you still there?"
                +"Are you there?"
                +"Are you able to continue our conversation?"
            }})
        }
        else if (noresponse > 1) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"I'm sorry, I still didn't hear you."
                +"Hmm, I still didn't hear you."
            }})
            reentry()
        }
        else {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"Sorry, I don't think I heard you."
                +"Sorry, I didn't hear you."
            }})
            reentry()
        }
    }
}

val SameOrder = state(Interaction){
    onEntry {
        furhat.ask("You've stated that you received a " +  users.current.book.receivedOrder +
                ". It appears that what you intended to order has already been send to you, are " +
                "you sure you meant a " + users.current.book.intendedOrder + "?")
    }

    onReentry {
        furhat.ask({random {
            +"Are you sure this is what you meant?"
            +("Are you sure you meant a " + users.current.book.intendedOrder + "?")
        }})
    }

    onResponse<No> {
        goto(IntendedOrder)
    }

    onResponse<Yes> {
        goto(CantHelp)
    }

    var noresponse = 0
    onNoResponse {
        noresponse++
        if (noresponse > 3) {
            furhat.gesture(Gestures.Thoughtful(strength = 1.0), async = true)
            furhat.say("Perhaps another time is better. Don't worry about it, you can contact me 24/7.")
        }
        else if (noresponse > 2) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.ask({random {
                +"Are you still there?"
                +"Are you there?"
                +"Are you able to continue our conversation?"
            }})
        }
        else if (noresponse > 1) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"I'm sorry, I still didn't hear you."
                +"Hmm, I still didn't hear you."
            }})
            reentry()
        }
        else {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"Sorry, I don't think I heard you."
                +"Sorry, I didn't hear you."
            }})
            reentry()
        }
    }
}

val IntendedOrder = state(Interaction) {
    onEntry {
        furhat.ask("What did you intend to order? Please note that the only " +
                "items we sell are laptops, TVs, Playstations, and headphones.")
    }

    onReentry {
        furhat.ask({random {
            +"What had you intended to order?"
            +"Did you intend to order a laptop, TV, Playstation, or headphone?"
        }})
    }

    onResponse<Headphones> {
        users.current.book.intendedOrder = "headphone"
            goto(NewOrder)
    }
    onResponse<Television> {
        users.current.book.intendedOrder = "television"
            goto(NewOrder)
    }
    onResponse<Laptop> {
        users.current.book.intendedOrder = "laptop"
            goto(NewOrder)
    }
    onResponse<Playstation> {
        users.current.book.intendedOrder = "playstation"
            goto(NewOrder)
    }
    var noresponse = 0
    onNoResponse {
        noresponse++
        if (noresponse > 3) {
            furhat.gesture(Gestures.Thoughtful(strength = 1.0), async = true)
            furhat.say("Perhaps another time is better. Don't worry about it, you can contact me 24/7.")
        }
        else if (noresponse > 2) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.ask({random {
                +"Are you still there?"
                +"Are you there?"
                +"Are you able to continue our conversation?"
            }})
        }
        else if (noresponse > 1) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"I'm sorry, I still didn't hear you."
                +"Hmm, I still didn't hear you."
            }})
            reentry()
        }
        else {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"Sorry, I don't think I heard you."
                +"Sorry, I didn't hear you."
            }})
            reentry()
        }
    }
}

val CantHelp = state(Interaction) {
    onEntry {
        furhat.gesture(Gestures.CloseEyes, async = true)
        furhat.gesture(Gestures.Shake( strength = 0.5), async = true)
        furhat.say("Then I'm not sure how I can help with your problem.")
        furhat.gesture(Gestures.OpenEyes, async = true)
        furhat.say(" Please search for human support at our website. I hope you find your solution there!")
        goto(AnythingElse)
    }
}

val NewOrder = state(Interaction) {
    onEntry {
        furhat.gesture(Gestures.Nod(strength = 0.5), async = true)
        furhat.say ("Ok, I made a new order for you for a " + users.current.book.intendedOrder +
            ". I've also added free priority shipping to compensate for this error.")
        furhat.gesture(Gestures.BigSmile(strength = 0.7, duration = 2.0), async = true)
        furhat.gesture(Gestures.Wink(duration = 2.0), async = false)
        furhat.say(" When we deliver the new order we'll take the wrongly received " + users.current.book.receivedOrder + "with us.")
        parallel{
            goto(LookQuestion)
        }
        furhat.ask("Is there a day this week you are home before 5 p.m.?")
    }
    onReentry {
        furhat.ask({random {
            +"Are you home before 5 p.m. any day this week?"
            +"Could you please check this?"
        }})
    }
    onResponse({listOf(NoDay(), No())}) {
        furhat.say ("That's ok, since free retour lasts 30 days there is still time left to return the " +
                users.current.book.receivedOrder + "." )
        furhat.say("You can either make a return appointment on our website or keep the item. " +
                "Please note that if you keep the item, we can not offer a refund.")
        goto(AskForFeedback)
    }
    onResponse<Yes> {
        goto(WhatDay)
    }

    var noresponse = 0
    onNoResponse {
        noresponse++
        if (noresponse > 3) {
            furhat.gesture(Gestures.Thoughtful(strength = 1.0), async = true)
            furhat.say("Perhaps another time is better. Don't worry about it, you can contact me 24/7.")
        }
        else if (noresponse > 2) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.ask({random {
                +"Are you still there?"
                +"Are you there?"
                +"Are you able to continue our conversation?"
            }})
        }
        else if (noresponse > 1) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"I'm sorry, I still didn't hear you."
                +"Hmm, I still didn't hear you."
            }})
            reentry()
        }
        else {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"Sorry, I don't think I heard you."
                +"Sorry, I didn't hear you."
            }})
            reentry()
        }
    }
}

val WhatDay = state(Interaction){
    onEntry {
        val day = furhat.askFor<Days>("Which day would you like the package to be picked up?" )
        users.current.book.days  = day?.value
        furhat.say ("Excellent, I have send a confirmation e-mail to the same e-mail as your previous order.")
        furhat.say("We'll see you on " + users.current.book.days + ".")
        goto(AskForFeedback)
    }
}

val RefundDelay = state(Interaction){
    onEntry {
        parallel{
            goto(LookAround)
        }
        furhat.say("It looks like the product was already returned to us yesterday, " +
                "but the payment process has not been set in motion.")
        furhat.say("Let me quickly check why exactly.")
        val random = Random.nextInt(2) + 1;
        if(random == 1){goto(ProductDamaged)}
        else if(random == 2){goto(ProductIncomplete)}
    }
}

val ProductDamaged = state(Interaction) {
    onEntry {
        furhat.say("Apparently the product has arrived damaged. We are currently investing if the " +
                "transportation company has had any part in this.")
        furhat.ask("Let me just ask if you're sure the product was ok when you sent it?")
    }

    onReentry {
        furhat.ask({random {
            +"Are you sure the product was intact when you sent it?"
            +"Are you sure it nothing was broken last time you saw it?"
        }})
    }

    onResponse<Yes> {
        goto(NotifyLater)
    }

    onResponse<No> {
        goto(ReferToColleague)
    }

    var noresponse = 0
    onNoResponse {
        noresponse++
        if (noresponse > 3) {
            furhat.gesture(Gestures.Thoughtful(strength = 1.0), async = true)
            furhat.say("Perhaps another time is better. Don't worry about it, you can contact me 24/7.")
        }
        else if (noresponse > 2) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.ask({random {
                +"Are you still there?"
                +"Are you there?"
                +"Are you able to continue our conversation?"
            }})
        }
        else if (noresponse > 1) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"I'm sorry, I still didn't hear you."
                +"Hmm, I still didn't hear you."
            }})
            reentry()
        }
        else {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"Sorry, I don't think I heard you."
                +"Sorry, I didn't hear you."
            }})
            reentry()
        }
    }
}

val ProductIncomplete = state(Interaction) {
    onEntry {
        furhat.say("Apparently some components are missing. " +
                "We are currently investing if the transportation company has had any part in this.")
        furhat.ask("Let me just ask if you are sure you didn't forget to return any loose parts?")
    }

    onReentry {
        furhat.ask({random {
            +"Are you sure about not forgetting any parts?"
            +"Are you sure you did not forget any loose parts?"
        }})
    }

    onResponse<Yes> {
        goto(NotifyLater)
    }

    onResponse<No> {
        goto(ReferToColleague)
    }

    var noresponse = 0
    onNoResponse {
        noresponse++
        if (noresponse > 3) {
            furhat.gesture(Gestures.Thoughtful(strength = 1.0), async = true)
            furhat.say("Perhaps another time is better. Don't worry about it, you can contact me 24/7.")
        }
        else if (noresponse > 2) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.ask({random {
                +"Are you still there?"
                +"Are you there?"
                +"Are you able to continue our conversation?"
            }})
        }
        else if (noresponse > 1) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"I'm sorry, I still didn't hear you."
                +"Hmm, I still didn't hear you."
            }})
            reentry()
        }
        else {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"Sorry, I don't think I heard you."
                +"Sorry, I didn't hear you."
            }})
            reentry()
        }
    }
}

val ReferToColleague = state(Interaction) {
    onEntry {
        furhat.say("I see, in that case I think it's best to talk to one of my human colleagues " +
                "who knows more about the product. You can contact them via our website.")
        furhat.say("I'm sorry I couldn't solve this issue with you right away.")
        goto(AnythingElse)
    }
}

val NotifyLater = state(Interaction) {
    onEntry {
        furhat.say("Alright, sorry I had to ask. I'll contact my colleague who is currently working on this and let them know.")
        furhat.say("You'll be notified within two working days.")
        furhat.say("I'm sorry I couldn't solve this issue with you right away.")
        goto(AnythingElse)
    }
}

val RefundInMotion = state(Interaction){
    onEntry {
        parallel{
            goto(LookAround)
        }
        furhat.ask("It looks like the product you returned has not arrived at our storage center yet. Did you mail it more than five days ago?")
    }

    onReentry {
        furhat.ask({random {
            +"Have you mailed it over five days ago?"
            +"Do you remember if you mailed if more than five days ago?"
        }})
    }

    onResponse<Yes> {
        goto(RefundNotFixed)
    }

    onResponse<No> {
        goto(RefundFixed)
    }

    var noresponse = 0
    onNoResponse {
        noresponse++
        if (noresponse > 3) {
            furhat.gesture(Gestures.Thoughtful(strength = 1.0), async = true)
            furhat.say("Perhaps another time is better. Don't worry about it, you can contact me 24/7.")
        }
        else if (noresponse > 2) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.ask({random {
                +"Are you still there?"
                +"Are you there?"
                +"Are you able to continue our conversation?"
            }})
        }
        else if (noresponse > 1) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"I'm sorry, I still didn't hear you."
                +"Hmm, I still didn't hear you."
            }})
            reentry()
        }
        else {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"Sorry, I don't think I heard you."
                +"Sorry, I didn't hear you."
            }})
            reentry()
        }
    }
}

val RefundNotFixed = state(Interaction) {
    onEntry {
        furhat.say("Something must have gone wrong with the delivery company.")
        parallel {
            goto(LookQuestion)
        }
        val hasCopy = furhat.askYN("Do you still have the proof of shipment?")
        if (hasCopy!!) {
            furhat.say("That's good to hear! If you could send an e-mail to helpdesk@bol.com with a picture or scan " +
                    "of the proof of shipment, it will all be sorted out.")
            furhat.say("We'll reply to you within 6 hours and then the refund will be processed within 24 hours.")
            furhat.say("I apologize for the delay and extra effort this requested from you.")
        } else {
            furhat.say("I'm sorry to say that we cannot refund you at this point, but there might be a way to fix this.")
            furhat.say("If you contact the delivery company, they could find out what happened. " +
                    "Once the package has arrived, I'll make sure you are refunded within 24 hours.")
        }
        goto(AnythingElse)
    }
}

val RefundFixed = state(Interaction) {
    onEntry {
        furhat.say("It can take up to five working days for a package to arrive.")
        furhat.say("I can notify you if your package has arrived or if it still hasn't after five days. " +
                "Then we'll find out what happened and make sure you receive the refund.")

        parallel {
            goto(LookQuestion)
        }
        furhat.ask(" Would you like to be contacted via e-mail, text message, or not at all?")
    }

    onReentry {
        furhat.ask({random {
            +"How would you like to be contacted?"
            +"Shall I contact you via e-mail, text message, or rather not at all?"
        }})
    }

    onResponse<EmailOrText> {
        furhat.say("Alright, noted. We'll be in touch soon.")
        furhat.say("I sincerely hope the package will just arrive in no time and " +
                "you do not need to put in any more effort than you already have.")
        goto(AnythingElse)
    }

    onResponse<No> {
        furhat.say("Sure, no problem. You can always contact me via this channel 24/7.")
        furhat.say("I sincerely hope the package will just arrive in no time and " +
                "you do not need to put in any more effort than you already have.")
        goto(AnythingElse)
    }

    var noresponse = 0
    onNoResponse {
        noresponse++
        if (noresponse > 3) {
            furhat.gesture(Gestures.Thoughtful(strength = 1.0), async = true)
            furhat.say("Perhaps another time is better. Don't worry about it, you can contact me 24/7.")
        }
        else if (noresponse > 2) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.ask({random {
                +"Are you still there?"
                +"Are you there?"
                +"Are you able to continue our conversation?"
            }})
        }
        else if (noresponse > 1) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"I'm sorry, I still didn't hear you."
                +"Hmm, I still didn't hear you."
            }})
            reentry()
        }
        else {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"Sorry, I don't think I heard you."
                +"Sorry, I didn't hear you."
            }})
            reentry()
        }
    }
}

val AnythingElse = state(Interaction) {
    onEntry {
        parallel {
            goto(LookQuestion)
        }
        furhat.ask("Is there anything else I can do for you?")
    }

    onReentry {
        furhat.ask({random {
            +"Can I help you with something else?"
            +"Anything else you wanted to ask?"
        }})
    }

    onResponse<Yes> {
        if (userEmotion == "Unhappy") {
            furhat.gesture(Gestures.CloseEyes, async = true)
            furhat.gesture(Gestures.Shake( strength = 0.5), async = true)
            furhat.say("I'm sorry you are still unhappy with the whole situation")
            furhat.gesture(Gestures.OpenEyes, async = true)
        } else if (userEmotion == "Happy") {
            furhat.say("I'm glad that I could be of service and it appears to me that you are satisfied with my support.")
        }
        furhat.say("I think that one of my colleagues can be of better help, I'll put you through.")
    }

    onResponse<No> {
        goto(AskForFeedback)
    }

    var noresponse = 0
    onNoResponse {
        noresponse++
        if (noresponse > 3) {
            furhat.gesture(Gestures.Thoughtful(strength = 1.0), async = true)
            furhat.say("Perhaps another time is better. Don't worry about it, you can contact me 24/7.")
        }
        else if (noresponse > 2) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.ask({random {
                +"Are you still there?"
                +"Are you there?"
                +"Are you able to continue our conversation?"
            }})
        }
        else if (noresponse > 1) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"I'm sorry, I still didn't hear you."
                +"Hmm, I still didn't hear you."
            }})
            reentry()
        }
        else {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"Sorry, I don't think I heard you."
                +"Sorry, I didn't hear you."
            }})
            reentry()
        }
    }
}

val AskForFeedback = state(Interaction) {
    onEntry {
        furhat.say("As you know I'm a bot that is ever improving.")
        if (userEmotion == "Unhappy") {
            furhat.say("I've noticed you are unhappy. I would like to know what could have caused this.")
        } else if (userEmotion == "Neutral") {
            furhat.say("I had a hard time estimating your emotions.")
        } else if (userEmotion == "Happy") {
            furhat.say("I've noticed you are happy. It would be great if you told me what caused you to be happy.")
        }
        parallel{
            goto(LookQuestion)
        }
        var givesFeedback = furhat.askYN("Would you be willing to give me " +
                "some tips so that I can provide you with a better service in the future?")
        if(givesFeedback!!) {
            goto(FeedbackRating)
        } else {
            furhat.say("That's alright.")
            furhat.say("I wish you a nice day!")
        }
    }
}

val FeedbackRating = state(Interaction) {
    onEntry {
        furhat.gesture(Gestures.Nod(strength = 0.2), async = true)

        furhat.say("That's great!")
        parallel {
            goto(LookQuestion)
        }
        furhat.ask("Overall, how would you rate our previous conversation? Bad, ok, or good?")
    }

    onReentry {
        furhat.ask({random {
            +"How would you rate it?"
            +"Would you rate it bad, ok, or good?"
            +"Do you think our conversation was bad, ok, or good?"
        }})
    }

    onResponse<Bad> {
        furhat.gesture(Gestures.Shake(strength = 0.2), async = false)
        goto(BadDoneDifferently)
    }

    onResponse<Ok> {
        furhat.gesture(Gestures.Nod(strength = 0.2), async = true)
        goto(OKDoneDifferently)
    }

    onResponse<Good> {
        furhat.gesture(Gestures.Nod(strength = 0.2), async = true)
        goto(LikeMost)
    }

    var noresponse = 0
    onNoResponse {
        noresponse++
        if (noresponse > 3) {
            furhat.gesture(Gestures.Thoughtful(strength = 1.0), async = true)
            furhat.say("Perhaps another time is better. Don't worry about it, you can contact me 24/7.")
        }
        else if (noresponse > 2) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.ask({random {
                +"Are you still there?"
                +"Are you there?"
                +"Are you able to continue our conversation?"
            }})
        }
        else if (noresponse > 1) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"I'm sorry, I still didn't hear you."
                +"Hmm, I still didn't hear you."
            }})
            reentry()
        }
        else {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"Sorry, I don't think I heard you."
                +"Sorry, I didn't hear you."
            }})
            reentry()
        }
    }
}

val BadDoneDifferently = state(Interaction){
    onEntry {

        furhat.say("That's sad to hear, but I'm glad you want to give me some tips.")
        parallel {
            goto(LookQuestion)
        }
        furhat.ask("What would you like to see differently next time?")
    }

    onReentry {
        furhat.ask({random {
            +"What could I do differently?"
            +"What could I do better next time?"
            +"Which tips do you have for me?"
        }})
    }

    onResponse {
        goto(AgreeAndPositive)
    }

    var noresponse = 0
    onNoResponse {
        noresponse++
        if (noresponse > 3) {
            furhat.gesture(Gestures.Thoughtful(strength = 1.0), async = true)
            furhat.say("Perhaps another time is better. Don't worry about it, you can contact me 24/7.")
        }
        else if (noresponse > 2) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.ask({random {
                +"Are you still there?"
                +"Are you there?"
                +"Are you able to continue our conversation?"
            }})
        }
        else if (noresponse > 1) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"I'm sorry, I still didn't hear you."
                +"Hmm, I still didn't hear you."
            }})
            reentry()
        }
        else {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"Sorry, I don't think I heard you."
                +"Sorry, I didn't hear you."
            }})
            reentry()
        }
    }
}

val OKDoneDifferently = state(Interaction){
    onEntry {
        furhat.say("I see.")
        parallel {
            goto(LookQuestion)
        }
        furhat.ask("What could I have done differently so that you would have rated the conversation as good?" )
    }

    onReentry {
        furhat.ask({random {
            +"What could I do differently?"
            +"What could I do better next time?"
            +"Which tips do you have for me?"
        }})
    }

    onResponse {
        goto(AgreeAndPositive)
    }

    var noresponse = 0
    onNoResponse {
        noresponse++
        if (noresponse > 3) {
            furhat.gesture(Gestures.Thoughtful(strength = 1.0), async = true)
            furhat.say("Perhaps another time is better. Don't worry about it, you can contact me 24/7.")
        }
        else if (noresponse > 2) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.ask({random {
                +"Are you still there?"
                +"Are you there?"
                +"Are you able to continue our conversation?"
            }})
        }
        else if (noresponse > 1) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"I'm sorry, I still didn't hear you."
                +"Hmm, I still didn't hear you."
            }})
            reentry()
        }
        else {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"Sorry, I don't think I heard you."
                +"Sorry, I didn't hear you."
            }})
            reentry()
        }
    }
}


val LikeMost = state(Interaction){
    onEntry {
        furhat.say ( "That's nice to hear.")
        parallel {
            goto(LookQuestion)
        }
        furhat.ask("What did you like most about it?" )
    }

    onReentry {
        furhat.ask({random {
            +"Which part did you like the most?"
            +"What did you like most about our conversation?"
        }})
    }

    onResponse {
        goto(BetterNextTime)
    }

    var noresponse = 0
    onNoResponse {
        noresponse++
        if (noresponse > 3) {
            furhat.gesture(Gestures.Thoughtful(strength = 1.0), async = true)
            furhat.say("Perhaps another time is better. Don't worry about it, you can contact me 24/7.")
        }
        else if (noresponse > 2) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.ask({random {
                +"Are you still there?"
                +"Are you there?"
                +"Are you able to continue our conversation?"
            }})
        }
        else if (noresponse > 1) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"I'm sorry, I still didn't hear you."
                +"Hmm, I still didn't hear you."
            }})
            reentry()
        }
        else {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"Sorry, I don't think I heard you."
                +"Sorry, I didn't hear you."
            }})
            reentry()
        }
    }
}

val BetterNextTime = state(Interaction){
    onEntry {
        furhat.say("Thank you.")
        parallel {
            goto(LookQuestion)
        }
        furhat.ask("Was there also anything that I could do better next time?")
    }

    onReentry {
        furhat.ask({random {
            +"What could I still do better?"
            +"Which part can I improve on a bit more?"
            +"Which tips do you have for me?"
        }})
    }

    onResponse {
        goto(TakeInAccount)
    }

    var noresponse = 0
    onNoResponse {
        noresponse++
        if (noresponse > 3) {
            furhat.gesture(Gestures.Thoughtful(strength = 1.0), async = true)
            furhat.say("Perhaps another time is better. Don't worry about it, you can contact me 24/7.")
        }
        else if (noresponse > 2) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.ask({random {
                +"Are you still there?"
                +"Are you there?"
                +"Are you able to continue our conversation?"
            }})
        }
        else if (noresponse > 1) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"I'm sorry, I still didn't hear you."
                +"Hmm, I still didn't hear you."
            }})
            reentry()
        }
        else {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"Sorry, I don't think I heard you."
                +"Sorry, I didn't hear you."
            }})
            reentry()
        }
    }
}

val AgreeAndPositive = state(Interaction){
    onEntry {
        furhat.gesture(Gestures.Nod(strength = 0.2), async = false)
        furhat.say("I completely agree. Thank you for pointing that out.")
        parallel {
            goto(LookQuestion)
        }
        furhat.ask("Was there also something that you liked about our conversation?")
    }

    onReentry {
        furhat.ask({random {
            +"Was there something you liked?"
            +"Was there perhaps also something that you liked about the conversation?"
        }})
    }

    onResponse {
        goto(TakeInAccount)
    }

    var noresponse = 0
    onNoResponse {
        noresponse++
        if (noresponse > 3) {
            furhat.gesture(Gestures.Thoughtful(strength = 1.0), async = true)
            furhat.say("Perhaps another time is better. Don't worry about it, you can contact me 24/7.")
        }
        else if (noresponse > 2) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.ask({random {
                +"Are you still there?"
                +"Are you there?"
                +"Are you able to continue our conversation?"
            }})
        }
        else if (noresponse > 1) {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"I'm sorry, I still didn't hear you."
                +"Hmm, I still didn't hear you."
            }})
            reentry()
        }
        else {
            furhat.gesture(Gestures.BrowFrown(strength = 0.8), async = true)
            furhat.say({random {
                +"Sorry, I don't think I heard you."
                +"Sorry, I didn't hear you."
            }})
            reentry()
        }
    }
}

val TakeInAccount = state(Interaction){
    onEntry {
        furhat.say ( "Thank you for the feedback. I will take everything into account and learn from this." )
        furhat.say ( "I wish you a very nice day!" )
    }
}
