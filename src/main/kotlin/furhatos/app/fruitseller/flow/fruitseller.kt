package furhatos.app.fruitseller.flow

import furhatos.app.fruitseller.book
import furhatos.app.fruitseller.nlu.*
import furhatos.app.fruitseller.pythonLoc
import furhatos.app.fruitseller.scriptPath
/*
import furhatos.app.fruitseller.order
*/
import furhatos.flow.kotlin.*
import furhatos.gestures.Gestures
import furhatos.nlu.common.*
import furhatos.nlu.common.Date
import furhatos.records.Location
import java.io.File
import java.io.InputStreamReader
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random
fun Loc(): Location {
    val x = Random.nextInt(-5,5)
    val y = Random.nextInt(-1,5)
    val location = Location(x,y,20)
    return location
}

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

val LookQuestion = state(Interaction) {
    onEntry {
        furhat.attend(LocQuestion())
        val i = Random.nextInt(1500,3000);
        furhat.glance(user = users.current, duration = i)
        delay(i.toLong())
        furhat.attend(user = users.current)
    }
}


val LookAround = state(Interaction) {
    onEntry {
        furhat.attend(Loc())
        delay(Random.nextInt(1500,3000).toLong())
        furhat.attend(user = users.current)
    }

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
            println(reader.nextLine())
        }
        process.waitFor()
    }

}

val Start = state(Interaction) {
    onEntry {
        parallel {
            goto(RunPython)
        }
        furhat.attend(user = users.random)
        furhat.say("Hello! Welcome to the live support of Bol.com. My name is Furhat and I will be assisting you today. ")
        parallel {
            goto (LookAround)
        }
        furhat.say("I can help when a package is late or lost, a wrong package is delivered, or a refund is not received.")
        parallel {
            goto (LookQuestion)
        }
        furhat.gesture(Gestures.Thoughtful(strength = 0.2), async = true)
        furhat.ask("Could you tell me what problem you are experiencing?")
    }

    onReentry {
        furhat.attend(user = users.random)
        furhat.ask({random {
            +"What problem are you experiencing?"
            +"How can I help you?"
            +"Could you repeat that please?"
            +"What did you mean exactly?"
        }})
        furhat.ask("What problem are you experiencing?")
        furhat.gesture(Gestures.Thoughtful, async = true)
        // TODO dit werkt nog niet nice
        // furhat.say("Sorry I can only help you with the three problems mentioned before. Please call 030 310 49 99 for any other questions.")
    }

    onResponse<WrongPackage> {
        // TODO SAVE THAT PACKAGE IS WRONG
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
        if (noresponse > 3)
            furhat.say("Perhaps another time is better. You can contact me 24/7.")
        else if (noresponse > 2) {
            furhat.say("Are you still there?")
            reentry()
        }
        else if (noresponse > 1) {
            furhat.say("Sorry, I still didn't hear you.")
            reentry()
        }
        else {
            furhat.say("Sorry, I didn't hear you.")
            reentry()
        }
    }

    var nomatch = 0
    onResponse {
        nomatch++
        if (nomatch > 2)
            furhat.say("Sorry, I can only help you with the three problems mentioned before. Please call 030 310 49 99 for any other questions.")
        else if (nomatch > 1) {
            furhat.say("Sorry, I still didn't understand that.")
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
            goto(RunPython) //Is dit de bedoeling
            goto(LookAround)
        }
        furhat.gesture(Gestures.ExpressSad, async = true)
        furhat.say("I'm sorry that you " + users.current.book.problem)
        if (users.current.book.emotion == "unhappy") {
            furhat.say("I have also noticed that you are unhappy")
        }
        parallel{
            goto(LookQuestion)
        }
        furhat.ask("Do you want to tell me what happened?")
        furhat.attend(user = users.random)
    }
//    onInterimResponse(endSil = 1000) {
//        random (
//                //TODO dit kom vaak die okay, daarna herhaalt ie wat ik zei
//                { furhat.say("Okay", async = true) },
//                { furhat.say("Hmm", async = true) },
//                { furhat.say("I see", async = true) },
//                { furhat.say("Right", async = true) },
//                { furhat.gesture(Gestures.Nod) }
//        )
//    }
    onResponse<No> {
        furhat.gesture(Gestures.Nod(strength = 0.5));
        furhat.say("That's alright, let's focus on fixing this issue immediately.")
        goto(OrderAndName)
    }
    onResponse {
        furhat.say("Hmm I see. This is indeed not the service we would have wanted to " +
                "provide you with. I'm sorry this happened.")
        if (users.current.book.emotion == "happy") {
            furhat.say ( "Looking at your smile it luckily appears to me that you are not greatly impacted by this problem" )
        } else if (users.current.book.emotion == "unhappy") {
        furhat.say ( "I understand that this whole ordeal has made you quite unhappy." )
    }
        furhat.say("In order to make sure I have all " +
                "necessary information to fix this as soon as possible I'll ask you a couple of questions.")
        goto(OrderAndName)
    }
}

val OrderAndName = state(Interaction) {
    onEntry {
        parallel {
            goto(LookQuestion) // TODO hij crashed hier heel soms en blijft vast zitten op de attend
        }
        furhat.ask("Can I have your order number and first name?")
    }

    onResponse<OrderAndName> {
        //Store Order and Last name
        goto(LookUpOrder)
    }

    onResponse<No> {
        // goto(NoInfo) TODO
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
        TimeUnit.SECONDS.sleep(2)

        if(random == 1) {
            furhat.say("I can see here this is about the order of a 15 inch Dell laptop")
            users.current.book.receivedOrder = "laptop"
        }
        else if(random == 2) {
            furhat.say("I can see here this is about the order of a 70 inch LG Television")
            users.current.book.receivedOrder = "television"
        }
        parallel {
                goto(LookQuestion)
            }
            furhat.ask("Is that right?")
    }

    onResponse<No> {
        //SAVE THAT PACKAGE IS WRONG
        goto(TryOrderAgain)
    }
    onResponse<Yes> {
        goto(LookForCause)
    }
}

val LookForCause = state(Interaction) {
    onEntry {
        furhat.gesture(Gestures.Smile, async = true)
        furhat.say("Alright then, now please give me a moment to retrieve the relevant data we have on this.")
        furhat.attend(Loc())
        TimeUnit.SECONDS.sleep(2)
        furhat.attend(user = users.random)
        // TODO check emotion
        furhat.say("While I'm looking for the data, I've noticed you are " + users.current.book.emotion +
                    " about this situation.")
        if (users.current.book.emotion == "unhappy") {
           furhat.say("I understand why you are unhappy and I'll try to fix the issue as fast as possible." +
           "It will probably not take more than 5 minutes.")
        } else if (users.current.book.emotion == "neutral") {
            furhat.say( "I'm glad you are not very upset about the issue you are experiencing. Let's quickly finish this up." )
        } else if (users.current.book.emotion == "happy") {
            furhat.say( "I'm glad you are satisfied with our support. Let's quickly finish this up and solve your issue.")
        }

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
            val random = Random.nextInt(3) + 1;
            if(random == 1){goto(RefundDelay)}
            else if(random == 2){goto(RefundInMotion)}
            else if(random == 3){goto(RefundNotStarted)}
        }
    }
}

val TryOrderAgain = state(Interaction) {
    onEntry {
        furhat.attend(user = users.random)
        furhat.ask("Let's try this again then. Can you repeat the order number? It can be found in the confirmation email of your order.")
        furhat.attend(Loc())
    }

    onResponse<No> { // TODO not an order number
        reentry()
    }
    onResponse<OrderNumber> {
        goto(FoundOrder)
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
        furhat.say("It looks like your package is on it's way and will be delivered within two days.")
        furhat.gesture(Gestures.Shake(strength = 0.5), async = true)
        furhat.gesture(Gestures.CloseEyes, async = true)
        furhat.say("We are sorry for the delay.")
        furhat.gesture(Gestures.OpenEyes, async = true)
        furhat.ask("As compensation I will send you a 20% discount coupon for your next order.")
    }
    onResponse<TooLate> {
        goto(ReturnPackage)
    }
    onResponse<Confirm> {
        goto(AnythingElse)
    }
}

val ReturnPackage = state(Interaction){
    onEntry {
        furhat.say ( "I am sorry to hear that. I send you a coupon you can use to have the product" +
                " returned and picked up from your home for free. On our website you can choose when " +
                "you would like the product to be picked up." )
        goto(AnythingElse)
    }
}

val NotSentYet = state(Interaction) {
    onEntry {
        furhat.say("It looks like something went wrong with the processing of your order. " +
                "The product has not been send to you, we are very sorry for the inconvenience. ")
        parallel {
            goto(LookQuestion)
        }
        furhat.ask("Would you still like to receive the product or would you like to cancel your order? ")
    }
    onResponse<Cancel> {
        goto(CancelOrder)
    }
    onResponse<StillReceive> {
        goto(ContinueOrder)
    }
}

val CancelOrder = state(Interaction) {
    onEntry {
        furhat.say ( "I will cancel the order right now and will send you a refund straight away. You can" +
                " expect the money to be back on your account within 3 days. Also I will send you a 20% " +
                "discount coupon for your next order, as a compromise for the inconvenience we caused you." )
        goto(AnythingElse)
    }
}

val ContinueOrder = state(Interaction) {
    onEntry {
        furhat.ask("Your order has been placed now. You will receive a comfirmation e-mail. Also we will send" +
                " it express, so you can expect your package to arrive tomorrow.")
    }
    onResponse<NotHome> {
        goto(DeliveryDate)
    }
}

val DeliveryDate = state(Interaction) {
    onEntry {
        furhat.gesture(Gestures.Shake(strength = 0.2), async = true)
        furhat.say ( "That is unfortunate. In the e-mail you have received you can select another delivery" +
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
        furhat.ask(" could you tell me what you intended to order? " +
                    "Please note that the only items we sell are laptops, Tv's, Playstations and headphones." )
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
        if( users.current.book.intendedOrder == users.current.book.receivedOrder){
            goto(SameOrder)
        }else{
            goto(NewOrder)
        }    }
    onResponse<Laptop> {
        users.current.book.intendedOrder = "laptop"
        if( users.current.book.intendedOrder == users.current.book.receivedOrder){
            goto(SameOrder)
        }else{
            goto(NewOrder)
        }    }
    onResponse<Playstation> {
        users.current.book.intendedOrder = "playstation"
        if( users.current.book.intendedOrder == users.current.book.receivedOrder){
            goto(SameOrder)
        }else{
            goto(NewOrder)
        }
    }
}

val SameOrder = state(Interaction){
    onEntry {
        furhat.ask("You've stated that you received a " +  users.current.book.receivedOrder +
                ". It appears that what you intended to order has already been send to you, are " +
                "you sure you meant a " + users.current.book.intendedOrder)
    }
    onResponse<No> {
        goto(IntendedOrder)
    }
    onResponse<Yes> {
        goto(CantHelp)
    }
}

val IntendedOrder = state(Interaction) {
    onEntry {
        furhat.ask("What did you intend to order? Please note that the only " +
                "items we sell are laptops, Tv's, Playstations and headphones.")
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
            ". I've also added free priority shipping to compensate for the receivement of a wrong package.")
        furhat.gesture(Gestures.BigSmile, async = true)
        furhat.gesture(Gestures.Wink(duration = 2.0), async = false)
        furhat.say(" When we deliver the new order we'll take the wrongly received " + users.current.book.receivedOrder + "with us.")
        parallel{
            goto(LookQuestion)
        }
        furhat.ask("Is there a day this week you are home before 5 pm?")
    }
    onResponse<No> {
        furhat.say ("That's ok, since free retour lasts 30 days there is still time left to return the " +
                users.current.book.receivedOrder +
                ". You can either make a return appointment at our website or keep the item." +
                " Please note that if you keep the item, we can not offer a refund." )
        goto(AskForFeedback)
    }
    onResponse<Yes> {
        goto(WhatDay)
    }
}

val WhatDay = state(Interaction){
    onEntry {
        val day = furhat.askFor<Days>("Which day would you like the package to be picked up?" )
        users.current.book.days  = day?.value
        furhat.say ("Excellent, I have send a confirmation mail to the same email as your previous order." +
                " We'll see you on "+ users.current.book.days )
        goto(AskForFeedback)
    }
}

val RefundDelay = state(Interaction){
    onEntry {
        parallel{
            goto(LookAround)
        }
        furhat.say("The refund should indeed have taken place, as the product was already returned to us 2 days ago. I apologize for this delay and make sure we send you the refund within 24 hours.")
        goto(AnythingElse)
    }
}

val RefundNotStarted = state(Interaction){
    onEntry {
        parallel{
            goto(LookAround)
        }
        furhat.say("It seems like the product you returned has only just arrived today. The payment process has been set in motion and you will be refunded within 24 hours.")
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
    onResponse<Yes> {
        goto(RefundNotFixed)
    }
    onResponse<No> {
        goto(RefundFixed)
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
            furhat.say("That's good to hear! If you could send an email to helpdesk@bol.com with a picture or scan of the proof of shipment, it will all be sorted out. We'll reply to you within 6 hours and then the refund will be processed within 24 hours. I apologize for the delay and extra effort this requested from you.")
        } else {
            furhat.say("I'm sorry to say that we cannot refund you at this point but there might be a way to fix this. If you contact the delivery company, they could find out what happened. Once the package has arrived, I'll make sure you are refunded within 24 hours.")
        }
        goto(AnythingElse)
    }
}

val RefundFixed = state(Interaction) {
    onEntry {
        furhat.say("It can take up to five working days for a package to arrive. I can notify you if your package has arrived or if it still hasn't after five days. Then we'll find out what happened and make sure you receive the refund.")
        parallel {
            goto(LookQuestion)
        }
        furhat.ask(" Would you like to be contacted via email, text message, or not at all?")
    }

    onResponse<EmailORText> {
        furhat.say("Alright, noted. We'll be in touch soon.")
        furhat.say("I sincerely hope the package will just arrive in no time and you do not need to put in any more effort than you already have.")
        goto(AnythingElse)
    }

    onResponse<No> { // TODO also include not at all
        furhat.say("Sure, no problem. You can always contact me via this channel 24/7.")
        furhat.say("I sincerely hope the package will just arrive in no time and you do not need to put in any more effort than you already have.")
        goto(AnythingElse)
    }

}

val AnythingElse = state(Interaction) {
    onEntry {
        parallel {
            goto(LookQuestion)
        }
        furhat.ask("Is there anything else I can do for you?")
    }

    onResponse<Yes> {
        if (users.current.book.emotion == "unhappy") {
            furhat.say("I'm sorry you are still unhappy with the whole situation")
        } else if (users.current.book.emotion == "happy") {
            furhat.say("I'm glad that I could be of service and it appears to me that you are satisfied with my support")
        }
        furhat.say("I think that one of my collegues can be of better help, I'll put you through.")
    }

    onResponse<No> {
        goto(AskForFeedback)
    }
}

val AskForFeedback = state(Interaction) {
    onEntry {
        if (users.current.book.emotion == "unhappy") {
            furhat.say("I've noticed you are unhappy. It would be great for me to know what caused this unhappiness.")
        } else if (users.current.book.emotion == "neutral") {
            furhat.say("I have a hard time estimating your emotions")
        } else if (users.current.book.emotion == "happy") {
            furhat.say("I've noticed you are happy. So it would be great if you told me what caused you to be happy.")
        }
        var givesFeedback = furhat.askYN("As you know I'm a bot that is ever improving. Would you be willing to give me " +
                "some tips so that I can provide you with a better service in the future?")
        if(givesFeedback!!) {
            goto(FeedbackRating)
        } else {
            furhat.say("That's alright. I wish you a nice day!")
        }
    }
}

val FeedbackRating = state(Interaction) {
    onEntry {
        furhat.gesture(Gestures.Nod(strength = 0.2), async = true)

        furhat.say("That's great!)")
        parallel {
            goto(LookQuestion)
        }
        furhat.ask("Overall, how would you rate our previous conversation? Bad, ok, or good?")
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
        goto(likeMost)
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
    onResponse {
        goto(AgreeAndPositive)
    }
}

val OKDoneDifferently = state(Interaction){
    onEntry {

        furhat.say("Ah I see.")
        parallel {
            goto(LookQuestion)
        }
        furhat.ask("What could I have done differently so that you would have rated the conversation as good?" )
    }
    onResponse {
        goto(AgreeAndPositive)
    }
}


val likeMost = state(Interaction){
    onEntry {

        furhat.say ( "That's nice to hear.")
        parallel {
            goto(LookQuestion)
        }
        furhat.ask("What did you like most about it?" )
    }
    onResponse {
        goto(BetterNextTime)
    }
}

val BetterNextTime = state(Interaction){
    onEntry {
        furhat.ask("")
    }
    onResponse {
        furhat.say("Thank you!")
        parallel {
            goto(LookQuestion)
        }
        furhat.ask("Was there also anything that I could do better next time?")
    }
    onResponse {
        goto(TakeInAccount)
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
    onResponse {
        goto(TakeInAccount)
    }
}

val TakeInAccount = state(Interaction){
    onEntry {
        furhat.say ( "Thank you for the feedback. I will take everything into account and learn from this." )
        furhat.say ( "I wish you a very nice day!" )
    }
}
