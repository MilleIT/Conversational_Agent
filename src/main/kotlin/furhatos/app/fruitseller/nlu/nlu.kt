package furhatos.app.fruitseller.nlu

import furhatos.nlu.*
import furhatos.nlu.grammar.Grammar
import furhatos.nlu.kotlin.grammar
import furhatos.nlu.common.Number
import furhatos.nlu.common.PersonName
import furhatos.util.Language

class RequestOptions: Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("What options do you have?",
                "What fruits do you have?",
                "What are the alternatives?",
                "What do you have?")
    }
}

class PeopleList : ListEntity<QuantifiedPeople>()

class QuantifiedPeople(
        var count : Number? = Number(1),
        var person : Person? = null) : ComplexEnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("@count @person", "@person")
    }

//    fun getCount(): Int {
//        return generate("$count").toInt()
//    }

    override fun toText(): String {
        return generate("${count?.value}")
    }
//    override fun toText(): String {
//        return generate("$count " + if (count?.value == 1) person?.value else "${person?.value}")
//    }
}

class Person : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("person", "people")
    }
}

class AvailableDays(var days : Days? = null) : ComplexEnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("@days")
    }
}

class Days : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    }
}

class Headphones() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("Headphones")
    }
}
class Laptop() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("Laptop")
    }
}
class Television() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("Television", "TV")
    }
}
class Playstation() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("Playstation")
    }
}

class NoDay() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("No day", "None", "I am not home this week")
    }
}

class WrongPackage() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("I got the wrong package")
    }
}

class NoPackage() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("My package didn't arrive", "I didn't receive my package")
    }
}

class NoRefund() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("I didn't receive my refund")
    }
}

class OrderAndName(var ordernumber : Number? = null, var name : PersonName? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("@ordernumber, @name", "My ordernumber is @ordernumber and my last name is @name")
    }
}

class OrderNumber(var ordernumber : Number? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("@ordernumber", "My ordernumber is @ordernumber")
    }
}

class Bad() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("Bad", "Poor", "Disappointing", "Unsatisfactory", "Hopeless", "Unacceptable", "Shit", "Crap", "Sucked", "Useless", "Terrible", "Awful", "Miserable")
    }
}

class Ok() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("Ok", "Alright", "Not too bad", "Fine", "Average", "Okay", "Standard", "Fair")
    }
}

class Good() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("Good", "Very good", "Nice", "Very nice", "Fantastic", "Excellent", "Super", "Glorious", "Impressive", "Outstanding", "Remarkable")
    }
}

class Cancel() : Intent(){
    override fun getExamples(lang: Language): List<String> {
        return listOf("I would like to cancel my order", "I want to cancel it", "I want to cancel my order", "cancel order")
    }
}

class StillReceive() : Intent(){
    override fun getExamples(lang: Language): List<String> {
        return listOf("I would still like to receive my order")
    }
}

class NotHome() : Intent(){
    override fun getExamples(lang: Language): List<String> {
        return listOf("I am not home tomorrow")
    }
}

class TooLate() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("That is too late, I don't want it anymore.")
    }
}

class Confirm() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("Okay", "Allright")
    }
}











class TellPeople(var people: PeopleList? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("@people")
    }
}

class StayList : ListEntity<QuantifiedStay>()

class QuantifiedStay(
        var count : Number? = Number(1),
        var stay : Staytype? = null) : ComplexEnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("@count @stay")
    }

    override fun toText(): String {
        return generate("${count?.value} " + if (count?.value == 1) stay?.value else "${stay?.value}s")

    }
}
class Staytype : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("day", "days", "week", "weeks")
    }
}
class Roomtype : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("suite", "citizen")
    }
}

class GiveLengthStay(var stayL : StayList? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("@stayL")
    }
}

class GiveRoomClass(var room : Roomtype? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("@room")
    }
}

class GiveName(var naam: UserName? = null) : Intent(){
    override fun getExamples(lang: Language): List<String> {
        return listOf("@naam")
    }
}

class UserName(var name : String? = null) : ComplexEnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("@name")
    }

    override fun toText(): String {
        return generate("$name")
    }
}

class Details(var name: PersonName? = null, var stay: StayList? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("@name", "@stay", "suite", "citizen")
    }
}

class Wishes : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("extra blanket", "bottle of water", "room service")
    }
}

class WishList : ListEntity<QuantifiedWishes>()

class QuantifiedWishes(
        val count : Number? = Number(1),
        val wish : Wishes? = null) : ComplexEnumEntity() {

    override fun getEnum(lang: Language): List<String> {
        return listOf("@count @wish", "@wish")
    }

    override fun toText(): String {
        return generate("$count $wish")
    }
}

class TellWish(val wish : WishList? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("@wish")
    }
}

class Activities : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("Skiing", "Tennis", "Badminton", "Zombie Survival")
    }
}

class ActivitiesList : ListEntity<QuantifiedActivities>()

class QuantifiedActivities(
        val count : Number? = Number(1),
        val activity : Activities? = null) : ComplexEnumEntity() {

    override fun getEnum(lang: Language): List<String> {
        return listOf("@count @activity", "@activity")
    }

    override fun toText(): String {
        return generate("$count $activity")
    }
}

class listActivities(val activities : ActivitiesList? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("@activities")
    }
}








