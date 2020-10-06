package furhatos.app.fruitseller.flow

import furhatos.app.fruitseller.book
import furhatos.app.fruitseller.nlu.*
/*
import furhatos.app.fruitseller.order
*/
import furhatos.flow.kotlin.*
import furhatos.nlu.common.*
import furhatos.nlu.common.Number
import furhatos.records.Location
import furhatos.util.Language
import kotlin.random.Random
fun Loc(): Location {
    val x = Random.nextInt(-5,5)
    val y = Random.nextInt(-1,5)
    val location = Location(x,y,20)
    return location
}

val LookAround = state(Interaction) {
    onEntry {
        furhat.attend(Loc())
        delay(Random.nextInt(3000,7000).toLong())
        furhat.attend(Loc())
    }

}

val Start = state(Interaction) {
    onEntry {
        furhat.attend(user = users.random)
        furhat.ask("Hello, how can I help you?")
    }

    onResponse<CheckIn> {
        goto(CheckingIn)
        //goto(FurtherDetails)
        // temp to get to wish state
//        goto(specificWishes)
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
        parallel {
            goto(LookAround)
        }
                furhat.say("Great! As the travel is longer than two days on our journey to Vulkan," +
                    " regulation requires we ask a few questions. Is that okay with you?")
                furhat.attend(user = users.random)
                furhat.ask("")

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
        parallel {
            goto(LookAround)
        }
        furhat.say("Without your information I cannot book you in. Are you sure")
        furhat.attend(user = users.random)
        furhat.ask("")

    }
    onResponse<Yes> {
        furhat.say("Goodbye then")
        goto(Idle)
    }

    onResponse<No> {
        goto(AmountGuests)
    }
}

val AmountGuests = state(Interaction) {
    onEntry {
        parallel {
            goto(LookAround)
        }
        furhat.say("Let's get started then. How many people would you like to check-in?")
        furhat.attend(user = users.random)
        furhat.ask("")
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
        parallel {
            goto(LookAround)
        }
        furhat.say("Great. By the way, would you like to know about the available amenities in our room?")
        furhat.attend(user = users.random)
        furhat.ask("")
    }
    onResponse<Yes> {
        goto(RandomQuestion1Yes)
    }

    onResponse<No> {
        println("the amount of people: " + "${users.current.book.people}")
        goto(FurtherDetails)
    }
}

val RandomQuestion1Yes = state(Interaction) {
    onEntry {
        parallel {
            goto(LookAround)
        }
        furhat.ask("You are provided a bed, table, a chair, and a Replicator, which allows you to instantly create " +
                "any dish you've ever wanted to eat, in the comfort of your own room. ", timeout = 0)

    }

    onNoResponse { goto (FurtherDetails) }
}

val FurtherDetails = state(Interaction) {
    onEntry {
        parallel {
            goto(LookAround)
        }
        val name = furhat.askFor<PersonName>("Perfect. Now, could you give me your name please?")
        furhat.attend(user = users.random)

        users.current.book.name  = name?.value
        parallel {
            goto(LookAround)
        }

        furhat.say("Thank you. How long do you intend to stay on" +
                "Starship Enterprise, and would you like to stay in our Suite-class rooms" +
                "or the Citizen-class rooms? (suite class have 2 beds, citizen-class have 1 bed)")
        furhat.attend(user = users.random)
        furhat.ask("")
    }

    onPartialResponse<GiveLengthStay> {
        println("Yes length partial")
        users.current.book.lengthStay = it.intent.stayL.toString()
        raise(it, it.secondaryIntent)
    }

    onResponse<GiveRoomClass> {
        println("Yes room")
        users.current.book.roomClass = it.intent.room.toString()
        goto(Summary) // is only for testing
        //goto(checkRooms)
    }

}

val Summary = state(Interaction) {
    onEntry {
        parallel {
            goto(LookAround)
        }
        furhat.say("would you like me to summarize?")
        furhat.attend(user = users.random)
        furhat.ask("")
    }
    onResponse<Yes> {
        furhat.say ( "so let me summarize")
        val name : String? = users.current.book.name
        val lengthstay : String? = users.current.book.lengthStay
        val roomClass : String? = users.current.book.roomClass
        println("your name is ${name}, you want to stay ${lengthstay}, and you want rooms of type ${roomClass}")
        goto(checkRooms)
    }

    onResponse<No> {
        goto(checkRooms)
    }
}

val checkRooms : State = state(Interaction){
    onEntry {
        parallel {
            goto(LookAround)
        }

        val people : Int? = users.current.book.people?.value
        var SuitesNeeded : Int? = 0
        var CitroomsNeeded : Int? = people
        val SuitesAvailable : Int? = users.current.book.suiteRooms?.value
        val CitroomsAvailable : Int? = users.current.book.citiRooms?.value
        if (users.current.book.roomClass == "suite") {
            SuitesNeeded = people?.div(2) // not tested if this floors or ceilings, but it needs to ceiling
            CitroomsNeeded = 0 // use this if line below is NOT used.
            // CitroomsNeeded = people?.rem(2) // for uneven people and suite choice, eg 3 people = 1 suite and 1 citizen room.
            // if line above is used, SuitesNeeded needs to floor.
        }
        if ((SuitesAvailable!! >= SuitesNeeded!!) && (CitroomsAvailable!! >= CitroomsNeeded!!)) {
            users.current.book.suiteRooms = Number(SuitesAvailable - SuitesNeeded)
            users.current.book.citiRooms = Number(CitroomsAvailable - CitroomsNeeded)
            goto(SpecificWishes)
        } else {
            goto(StarshipOverloaded)
        }
    }
}

val StarshipOverloaded = state(Interaction) {
    onEntry {
        parallel {
            goto(LookAround)
        }

        val number : Number? = if (users.current.book.roomClass == "suite") users.current.book.suiteRooms else users.current.book.citiRooms
        furhat.say("Unfortunately there are no rooms left of this kind. " +
                "We only have $number rooms of this kind free. ")
        furhat.attend(user = users.random)
        furhat.ask("Would you like to change the number of people you are checking in?", timeout = 5000)
    }

    onResponse<No> {
        goto(CheckinCancel)
    }
    onResponse<Yes> {
        goto(NumberOfPeopleChange)
    }
    onNoResponse {
        goto(CheckinCancel)
    }
}

val SpecificWishes = state(Interaction){
    onEntry {
        parallel {
            goto(LookAround)
        }

        val name : String? = users.current.book.name
        furhat.say("Amazing. The data has been entered to your name, ${name}")
        furhat.attend(user = users.random)
        furhat.ask("Now, before asking you about the different activities we offer on board, I would like to " +
                        "ask you if you have any specific wishes for your stay here?")
    }
    onResponse<No> {
        goto(NoWishes)
    }
    onResponse<TellWish> {
        goto(ExtraWish)
    }
}

val ExtraWish = state(Interaction){
    onEntry {
        parallel {
            goto(LookAround)
        }

        val random = Random.nextInt(1, 3)
        if(random == 1 ){
            furhat.say("Understood. Anything else?")
            furhat.attend(user = users.random)
            furhat.ask("")
        }
        else if(random == 2){
            furhat.say("Do you have any more wishes?")
            furhat.attend(user = users.random)
            furhat.ask("")
        }
        else if(random == 3){
            furhat.say("Is there something else you wish for?")
            furhat.attend(user = users.random)
            furhat.ask("")

        }
    }
    onResponse<TellWish> {
        reentry()
    }
    onResponse<No> {
        goto(EndWishes)
    }
}

val StarshipActivities = state(Interaction){
    onEntry {
        parallel {
            goto(LookAround)
        }

        furhat.say("On Starship Enterprise we offer numerous simulated activities, namely:" +
                "Skiing, Tennis, Badminton, and Zombie Survival. Please tell me which ones of" +
                "those activities you would like to sign up for today.")
        furhat.attend(user = users.random)
        furhat.ask("")
    }
    onResponse<No>{
        goto(endState)
    }
    onResponse<listActivities>{
        goto(endState)
    }

}

val endState = state(Interaction){
    onEntry {
        parallel {
            goto(LookAround)
        }

        furhat.say("Understood. You have now successfully checked in. You will soon be teleported to your" +
                "room, and your luggage will be delivered by our staff.")
        furhat.attend(user = users.random)
        furhat.say("We hope your stay at Starship Enterprise will be a fun and relaxing one.")

    }
}

val CheckinCancel = state(Interaction){
    onEntry {
        parallel {
            goto(LookAround)
        }

        furhat.say("Alright then, please tell me if you'd like to start over." +
                    " Otherwise, I wish you a good day.")
        furhat.attend(user = users.random)
        furhat.ask("")
    }
    onNoResponse {
        goto(Start)
    }
}

val NumberOfPeopleChange = state(Interaction) {
    onEntry {
        parallel {
            goto(LookAround)
        }

        furhat.say("Wonderful. Please tell me how many guests you would like to check in.")
        furhat.attend(user = users.random)
        furhat.ask("")
    }

    onResponse<TellPeople> {
        val people = it.intent.values[0].toString().toInt()
        users.current.book.people = Number(people)
        println("$people " + if (people == 1) "person" else "people" )
        goto(FurtherDetails)
    }
}

val Explaining = state(Interaction) {
    onEntry {
        parallel {
            goto(LookAround)
        }

        random(
                {
                    furhat.say("Welcome to StarShip Enterprise. We are currently leaving" +
                        " for a 12-day voyage from planet Earth to planet Vulkan." +
                        " My name is Data and I am your check-in assistant for today." +
                        " Would you like to check in?")
                    furhat.attend(user = users.random)
                    furhat.ask("")
                })
    }

    onResponse<Yes> {
        goto(CheckingIn)
    }
    onResponse<CheckIn> {
        goto(CheckingIn)
    }
}

val EndWishes = state(Interaction) {
    onEntry {
        parallel {
            goto(LookAround)
        }

        furhat.say("All right, your demands have been noted and will be read by the crew. Let's move on then.")
        goto(StarshipActivities)
    }
}

val NoWishes = state(Interaction) {
    onEntry {
        parallel {
            goto(LookAround)
        }

        furhat.say("Alright, then let's move on.")
        goto(StarshipActivities)
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
