package furhatos.app.fruitseller.flow

import furhatos.app.fruitseller.book
import furhatos.app.fruitseller.nlu.*
/*
import furhatos.app.fruitseller.order
*/
import furhatos.flow.kotlin.*
import furhatos.nlu.common.*
import furhatos.util.Language

val Start = state(Interaction) {
    onEntry {
        random(
            {   furhat.ask("Hello, how can I help you?") }
        )
    }
    onResponse<CheckIn> {
        goto(CheckingIn)
    }
    onResponse<Confusion> {
        goto(Explaining)
    }
}
/*val Options = state(Interaction) {
    onResponse<BuyFruit> {
        val fruits = it.intent.fruits
        if (fruits != null) {
            goto(OrderReceived(fruits))
        }
        else {
            propagate()
        }
    }

    onResponse<RequestOptions> {
        furhat.say("We have ${Fruit().optionsToText()}")
        furhat.ask("Do you want some?")
    }

    onResponse<Yes> {
        random(
                { furhat.ask("What kind of fruit do you want?") },
                { furhat.ask("What type of fruit?") }
        )
    }
}*/

val CheckingIn = state(Interaction) {
    onEntry {
        random(
            { furhat.ask(" Great! As the travel is longer than two days on our journey to Vulkan," +
                    " regulation requires we ask a few questions. Is that okay with you?") })
    }

    onResponse<Yes> {
        goto(AmountGuests)
    }

    onResponse<No> {
        goto(NoInfo)
    }
}

val NoInfo = state(Interaction) {
    onEntry {
        furhat.ask(" Without your information I cannot book you in. Are you sure")
    }
    onResponse<Yes> {
        furhat.say { "Goodbye then" }
        goto(Idle)
    }

    onResponse<No> {
        goto(AmountGuests)
    }
}

val AmountGuests = state(Interaction) {
    onEntry {
        furhat.ask("Let's get started then. How many people would you like to check-in?")
    }
    onResponse<TellPeople> {
        val people = it.intent.values[0].toString().toInt()
//        furhat.say { "$people" + if (people == 1) "person" else "people" }
//        println("${it.intent}")
//        println("${it.intent.values[0].toString().toInt()}")
        users.current.book.people = Number(people)
        println("$people " + if (people == 1) "person" else "people" )
        goto(RandomQuestion)
    }
}

val RandomQuestion = state(Interaction) {
    onEntry {
        furhat.ask("Great. By the way, would you like to know about the available amenities in our room?")
    }
    onResponse<Yes> {
        furhat.say { "You are provided a bed, table, a chair, and a Replicator, which allows you to instantly create " +
                "any dish you've ever wanted to eat, in the comfort of your own room. " }
        goto(FurtherDetails)
    }

    onResponse<No> {
        println("the amount of people: " + "${users.current.book.people}")
        goto(FurtherDetails)
    }
}

val FurtherDetails = state(Interaction) {
    onEntry {
        furhat.ask(" Perfect. Now, could you give me your name, how long you intend to stay on" +
                "Starship Enterprise, and whether you would like to stay in our Suite-class rooms" +
                "or the Citizen-class rooms? (suite class have 2 beds, citizen-class have 1 bed)")
    }

    onResponse<GiveName> {
        furhat.say("Yes")
        reentry()
    }

    onResponse<GiveLengthStay> {
        furhat.say("No")
        reentry()
    }

    onResponse<GiveRoomClass> {
        furhat.say("No")
        reentry()
    }

    onPartialResponse<GiveName> {
        furhat.say("Yes name partial")
        raise(it, it.secondaryIntent)
    }

    onPartialResponse<GiveLengthStay> {
        furhat.say("Yes length partial")
        raise(it, it.secondaryIntent)
    }

    onPartialResponse<GiveRoomClass> {
        furhat.say("Yes room partial")

    }

}


val starshipOverloaded = state(Interaction) {
    onEntry {
        furhat.say(" Unfortunately there are no rooms left of this kind. " +
                "We only have <number> rooms of this kind free. " +
                "Would you like to change the number of people you are checking in?")
        furhat.say { "rooms" }
        furhat.ask("rooms of this kind free. " +
                "Would you like to change the number of people you are checking in?", timeout = 5000)
    }

    onResponse<No> {
        goto(checkinCancel)
    }
    onResponse<Yes> {
        goto(numberOfPeopleChange)
    }
    onNoResponse {
        goto(checkinCancel)
    }

}

val checkinCancel = state(Interaction){
    onEntry {
            furhat.say { "Alright then, please tell me if you'd like to start over." +
                    " Otherwise, I wish you a good day." }
    }
    onNoResponse {
        goto(Start)
    }
}

val numberOfPeopleChange = state(Interaction) {
    onEntry {
        furhat.ask(" Wonderful. Please tell me how many guests you would like to check in.")
    }

}

val Explaining = state(Interaction) {
    onEntry {
        random(
                { furhat.ask(" Welcome to StarShip Enterprise. We are currently leaving" +
                        " for a 12-day voyage from planet Earth to planet Vulkan." +
                        " My name is Data and I am your check-in assistant for today." +
                        " Would you like to check in?") })
    }

    onResponse<Yes> {
        goto(CheckingIn)
    }
    onResponse<CheckIn> {
        goto(CheckingIn)
    }
}

/*
fun OrderReceived(fruits: FruitList) : State = state(Options) {
    onEntry {
        furhat.say("${fruits.text}, what a lovely choice!")
        fruits.list.forEach {
            users.current.order.fruits.list.add(it)
        }
        furhat.ask("Anything else?")
    }

    onReentry {
        furhat.ask("Did you want something else?")
    }

    onResponse<No> {
        furhat.say("Okay, here is your order of ${users.current.order.fruits}. Mission accomplished!")
    }
}*/
